import { useState } from 'react';
import { TrendingUp, ChevronDown, ChevronUp } from 'lucide-react';
import { useAnalyticsVelocity } from '@/hooks/api';

interface SprintVelocity {
    sprintId: string;
    sprintName: string;
    sprintStatus: string;
    startDate?: string;
    endDate?: string;
    totalTasks: number;
    completedTasks: number;
    fulfillmentPct: number;
}

interface VelocityData {
    target: number;
    overallPct: number;
    sprints: SprintVelocity[];
}

function SprintRow({ sprint }: { sprint: SprintVelocity }) {
    const pct = sprint.fulfillmentPct;
    const met = pct >= 84;

    return (
        <div className="flex items-center gap-3">
            <span className="w-24 shrink-0 truncate text-xs text-zinc-500 dark:text-zinc-400" title={sprint.sprintName}>
                {sprint.sprintName}
            </span>
            <div className="relative h-1.5 flex-1 overflow-hidden rounded-full bg-zinc-200 dark:bg-zinc-700">
                <div
                    className={`h-full rounded-full transition-all ${met ? 'bg-emerald-500' : 'bg-sky-500'}`}
                    style={{ width: `${Math.min(pct, 100)}%` }}
                />
                {/* Target marker at 84% */}
                <div
                    className="absolute top-0 h-full w-px bg-amber-400/80"
                    style={{ left: '84%' }}
                />
            </div>
            <span className={`w-12 shrink-0 text-right text-xs font-semibold tabular-nums ${met ? 'text-emerald-600 dark:text-emerald-400' : 'text-zinc-600 dark:text-zinc-300'}`}>
                {pct.toFixed(1)}%
            </span>
        </div>
    );
}

export function VelocityFulfillmentCard() {
    const [expanded, setExpanded] = useState(false);
    const velocityQuery = useAnalyticsVelocity();
    const loading = velocityQuery.isLoading;

    const data: VelocityData | null = velocityQuery.data
        ? {
            target: velocityQuery.data.target ?? 84,
            overallPct: velocityQuery.data.overallPct ?? 0,
            sprints: (velocityQuery.data.sprints ?? []).map((sprint) => ({
                sprintId: sprint.sprintId ?? sprint.sprintName ?? 'unknown-sprint',
                sprintName: sprint.sprintName ?? sprint.sprintId ?? 'Sprint',
                sprintStatus: sprint.sprintStatus ?? '',
                startDate: sprint.startDate,
                endDate: sprint.endDate,
                totalTasks: sprint.totalTasks ?? 0,
                completedTasks: sprint.completedTasks ?? 0,
                fulfillmentPct: sprint.fulfillmentPct ?? 0,
            })),
        }
        : null;

    const pct = data?.overallPct ?? 0;
    const target = data?.target ?? 84;
    const metTarget = pct >= target;
    const sprintsWithTasks = data?.sprints.filter((s) => s.totalTasks > 0) ?? [];

    return (
        <div className="rounded-2xl border border-zinc-200 bg-white p-4 dark:border-zinc-800 dark:bg-zinc-900/60">
            {/* Header */}
            <div className="flex items-center justify-between gap-3">
                <div className="flex items-center gap-2.5">
                    <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-sky-500/10 text-sky-600 dark:bg-sky-500/15 dark:text-sky-400">
                        <TrendingUp className="h-4 w-4" />
                    </div>
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-wider text-zinc-400 dark:text-zinc-500">
                            KPI · Velocity Fulfillment
                        </p>
                        <p className="text-[11px] text-zinc-400 dark:text-zinc-600">
                            Completed vs planned tasks · Target: {target}%
                        </p>
                    </div>
                </div>

                {/* Big number */}
                {loading ? (
                    <div className="h-8 w-16 animate-pulse rounded-lg bg-zinc-100 dark:bg-zinc-800" />
                ) : (
                    <span className={`text-3xl font-bold tabular-nums ${metTarget ? 'text-emerald-600 dark:text-emerald-400' : 'text-zinc-700 dark:text-zinc-200'}`}>
                        {pct.toFixed(1)}%
                    </span>
                )}
            </div>

            {/* Overall progress bar */}
            {!loading && (
                <div className="mt-3">
                    <div className="relative h-2 overflow-hidden rounded-full bg-zinc-100 dark:bg-zinc-800">
                        <div
                            className={`h-full rounded-full transition-all duration-700 ${metTarget ? 'bg-emerald-500' : 'bg-sky-500'}`}
                            style={{ width: `${Math.min(pct, 100)}%` }}
                        />
                        {/* Target marker */}
                        <div
                            className="absolute top-0 h-full w-0.5 bg-amber-400"
                            style={{ left: `${target}%` }}
                            title={`Target: ${target}%`}
                        />
                    </div>
                    <div className="mt-1 flex items-center justify-between text-[11px] text-zinc-400 dark:text-zinc-600">
                        <span>0%</span>
                        <span className="text-amber-500 dark:text-amber-400">▲ Target {target}%</span>
                        <span>100%</span>
                    </div>
                </div>
            )}

            {/* Per-sprint breakdown toggle */}
            {!loading && sprintsWithTasks.length > 0 && (
                <div className="mt-3">
                    <button
                        type="button"
                        onClick={() => setExpanded((v) => !v)}
                        className="flex w-full items-center gap-1 text-left text-[11px] font-medium text-zinc-400 hover:text-zinc-600 dark:text-zinc-500 dark:hover:text-zinc-300"
                    >
                        {expanded ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />}
                        {expanded ? 'Hide' : 'View'} by sprint ({sprintsWithTasks.length})
                    </button>

                    {expanded && (
                        <div className="mt-2 flex flex-col gap-2 border-t border-zinc-100 pt-2 dark:border-zinc-800">
                            {sprintsWithTasks.map((sprint) => (
                                <SprintRow key={sprint.sprintId} sprint={sprint} />
                            ))}
                        </div>
                    )}
                </div>
            )}

            {!loading && sprintsWithTasks.length === 0 && (
                <p className="mt-2 text-xs text-zinc-400 dark:text-zinc-600">
                    No sprint data available yet.
                </p>
            )}
        </div>
    );
}
