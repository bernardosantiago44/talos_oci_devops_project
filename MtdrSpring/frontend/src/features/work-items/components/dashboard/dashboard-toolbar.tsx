import React from 'react';
import { Search, ListIcon, LayoutGrid, Plus } from 'lucide-react';
import type { WorkItemStatus } from '../../enums/work-item-status.enum';
import type { UserSummaryDto } from '@/shared/dtos/user-summary.dto';
import { WORK_ITEM_STATUSES } from '../../enums/work-item-status.enum';
import { formatStatusLabel } from '../../lib/dashboard-ui';

export type ViewMode = 'list' | 'kanban';

interface DashboardToolbarProps {
    search: string;
    onSearchChange: (v: string) => void;
    statusFilter: WorkItemStatus | '';
    onStatusFilterChange: (v: WorkItemStatus | '') => void;
    assigneeFilter: string;
    onAssigneeFilterChange: (v: string) => void;
    viewMode: ViewMode;
    onViewModeChange: (v: ViewMode) => void;
    onCreateClick: () => void;
    users: UserSummaryDto[];
}

export function DashboardToolbar({
    search,
    onSearchChange,
    statusFilter,
    onStatusFilterChange,
    assigneeFilter,
    onAssigneeFilterChange,
    viewMode,
    onViewModeChange,
    onCreateClick,
    users,
}: DashboardToolbarProps) {
    return (
        <div className="flex flex-wrap items-center gap-3">
            {/* Search */}
            <div className="relative min-w-[200px] flex-1">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-zinc-400 dark:text-zinc-500" />
                <input
                    type="text"
                    placeholder="Search tasks…"
                    value={search}
                    onChange={(e) => onSearchChange(e.target.value)}
                    className="w-full rounded-xl border border-zinc-200 bg-white py-2 pl-9 pr-4 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-500 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                />
            </div>

            {/* Status filter */}
            <select
                value={statusFilter}
                onChange={(e) => onStatusFilterChange(e.target.value as WorkItemStatus | '')}
                className="rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-700 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
            >
                <option value="">All statuses</option>
                {WORK_ITEM_STATUSES.map((s) => (
                    <option key={s} value={s}>{formatStatusLabel(s)}</option>
                ))}
            </select>

            {/* Assignee filter */}
            <select
                value={assigneeFilter}
                onChange={(e) => onAssigneeFilterChange(e.target.value)}
                className="rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-700 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
            >
                <option value="">All assignees</option>
                {users.map((u) => (
                    <option key={u.id} value={u.id}>{u.name}</option>
                ))}
            </select>

            {/* View toggle */}
            <div className="flex items-center rounded-xl border border-zinc-200 bg-zinc-100 p-1 dark:border-zinc-700/60 dark:bg-zinc-800/60">
                <button
                    type="button"
                    onClick={() => onViewModeChange('list')}
                    className={`flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium transition-colors ${
                        viewMode === 'list'
                            ? 'bg-white text-zinc-800 shadow-sm dark:bg-zinc-700 dark:text-white dark:shadow-none'
                            : 'text-zinc-500 hover:text-zinc-700 dark:text-zinc-400 dark:hover:text-zinc-200'
                    }`}
                    title="List view"
                >
                    <ListIcon className="h-4 w-4" />
                    <span>List</span>
                </button>
                <button
                    type="button"
                    onClick={() => onViewModeChange('kanban')}
                    className={`flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium transition-colors ${
                        viewMode === 'kanban'
                            ? 'bg-white text-zinc-800 shadow-sm dark:bg-zinc-700 dark:text-white dark:shadow-none'
                            : 'text-zinc-500 hover:text-zinc-700 dark:text-zinc-400 dark:hover:text-zinc-200'
                    }`}
                    title="Kanban view"
                >
                    <LayoutGrid className="h-4 w-4" />
                    <span>Kanban</span>
                </button>
            </div>

            {/* Create button */}
            <button
                type="button"
                onClick={onCreateClick}
                className="ml-auto flex items-center gap-2 rounded-xl bg-sky-500 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/50 dark:hover:bg-sky-400"
            >
                <Plus className="h-4 w-4" />
                New Task
            </button>
        </div>
    );
}
