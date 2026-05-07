import { useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import {
    Bar,
    BarChart,
    CartesianGrid,
    Cell,
    Legend,
    Pie,
    PieChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import { Clock, Filter, ListChecks, Target } from 'lucide-react';
import { useAnalyticsDashboard } from '@/hooks/api';

type BackendRow = {
    DEVELOPER?: string;
    SPRINT?: string;
    SPRINT_NAME?: string;
    TASKS_COMPLETED?: number;
    REAL_HOURS?: number;
    TOTAL_HOURS_WORKED?: number;
    developer?: string;
    sprint?: string;
    tasksCompleted?: number;
    totalHoursWorked?: number;
};

type DeveloperSprintKpi = {
    developer: string;
    sprint: string;
    tasks: number;
    hours: number;
};

type ChartRow = {
    sprint: string;
    [developer: string]: string | number;
};

const DEV_COLORS = ['#2563eb', '#f97316', '#16a34a', '#dc2626', '#8b5cf6', '#0891b2', '#db2777', '#ca8a04'];

function compareNames(a: string, b: string) {
    return a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' });
}

function uniqueSorted(values: string[]) {
    return Array.from(new Set(values)).sort(compareNames);
}

function readNumber(value: unknown) {
    return typeof value === 'number' && Number.isFinite(value) ? value : 0;
}

function normalizeRows(rows: BackendRow[]): DeveloperSprintKpi[] {
    return rows
        .map((row) => ({
            developer: row.DEVELOPER ?? row.developer ?? '',
            sprint: row.SPRINT_NAME ?? row.SPRINT ?? row.sprint ?? '',
            tasks: readNumber(row.TASKS_COMPLETED ?? row.tasksCompleted),
            hours: readNumber(row.REAL_HOURS ?? row.TOTAL_HOURS_WORKED ?? row.totalHoursWorked),
        }))
        .filter((row) => row.developer && row.sprint);
}

function buildGroupedBars(rows: DeveloperSprintKpi[], sprints: string[], developers: string[], metric: 'tasks' | 'hours') {
    return sprints.map((sprint) => {
        const chartRow: ChartRow = { sprint };

        for (const developer of developers) {
            const row = rows.find((item) => item.sprint === sprint && item.developer === developer);
            chartRow[developer] = row ? Number(row[metric].toFixed(metric === 'hours' ? 1 : 0)) : 0;
        }

        return chartRow;
    });
}

function summarizeByDeveloper(rows: DeveloperSprintKpi[]) {
    const summary = new Map<string, DeveloperSprintKpi>();

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

function ChartTooltip({ active, payload, label }: any) {
    if (!active || !payload?.length) return null;

    return (
        <div className="rounded-xl border border-zinc-200 bg-white px-3 py-2 shadow-lg dark:border-zinc-700 dark:bg-zinc-800">
            <p className="mb-1 text-xs font-semibold text-zinc-700 dark:text-zinc-200">{label}</p>
            {payload.map((entry: any) => (
                <p key={entry.dataKey} className="text-xs" style={{ color: entry.color }}>
                    {entry.name}: <span className="font-semibold tabular-nums">{Number(entry.value).toLocaleString()}</span>
                </p>
            ))}
        </div>
    );
}

function KpiCard({
    icon,
    label,
    value,
    sub,
    accent,
}: {
    icon: ReactNode;
    label: string;
    value: string;
    sub: string;
    accent: string;
}) {
    return (
        <div className="rounded-xl border border-zinc-200 bg-white p-4 dark:border-zinc-800 dark:bg-zinc-900/60">
            <div className="flex items-center justify-between gap-3">
                <p className="text-[11px] font-semibold uppercase tracking-wider text-zinc-500 dark:text-zinc-400">{label}</p>
                <div className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${accent}`}>
                    {icon}
                </div>
            </div>
            <p className="mt-2 text-2xl font-bold tabular-nums text-zinc-900 dark:text-zinc-100">{value}</p>
            <p className="mt-0.5 text-xs text-zinc-500 dark:text-zinc-500">{sub}</p>
        </div>
    );
}

function SprintGroupedBarChart({
    title,
    subtitle,
    data,
    developers,
    allowDecimals,
}: {
    title: string;
    subtitle: string;
    data: ChartRow[];
    developers: string[];
    allowDecimals?: boolean;
}) {
    return (
        <div className="rounded-xl border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-900/60">
            <div className="mb-4">
                <h3 className="text-sm font-semibold text-zinc-800 dark:text-zinc-100">{title}</h3>
                <p className="mt-0.5 text-xs text-zinc-500 dark:text-zinc-500">{subtitle}</p>
            </div>

            <div className="h-80 min-w-0">
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data} margin={{ top: 8, right: 8, left: -18, bottom: 8 }} barGap={2}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#e4e4e7" vertical={false} />
                        <XAxis dataKey="sprint" tick={{ fontSize: 11 }} axisLine={false} tickLine={false} interval={0} />
                        <YAxis tick={{ fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={allowDecimals} />
                        <Tooltip content={<ChartTooltip />} cursor={{ fill: 'rgba(15, 23, 42, 0.04)' }} />
                        <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12, paddingTop: 12 }} />
                        {developers.map((developer, index) => (
                            <Bar
                                key={developer}
                                dataKey={developer}
                                name={developer}
                                fill={DEV_COLORS[index % DEV_COLORS.length]}
                                radius={[4, 4, 0, 0]}
                                maxBarSize={34}
                            />
                        ))}
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}

export function DeveloperAnalyticsCard() {
    const dashboardQuery = useAnalyticsDashboard();
    const [selectedSprint, setSelectedSprint] = useState('all');

    const rows = useMemo(
        () => normalizeRows((dashboardQuery.data?.chartData ?? []) as BackendRow[]),
        [dashboardQuery.data?.chartData],
    );

    const developers = useMemo(() => uniqueSorted(rows.map((row) => row.developer)), [rows]);
    const sprints = useMemo(() => uniqueSorted(rows.map((row) => row.sprint)), [rows]);

    const tasksBySprint = useMemo(
        () => buildGroupedBars(rows, sprints, developers, 'tasks'),
        [developers, rows, sprints],
    );
    const hoursBySprint = useMemo(
        () => buildGroupedBars(rows, sprints, developers, 'hours'),
        [developers, rows, sprints],
    );

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
        return (
            <div className="space-y-4">
                {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="h-28 animate-pulse rounded-xl bg-zinc-100 dark:bg-zinc-800" />
                ))}
            </div>
        );
    }

    if (rows.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-zinc-300 py-16 text-center dark:border-zinc-700">
                <p className="text-sm font-medium text-zinc-400">No completed tasks or logged hours yet</p>
                <p className="mt-1 text-xs text-zinc-300 dark:text-zinc-600">Complete tasks and register minutes to see sprint KPIs.</p>
            </div>
        );
    }

    return (
        <div className="space-y-5">
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                <KpiCard
                    icon={<ListChecks className="h-4 w-4" />}
                    label="Completed Tasks"
                    value={String(totalTasks)}
                    sub={`${sprintLabel} total`}
                    accent="bg-indigo-500/15 text-indigo-600 dark:text-indigo-400"
                />
                <KpiCard
                    icon={<Clock className="h-4 w-4" />}
                    label="Logged Hours"
                    value={`${totalHours.toFixed(1)}h`}
                    sub={`${sprintLabel} total`}
                    accent="bg-cyan-500/15 text-cyan-600 dark:text-cyan-400"
                />
                <KpiCard
                    icon={<Target className="h-4 w-4" />}
                    label="Hours / Task"
                    value={totalTasks > 0 ? `${(totalHours / totalTasks).toFixed(1)}h` : '0h'}
                    sub="logged effort per completed task"
                    accent="bg-emerald-500/15 text-emerald-600 dark:text-emerald-400"
                />
                <KpiCard
                    icon={<Filter className="h-4 w-4" />}
                    label="Avg Hours / Dev"
                    value={`${avgHoursPerDeveloper.toFixed(1)}h`}
                    sub={`${sprintLabel} average`}
                    accent="bg-amber-500/15 text-amber-600 dark:text-amber-400"
                />
            </div>

            <SprintGroupedBarChart
                title="Tasks Completed by Developer per Sprint"
                subtitle="Use one grouped chart to compare a developer across Sprints."
                data={tasksBySprint}
                developers={developers}
            />

            <SprintGroupedBarChart
                title="Logged Hours by Developer per Sprint"
                subtitle="This answers why a developer has only Y hours in Sprint Z: it shows the logged time-entry total by developer and sprint."
                data={hoursBySprint}
                developers={developers}
                allowDecimals
            />

            <div className="grid gap-5 xl:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]">
                <div className="rounded-xl border border-zinc-200 bg-white p-4 dark:border-zinc-800 dark:bg-zinc-900/60 xl:col-span-2">
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                            <p className="text-sm font-semibold text-zinc-800 dark:text-zinc-100">Sprint focus</p>
                            <p className="text-xs text-zinc-500 dark:text-zinc-500">Filters the hour share and developer detail cards below.</p>
                        </div>
                        <select
                            value={selectedSprint}
                            onChange={(event) => setSelectedSprint(event.target.value)}
                            className="w-full rounded-lg border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30 sm:w-56"
                        >
                            <option value="all">All sprints</option>
                            {sprints.map((sprint) => (
                                <option key={sprint} value={sprint}>
                                    {sprint}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                <div className="rounded-xl border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-900/60">
                    <h3 className="text-sm font-semibold text-zinc-800 dark:text-zinc-100">Logged Hours Share - {sprintLabel}</h3>
                    <p className="mt-0.5 text-xs text-zinc-500 dark:text-zinc-500">Shows which developers account for the selected sprint's registered hours.</p>
                    <div className="mt-4 h-72">
                        {hoursPieData.length === 0 ? (
                            <div className="flex h-full items-center justify-center text-sm text-zinc-400 dark:text-zinc-500">
                                No hours logged for this sprint.
                            </div>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={hoursPieData}
                                        cx="50%"
                                        cy="48%"
                                        innerRadius={58}
                                        outerRadius={88}
                                        paddingAngle={3}
                                        dataKey="value"
                                        nameKey="name"
                                    >
                                        {hoursPieData.map((entry) => (
                                            <Cell key={entry.name} fill={entry.fill} />
                                        ))}
                                    </Pie>
                                    <Tooltip content={<ChartTooltip />} />
                                    <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12 }} />
                                </PieChart>
                            </ResponsiveContainer>
                        )}
                    </div>
                </div>

                <div className="rounded-xl border border-zinc-200 bg-white p-5 dark:border-zinc-800 dark:bg-zinc-900/60">
                    <h3 className="text-sm font-semibold text-zinc-800 dark:text-zinc-100">Developer Detail - {sprintLabel}</h3>
                    <p className="mt-0.5 text-xs text-zinc-500 dark:text-zinc-500">Hours, completed tasks, and variance from the selected sprint average.</p>
                    <div className="mt-4 overflow-x-auto">
                        <table className="min-w-full text-left text-sm">
                            <thead>
                                <tr className="border-b border-zinc-200 text-xs uppercase tracking-wider text-zinc-400 dark:border-zinc-800">
                                    <th className="py-2 pr-4 font-semibold">Developer</th>
                                    <th className="py-2 pr-4 font-semibold">Tasks</th>
                                    <th className="py-2 pr-4 font-semibold">Hours</th>
                                    <th className="py-2 pr-4 font-semibold">Hours / Task</th>
                                    <th className="py-2 font-semibold">Vs Avg Hours</th>
                                </tr>
                            </thead>
                            <tbody>
                                {developerSummary.map((row, index) => {
                                    const color = DEV_COLORS[developers.indexOf(row.developer) % DEV_COLORS.length] ?? DEV_COLORS[index % DEV_COLORS.length];
                                    const hoursPerTask = row.tasks > 0 ? row.hours / row.tasks : 0;
                                    const hourGap = row.hours - avgHoursPerDeveloper;

                                    return (
                                        <tr key={row.developer} className="border-b border-zinc-100 last:border-0 dark:border-zinc-800/80">
                                            <td className="py-3 pr-4 font-medium text-zinc-800 dark:text-zinc-100">
                                                <span className="mr-2 inline-block h-2.5 w-2.5 rounded-full" style={{ backgroundColor: color }} />
                                                {row.developer}
                                            </td>
                                            <td className="py-3 pr-4 tabular-nums text-zinc-600 dark:text-zinc-300">{row.tasks}</td>
                                            <td className="py-3 pr-4 tabular-nums text-zinc-600 dark:text-zinc-300">{row.hours.toFixed(1)}h</td>
                                            <td className="py-3 pr-4 tabular-nums text-zinc-600 dark:text-zinc-300">
                                                {row.tasks > 0 ? `${hoursPerTask.toFixed(1)}h` : 'No completed tasks'}
                                            </td>
                                            <td className={`py-3 tabular-nums font-medium ${hourGap < 0 ? 'text-rose-500 dark:text-rose-400' : 'text-emerald-600 dark:text-emerald-400'}`}>
                                                {hourGap >= 0 ? '+' : ''}
                                                {hourGap.toFixed(1)}h
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}
