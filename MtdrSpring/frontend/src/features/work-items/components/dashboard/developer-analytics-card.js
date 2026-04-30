import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell, PieChart, Pie, Legend, } from 'recharts';
import { Lightbulb, Zap } from 'lucide-react';
import { useAnalyticsDashboard } from '@/hooks/api';
function aggregateByDeveloper(rows) {
    const map = new Map();
    for (const row of rows) {
        const existing = map.get(row.DEVELOPER);
        if (existing) {
            existing.tasksCompleted += row.TASKS_COMPLETED;
            existing.realHours += row.REAL_HOURS;
        }
        else {
            map.set(row.DEVELOPER, {
                developer: row.DEVELOPER,
                tasksCompleted: row.TASKS_COMPLETED,
                realHours: row.REAL_HOURS,
            });
        }
    }
    return Array.from(map.values()).sort((a, b) => b.tasksCompleted - a.tasksCompleted);
}
function generateInsights(devs, kpis) {
    if (devs.length === 0)
        return ['No data available yet.'];
    const insights = [];
    const topTaskDev = devs[0];
    const topHoursDev = [...devs].sort((a, b) => b.realHours - a.realHours)[0];
    const noHours = devs.filter(d => d.realHours === 0);
    const avgTasks = kpis.avgTasksPerDev;
    insights.push(`${topTaskDev.developer} leads in completed tasks with ${topTaskDev.tasksCompleted} tasks.`);
    if (topHoursDev.realHours > 0)
        insights.push(`${topHoursDev.developer} logged the most real hours: ${topHoursDev.realHours.toFixed(1)}h.`);
    if (noHours.length > 0)
        insights.push(`${noHours.map(d => d.developer).join(', ')} ${noHours.length === 1 ? 'has' : 'have'} no hours logged yet.`);
    const over = devs.filter(d => d.tasksCompleted > avgTasks * 1.3);
    if (over.length > 0)
        insights.push(`${over.map(d => d.developer).join(', ')} ${over.length === 1 ? 'exceeds' : 'exceed'} the team average (${avgTasks.toFixed(1)} tasks/dev) by more than 30%.`);
    const under = devs.filter(d => d.tasksCompleted < avgTasks * 0.6);
    if (under.length > 0)
        insights.push(`${under.map(d => d.developer).join(', ')} ${under.length === 1 ? 'is' : 'are'} below 60% of the team average.`);
    return insights;
}
function generateActions(devs, kpis) {
    if (devs.length === 0)
        return ['Log tasks and hours to generate recommendations.'];
    const actions = [];
    const noHours = devs.filter(d => d.realHours === 0);
    if (noHours.length > 0)
        actions.push(`Ask ${noHours.map(d => d.developer).join(', ')} to log real hours on each task.`);
    const avgTasks = kpis.avgTasksPerDev;
    const under = devs.filter(d => d.tasksCompleted < avgTasks * 0.6);
    if (under.length > 0)
        actions.push(`Review workload for ${under.map(d => d.developer).join(', ')} — consider redistributing tasks or identifying blockers.`);
    const maxTasks = devs[0]?.tasksCompleted ?? 0;
    const minTasks = devs[devs.length - 1]?.tasksCompleted ?? 0;
    if (devs.length > 1 && maxTasks > minTasks * 2)
        actions.push('Task distribution is uneven — rebalance the backlog in the next sprint planning session.');
    const highLow = devs.filter(d => d.realHours > kpis.avgHoursPerDev * 1.5 && d.tasksCompleted < avgTasks);
    if (highLow.length > 0)
        actions.push(`${highLow.map(d => d.developer).join(', ')} is spending many hours but completing few tasks — review task estimates or complexity.`);
    if (actions.length === 0)
        actions.push('The team is working in a balanced way. Keep up the current pace.');
    return actions;
}
const DEV_COLORS = ['#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];
const CustomTooltip = ({ active, payload, label }) => {
    if (!active || !payload?.length)
        return null;
    return (_jsxs("div", { className: "rounded-xl border border-zinc-200 bg-white px-3 py-2 shadow-lg dark:border-zinc-700 dark:bg-zinc-800", children: [_jsx("p", { className: "mb-1 text-xs font-semibold text-zinc-700 dark:text-zinc-200", children: label }), payload.map((p) => (_jsxs("p", { className: "text-xs", style: { color: p.color }, children: [p.name, ": ", _jsx("span", { className: "font-bold", children: typeof p.value === 'number' && p.value % 1 !== 0 ? p.value.toFixed(1) : p.value })] }, p.dataKey)))] }));
};
function KpiCard({ label, value, sub, color }) {
    return (_jsxs("div", { className: `rounded-2xl p-4 ${color}`, children: [_jsx("p", { className: "text-[11px] font-semibold uppercase tracking-wider opacity-70", children: label }), _jsx("p", { className: "mt-1 text-3xl font-bold tabular-nums", children: value }), _jsx("p", { className: "mt-0.5 text-[11px] opacity-60", children: sub })] }));
}
function SectionHeader({ children }) {
    return (_jsx("h3", { className: "mb-4 text-sm font-semibold text-zinc-700 dark:text-zinc-200", children: children }));
}
export function DeveloperAnalyticsCard() {
    const [activeChart, setActiveChart] = useState('tasks');
    const dashboardQuery = useAnalyticsDashboard();
    const loading = dashboardQuery.isLoading;
    const data = dashboardQuery.data
        ? {
            kpis: {
                totalTasks: dashboardQuery.data.kpis?.totalTasks ?? 0,
                totalHours: dashboardQuery.data.kpis?.totalHours ?? 0,
                avgTasksPerDev: dashboardQuery.data.kpis?.avgTasksPerDev ?? 0,
                avgHoursPerDev: dashboardQuery.data.kpis?.avgHoursPerDev ?? 0,
            },
            chartData: (dashboardQuery.data.chartData ?? [])
                .map((row) => ({
                DEVELOPER: row.DEVELOPER ?? row.developer ?? '',
                SPRINT_NAME: row.SPRINT_NAME ?? row.SPRINT ?? row.sprint ?? '',
                TASKS_COMPLETED: row.TASKS_COMPLETED ?? row.tasksCompleted ?? 0,
                REAL_HOURS: row.REAL_HOURS ?? row.TOTAL_HOURS_WORKED ?? row.totalHoursWorked ?? 0,
            }))
                .filter((row) => row.DEVELOPER),
        }
        : null;
    const devs = data ? aggregateByDeveloper(data.chartData) : [];
    const insights = data ? generateInsights(devs, data.kpis) : [];
    const actions = data ? generateActions(devs, data.kpis) : [];
    const barData = devs.map((d, i) => ({
        name: d.developer,
        Tasks: d.tasksCompleted,
        Hours: parseFloat(d.realHours.toFixed(1)),
        fill: DEV_COLORS[i % DEV_COLORS.length],
    }));
    const pieData = devs.map((d, i) => ({
        name: d.developer,
        value: d.tasksCompleted,
        fill: DEV_COLORS[i % DEV_COLORS.length],
    }));
    if (loading) {
        return (_jsx("div", { className: "space-y-4", children: [1, 2, 3, 4].map(i => (_jsx("div", { className: "h-24 animate-pulse rounded-2xl bg-zinc-100 dark:bg-zinc-800" }, i))) }));
    }
    if (devs.length === 0) {
        return (_jsxs("div", { className: "flex flex-col items-center justify-center rounded-2xl border border-dashed border-zinc-300 py-16 text-center dark:border-zinc-700", children: [_jsx("p", { className: "text-sm font-medium text-zinc-400", children: "No completed tasks yet" }), _jsx("p", { className: "mt-1 text-xs text-zinc-300 dark:text-zinc-600", children: "Complete tasks and log hours to see analytics" })] }));
    }
    return (_jsxs("div", { className: "space-y-5", children: [_jsxs("div", { className: "grid grid-cols-2 gap-3 sm:grid-cols-4", children: [_jsx(KpiCard, { label: "Total Tasks", value: String(data.kpis.totalTasks), sub: "completed across all sprints", color: "bg-indigo-500 text-white" }), _jsx(KpiCard, { label: "Total Hours", value: `${data.kpis.totalHours}h`, sub: "real hours logged", color: "bg-cyan-500 text-white" }), _jsx(KpiCard, { label: "Avg Tasks / Dev", value: String(data.kpis.avgTasksPerDev), sub: "team average", color: "bg-emerald-500 text-white" }), _jsx(KpiCard, { label: "Avg Hours / Dev", value: `${data.kpis.avgHoursPerDev}h`, sub: "team average", color: "bg-amber-500 text-white" })] }), _jsxs("div", { className: "rounded-2xl border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-900/60", children: [_jsxs("div", { className: "mb-4 flex items-center justify-between", children: [_jsx(SectionHeader, { children: "Performance by Developer" }), _jsxs("div", { className: "flex gap-1 rounded-lg border border-zinc-200 bg-zinc-50 p-0.5 dark:border-zinc-700 dark:bg-zinc-800", children: [_jsx("button", { onClick: () => setActiveChart('tasks'), className: `rounded-md px-3 py-1 text-xs font-medium transition-colors ${activeChart === 'tasks' ? 'bg-indigo-500 text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-700 dark:text-zinc-400'}`, children: "Tasks" }), _jsx("button", { onClick: () => setActiveChart('hours'), className: `rounded-md px-3 py-1 text-xs font-medium transition-colors ${activeChart === 'hours' ? 'bg-cyan-500 text-white shadow-sm' : 'text-zinc-500 hover:text-zinc-700 dark:text-zinc-400'}`, children: "Hours" })] })] }), _jsx(ResponsiveContainer, { width: "100%", height: 220, children: _jsxs(BarChart, { data: barData, barSize: 36, margin: { top: 4, right: 4, left: -20, bottom: 0 }, children: [_jsx(CartesianGrid, { strokeDasharray: "3 3", stroke: "#e4e4e7", vertical: false }), _jsx(XAxis, { dataKey: "name", tick: { fontSize: 11 }, axisLine: false, tickLine: false }), _jsx(YAxis, { tick: { fontSize: 11 }, axisLine: false, tickLine: false }), _jsx(Tooltip, { content: _jsx(CustomTooltip, {}), cursor: { fill: 'rgba(0,0,0,0.04)' } }), _jsx(Bar, { dataKey: activeChart === 'tasks' ? 'Tasks' : 'Hours', radius: [6, 6, 0, 0], children: barData.map((entry, i) => (_jsx(Cell, { fill: entry.fill }, i))) })] }) })] }), _jsxs("div", { className: "rounded-2xl border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-900/60", children: [_jsx(SectionHeader, { children: "Task Share by Developer" }), _jsx("div", { className: "flex flex-col items-center gap-4 sm:flex-row", children: _jsx(ResponsiveContainer, { width: "100%", height: 200, children: _jsxs(PieChart, { children: [_jsx(Pie, { data: pieData, cx: "50%", cy: "50%", innerRadius: 55, outerRadius: 85, paddingAngle: 3, dataKey: "value", children: pieData.map((entry, i) => (_jsx(Cell, { fill: entry.fill }, i))) }), _jsx(Tooltip, { content: _jsx(CustomTooltip, {}) }), _jsx(Legend, { iconType: "circle", iconSize: 8, formatter: (value) => _jsx("span", { className: "text-xs text-zinc-600 dark:text-zinc-300", children: value }) })] }) }) })] }), _jsxs("div", { className: "grid gap-4 sm:grid-cols-2", children: [_jsxs("div", { className: "rounded-2xl border border-amber-200 bg-amber-50 p-5 dark:border-amber-900/40 dark:bg-amber-950/20", children: [_jsxs("div", { className: "mb-3 flex items-center gap-2", children: [_jsx("span", { className: "flex h-6 w-6 items-center justify-center rounded-lg bg-amber-400 text-white", children: _jsx(Lightbulb, { className: "h-3.5 w-3.5" }) }), _jsx("h3", { className: "text-sm font-semibold text-amber-800 dark:text-amber-300", children: "Insights" })] }), _jsx("ul", { className: "space-y-2", children: insights.map((insight, i) => (_jsxs("li", { className: "flex gap-2 text-xs text-amber-700 dark:text-amber-400", children: [_jsx("span", { className: "mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-amber-400" }), insight] }, i))) })] }), _jsxs("div", { className: "rounded-2xl border border-emerald-200 bg-emerald-50 p-5 dark:border-emerald-900/40 dark:bg-emerald-950/20", children: [_jsxs("div", { className: "mb-3 flex items-center gap-2", children: [_jsx("span", { className: "flex h-6 w-6 items-center justify-center rounded-lg bg-emerald-500 text-white", children: _jsx(Zap, { className: "h-3.5 w-3.5" }) }), _jsx("h3", { className: "text-sm font-semibold text-emerald-800 dark:text-emerald-300", children: "Improvement Actions" })] }), _jsx("ul", { className: "space-y-2", children: actions.map((action, i) => (_jsxs("li", { className: "flex gap-2 text-xs text-emerald-700 dark:text-emerald-400", children: [_jsx("span", { className: "mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-500" }), action] }, i))) })] })] })] }));
}
