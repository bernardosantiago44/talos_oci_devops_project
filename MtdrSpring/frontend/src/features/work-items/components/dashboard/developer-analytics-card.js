import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useMemo, useState } from 'react';
import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis, } from 'recharts';
import { Clock, Filter, ListChecks, Target } from 'lucide-react';
import { useAnalyticsDashboard } from '@/hooks/api';
const DEV_COLORS = ['#2563eb', '#f97316', '#16a34a', '#dc2626', '#8b5cf6', '#0891b2', '#db2777', '#ca8a04'];
function compareNames(a, b) {
    return a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' });
}
function uniqueSorted(values) {
    return Array.from(new Set(values)).sort(compareNames);
}
function readNumber(value) {
    return typeof value === 'number' && Number.isFinite(value) ? value : 0;
}
function normalizeRows(rows) {
    return rows
        .map((row) => ({
        developer: row.DEVELOPER ?? row.developer ?? '',
        sprint: row.SPRINT_NAME ?? row.SPRINT ?? row.sprint ?? '',
        tasks: readNumber(row.TASKS_COMPLETED ?? row.tasksCompleted),
        hours: readNumber(row.REAL_HOURS ?? row.TOTAL_HOURS_WORKED ?? row.totalHoursWorked),
    }))
        .filter((row) => row.developer && row.sprint);
}
function buildGroupedBars(rows, sprints, developers, metric) {
    return sprints.map((sprint) => {
        const chartRow = { sprint };
        for (const developer of developers) {
            const row = rows.find((item) => item.sprint === sprint && item.developer === developer);
            chartRow[developer] = row ? Number(row[metric].toFixed(metric === 'hours' ? 1 : 0)) : 0;
        }
        return chartRow;
    });
}
function summarizeByDeveloper(rows) {
    const summary = new Map();
    for (const row of rows) {
        const current = summary.get(row.developer) ?? {
            developer: row.developer,
            sprint: 'summary',
            tasks: 0,
            hours: 0,
        };
        current.tasks += row.tasks;
        current.hours += row.hours;
        summary.set(row.developer, current);
    }
    return Array.from(summary.values()).sort((a, b) => b.hours - a.hours);
}
function ChartTooltip({ active, payload, label }) {
    if (!active || !payload?.length)
        return null;
    return (_jsxs("div", { className: "rounded-xl border border-zinc-200 bg-white px-3 py-2 shadow-lg dark:border-zinc-700 dark:bg-zinc-800", children: [_jsx("p", { className: "mb-1 text-xs font-semibold text-zinc-700 dark:text-zinc-200", children: label }), payload.map((entry) => (_jsxs("p", { className: "text-xs", style: { color: entry.color }, children: [entry.name, ": ", _jsx("span", { className: "font-semibold tabular-nums", children: Number(entry.value).toLocaleString() })] }, entry.dataKey)))] }));
}
function KpiCard({ icon, label, value, sub, accent, }) {
    return (_jsxs("div", { className: "rounded-xl border border-zinc-200 bg-white p-4 dark:border-zinc-800 dark:bg-zinc-900/60", children: [_jsxs("div", { className: "flex items-center justify-between gap-3", children: [_jsx("p", { className: "text-[11px] font-semibold uppercase tracking-wider text-zinc-500 dark:text-zinc-400", children: label }), _jsx("div", { className: `flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${accent}`, children: icon })] }), _jsx("p", { className: "mt-2 text-2xl font-bold tabular-nums text-zinc-900 dark:text-zinc-100", children: value }), _jsx("p", { className: "mt-0.5 text-xs text-zinc-500 dark:text-zinc-500", children: sub })] }));
}
function SprintGroupedBarChart({ title, subtitle, data, developers, allowDecimals, }) {
    return (_jsxs("div", { className: "rounded-xl border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-900/60", children: [_jsxs("div", { className: "mb-4", children: [_jsx("h3", { className: "text-sm font-semibold text-zinc-800 dark:text-zinc-100", children: title }), _jsx("p", { className: "mt-0.5 text-xs text-zinc-500 dark:text-zinc-500", children: subtitle })] }), _jsx("div", { className: "h-80 min-w-0", children: _jsx(ResponsiveContainer, { width: "100%", height: "100%", children: _jsxs(BarChart, { data: data, margin: { top: 8, right: 8, left: -18, bottom: 8 }, barGap: 2, children: [_jsx(CartesianGrid, { strokeDasharray: "3 3", stroke: "#e4e4e7", vertical: false }), _jsx(XAxis, { dataKey: "sprint", tick: { fontSize: 11 }, axisLine: false, tickLine: false, interval: 0 }), _jsx(YAxis, { tick: { fontSize: 11 }, axisLine: false, tickLine: false, allowDecimals: allowDecimals }), _jsx(Tooltip, { content: _jsx(ChartTooltip, {}), cursor: { fill: 'rgba(15, 23, 42, 0.04)' } }), _jsx(Legend, { iconType: "circle", iconSize: 8, wrapperStyle: { fontSize: 12, paddingTop: 12 } }), developers.map((developer, index) => (_jsx(Bar, { dataKey: developer, name: developer, fill: DEV_COLORS[index % DEV_COLORS.length], radius: [4, 4, 0, 0], maxBarSize: 34 }, developer)))] }) }) })] }));
}
export function DeveloperAnalyticsCard() {
    const dashboardQuery = useAnalyticsDashboard();
    const [selectedSprint, setSelectedSprint] = useState('all');
    const rows = useMemo(() => normalizeRows((dashboardQuery.data?.chartData ?? [])), [dashboardQuery.data?.chartData]);
    const developers = useMemo(() => uniqueSorted(rows.map((row) => row.developer)), [rows]);
    const sprints = useMemo(() => uniqueSorted(rows.map((row) => row.sprint)), [rows]);
    const tasksBySprint = useMemo(() => buildGroupedBars(rows, sprints, developers, 'tasks'), [developers, rows, sprints]);
    const hoursBySprint = useMemo(() => buildGroupedBars(rows, sprints, developers, 'hours'), [developers, rows, sprints]);
    const sprintRows = selectedSprint === 'all' ? rows : rows.filter((row) => row.sprint === selectedSprint);
    const developerSummary = summarizeByDeveloper(sprintRows);
    const totalTasks = developerSummary.reduce((sum, row) => sum + row.tasks, 0);
    const totalHours = developerSummary.reduce((sum, row) => sum + row.hours, 0);
    const avgHoursPerDeveloper = developerSummary.length > 0 ? totalHours / developerSummary.length : 0;
    const sprintLabel = selectedSprint === 'all' ? 'All sprints' : selectedSprint;
    const hoursPieData = developerSummary
        .filter((row) => row.hours > 0)
        .map((row, index) => ({
        name: row.developer,
        value: Number(row.hours.toFixed(1)),
        fill: DEV_COLORS[developers.indexOf(row.developer) % DEV_COLORS.length] ?? DEV_COLORS[index % DEV_COLORS.length],
    }));
    if (dashboardQuery.isLoading) {
        return (_jsx("div", { className: "space-y-4", children: [1, 2, 3, 4].map((i) => (_jsx("div", { className: "h-28 animate-pulse rounded-xl bg-zinc-100 dark:bg-zinc-800" }, i))) }));
    }
    if (rows.length === 0) {
        return (_jsxs("div", { className: "flex flex-col items-center justify-center rounded-xl border border-dashed border-zinc-300 py-16 text-center dark:border-zinc-700", children: [_jsx("p", { className: "text-sm font-medium text-zinc-400", children: "No completed tasks or logged hours yet" }), _jsx("p", { className: "mt-1 text-xs text-zinc-300 dark:text-zinc-600", children: "Complete tasks and register minutes to see sprint KPIs." })] }));
    }
    return (_jsxs("div", { className: "space-y-5", children: [_jsxs("div", { className: "grid gap-3 sm:grid-cols-2 xl:grid-cols-4", children: [_jsx(KpiCard, { icon: _jsx(ListChecks, { className: "h-4 w-4" }), label: "Completed Tasks", value: String(totalTasks), sub: `${sprintLabel} total`, accent: "bg-indigo-500/15 text-indigo-600 dark:text-indigo-400" }), _jsx(KpiCard, { icon: _jsx(Clock, { className: "h-4 w-4" }), label: "Logged Hours", value: `${totalHours.toFixed(1)}h`, sub: `${sprintLabel} total`, accent: "bg-cyan-500/15 text-cyan-600 dark:text-cyan-400" }), _jsx(KpiCard, { icon: _jsx(Target, { className: "h-4 w-4" }), label: "Hours / Task", value: totalTasks > 0 ? `${(totalHours / totalTasks).toFixed(1)}h` : '0h', sub: "logged effort per completed task", accent: "bg-emerald-500/15 text-emerald-600 dark:text-emerald-400" }), _jsx(KpiCard, { icon: _jsx(Filter, { className: "h-4 w-4" }), label: "Avg Hours / Dev", value: `${avgHoursPerDeveloper.toFixed(1)}h`, sub: `${sprintLabel} average`, accent: "bg-amber-500/15 text-amber-600 dark:text-amber-400" })] }), _jsx("div", { className: "rounded-xl border border-zinc-200 bg-white p-4 dark:border-zinc-800 dark:bg-zinc-900/60", children: _jsxs("div", { className: "flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between", children: [_jsxs("div", { children: [_jsx("p", { className: "text-sm font-semibold text-zinc-800 dark:text-zinc-100", children: "Sprint focus" }), _jsx("p", { className: "text-xs text-zinc-500 dark:text-zinc-500", children: "Filters the KPI cards, hour share, and developer detail table." })] }), _jsxs("select", { value: selectedSprint, onChange: (event) => setSelectedSprint(event.target.value), className: "w-full rounded-lg border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30 sm:w-56", children: [_jsx("option", { value: "all", children: "All sprints" }), sprints.map((sprint) => (_jsx("option", { value: sprint, children: sprint }, sprint)))] })] }) }), _jsx(SprintGroupedBarChart, { title: "Tasks Completed by Developer per Sprint", subtitle: "Use one grouped chart to compare a developer across Sprints.", data: tasksBySprint, developers: developers }), _jsx(SprintGroupedBarChart, { title: "Logged Hours by Developer per Sprint", subtitle: "This answers why a developer has only Y hours in Sprint Z: it shows the logged time-entry total by developer and sprint.", data: hoursBySprint, developers: developers, allowDecimals: true }), _jsxs("div", { className: "grid gap-5 xl:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]", children: [_jsxs("div", { className: "rounded-xl border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-900/60", children: [_jsxs("h3", { className: "text-sm font-semibold text-zinc-800 dark:text-zinc-100", children: ["Logged Hours Share - ", sprintLabel] }), _jsx("p", { className: "mt-0.5 text-xs text-zinc-500 dark:text-zinc-500", children: "Shows which developers account for the selected sprint's registered hours." }), _jsx("div", { className: "mt-4 h-72", children: hoursPieData.length === 0 ? (_jsx("div", { className: "flex h-full items-center justify-center text-sm text-zinc-400 dark:text-zinc-500", children: "No hours logged for this sprint." })) : (_jsx(ResponsiveContainer, { width: "100%", height: "100%", children: _jsxs(PieChart, { children: [_jsx(Pie, { data: hoursPieData, cx: "50%", cy: "48%", innerRadius: 58, outerRadius: 88, paddingAngle: 3, dataKey: "value", nameKey: "name", children: hoursPieData.map((entry) => (_jsx(Cell, { fill: entry.fill }, entry.name))) }), _jsx(Tooltip, { content: _jsx(ChartTooltip, {}) }), _jsx(Legend, { iconType: "circle", iconSize: 8, wrapperStyle: { fontSize: 12 } })] }) })) })] }), _jsxs("div", { className: "rounded-xl border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-900/60", children: [_jsxs("h3", { className: "text-sm font-semibold text-zinc-800 dark:text-zinc-100", children: ["Developer Detail - ", sprintLabel] }), _jsx("p", { className: "mt-0.5 text-xs text-zinc-500 dark:text-zinc-500", children: "Hours, completed tasks, and variance from the selected sprint average." }), _jsx("div", { className: "mt-4 overflow-x-auto", children: _jsxs("table", { className: "min-w-full text-left text-sm", children: [_jsx("thead", { children: _jsxs("tr", { className: "border-b border-zinc-200 text-xs uppercase tracking-wider text-zinc-400 dark:border-zinc-800", children: [_jsx("th", { className: "py-2 pr-4 font-semibold", children: "Developer" }), _jsx("th", { className: "py-2 pr-4 font-semibold", children: "Tasks" }), _jsx("th", { className: "py-2 pr-4 font-semibold", children: "Hours" }), _jsx("th", { className: "py-2 pr-4 font-semibold", children: "Hours / Task" }), _jsx("th", { className: "py-2 font-semibold", children: "Vs Avg Hours" })] }) }), _jsx("tbody", { children: developerSummary.map((row, index) => {
                                                const color = DEV_COLORS[developers.indexOf(row.developer) % DEV_COLORS.length] ?? DEV_COLORS[index % DEV_COLORS.length];
                                                const hoursPerTask = row.tasks > 0 ? row.hours / row.tasks : 0;
                                                const hourGap = row.hours - avgHoursPerDeveloper;
                                                return (_jsxs("tr", { className: "border-b border-zinc-100 last:border-0 dark:border-zinc-800/80", children: [_jsxs("td", { className: "py-3 pr-4 font-medium text-zinc-800 dark:text-zinc-100", children: [_jsx("span", { className: "mr-2 inline-block h-2.5 w-2.5 rounded-full", style: { backgroundColor: color } }), row.developer] }), _jsx("td", { className: "py-3 pr-4 tabular-nums text-zinc-600 dark:text-zinc-300", children: row.tasks }), _jsxs("td", { className: "py-3 pr-4 tabular-nums text-zinc-600 dark:text-zinc-300", children: [row.hours.toFixed(1), "h"] }), _jsx("td", { className: "py-3 pr-4 tabular-nums text-zinc-600 dark:text-zinc-300", children: row.tasks > 0 ? `${hoursPerTask.toFixed(1)}h` : 'No completed tasks' }), _jsxs("td", { className: `py-3 tabular-nums font-medium ${hourGap < 0 ? 'text-rose-500 dark:text-rose-400' : 'text-emerald-600 dark:text-emerald-400'}`, children: [hourGap >= 0 ? '+' : '', hourGap.toFixed(1), "h"] })] }, row.developer));
                                            }) })] }) })] })] })] }));
}
