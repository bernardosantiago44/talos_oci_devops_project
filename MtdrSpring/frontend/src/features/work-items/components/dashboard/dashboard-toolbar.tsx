import React from 'react';
import Select, { MultiValue } from 'react-select';
import { Search, ListIcon, LayoutGrid, Plus, Tags, Layers } from 'lucide-react';
import type { WorkItemStatus } from '../../enums/work-item-status.enum';
import type { WorkItemType } from '../../enums/work-item-type.enum';
import type { SprintDto } from '../../viewModels/useWorkItemsViewModel';
import type { UserSummaryDto } from '@/shared/dtos/user-summary.dto';
import { WORK_ITEM_STATUSES } from '../../enums/work-item-status.enum';
import { WORK_ITEM_TYPES } from '../../enums/work-item-type.enum';
import { formatStatusLabel, formatTypeLabel } from '../../lib/dashboard-ui';

export type ViewMode = 'list' | 'kanban';

interface DashboardToolbarProps {
    search: string;
    onSearchChange: (v: string) => void;
    statusFilter: WorkItemStatus[];
    onStatusFilterChange: (v: WorkItemStatus[]) => void;
    assigneeFilter: string[];
    onAssigneeFilterChange: (v: string[]) => void;
    typeFilter: WorkItemType[];
    onTypeFilterChange: (v: WorkItemType[]) => void;
    sprintFilter: string[];
    onSprintFilterChange: (v: string[]) => void;
    viewMode: ViewMode;
    onViewModeChange: (v: ViewMode) => void;
    onManageTagsClick: () => void;
    onCreateClick: () => void;
    onAddSprintClick: () => void;
    users: UserSummaryDto[];
    sprints: SprintDto[];
}

type SelectOption<T extends string = string> = {
    value: T;
    label: string;
};

const statusOptions: SelectOption<WorkItemStatus>[] = WORK_ITEM_STATUSES.map((status) => ({
    value: status,
    label: formatStatusLabel(status),
}));

const typeOptions: SelectOption<WorkItemType>[] = WORK_ITEM_TYPES.map((type) => ({
    value: type,
    label: formatTypeLabel(type),
}));

const selectClassNames = {
    control: ({ isFocused }: { isFocused: boolean }) =>
        `min-h-[38px] rounded-xl border bg-white text-sm shadow-none dark:bg-zinc-800/60 ${
            isFocused
                ? 'border-sky-400/60 ring-1 ring-sky-400/30 dark:border-sky-500/60 dark:ring-sky-500/30'
                : 'border-zinc-200 dark:border-zinc-700/60'
        }`,
    valueContainer: () => 'gap-1 px-3 py-0.5',
    input: () => 'text-zinc-800 dark:text-zinc-200',
    placeholder: () => 'text-zinc-400 dark:text-zinc-500',
    multiValue: () => 'rounded-md bg-zinc-100 dark:bg-zinc-700',
    multiValueLabel: () => 'text-xs text-zinc-700 dark:text-zinc-200',
    multiValueRemove: () => 'text-zinc-500 hover:bg-zinc-200 hover:text-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-600 dark:hover:text-white',
    indicatorsContainer: () => 'text-zinc-400 dark:text-zinc-500',
    menu: () => 'z-30 overflow-hidden rounded-xl border border-zinc-200 bg-white shadow-lg dark:border-zinc-700 dark:bg-zinc-800',
    option: ({ isFocused, isSelected }: { isFocused: boolean; isSelected: boolean }) =>
        `px-3 py-2 text-sm ${
            isSelected
                ? 'bg-sky-500 text-white'
                : isFocused
                    ? 'bg-zinc-100 text-zinc-900 dark:bg-zinc-700 dark:text-zinc-100'
                    : 'bg-white text-zinc-700 dark:bg-zinc-800 dark:text-zinc-200'
        }`,
};

export function DashboardToolbar({
    search,
    onSearchChange,
    statusFilter,
    onStatusFilterChange,
    assigneeFilter,
    onAssigneeFilterChange,
    typeFilter,
    onTypeFilterChange,
    sprintFilter,
    onSprintFilterChange,
    viewMode,
    onViewModeChange,
    onManageTagsClick,
    onCreateClick,
    onAddSprintClick,
    users,
    sprints,
}: DashboardToolbarProps) {
    const assigneeOptions = users.map((user) => ({
        value: user.userId,
        label: user.name,
    }));
    const sprintOptions = sprints.map((sprint) => ({
        value: sprint.sprintId,
        label: sprint.name,
    }));
    const selectedStatuses = statusOptions.filter((option) => statusFilter.includes(option.value));
    const selectedAssignees = assigneeOptions.filter((option) => assigneeFilter.includes(option.value));
    const selectedTypes = typeOptions.filter((option) => typeFilter.includes(option.value));
    const selectedSprints = sprintOptions.filter((option) => sprintFilter.includes(option.value));

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

            <div className="min-w-[180px]">
                <Select<SelectOption<WorkItemStatus>, true>
                    isMulti
                    options={statusOptions}
                    value={selectedStatuses}
                    onChange={(options: MultiValue<SelectOption<WorkItemStatus>>) =>
                        onStatusFilterChange(options.map((option) => option.value))
                    }
                    placeholder="All statuses"
                    classNames={selectClassNames}
                    unstyled
                />
            </div>

            <div className="min-w-[200px]">
                <Select<SelectOption<WorkItemType>, true>
                    isMulti
                    options={typeOptions}
                    value={selectedTypes}
                    onChange={(options: MultiValue<SelectOption<WorkItemType>>) =>
                        onTypeFilterChange(options.map((option) => option.value))
                    }
                    placeholder="All types"
                    classNames={selectClassNames}
                    unstyled
                />
            </div>

            <div className="min-w-[200px]">
                <Select<SelectOption, true>
                    isMulti
                    options={sprintOptions}
                    value={selectedSprints}
                    onChange={(options: MultiValue<SelectOption>) =>
                        onSprintFilterChange(options.map((option) => option.value))
                    }
                    placeholder="All sprints"
                    classNames={selectClassNames}
                    unstyled
                />
            </div>

            <div className="min-w-[200px]">
                <Select<SelectOption, true>
                    isMulti
                    options={assigneeOptions}
                    value={selectedAssignees}
                    onChange={(options: MultiValue<SelectOption>) =>
                        onAssigneeFilterChange(options.map((option) => option.value))
                    }
                    placeholder="All assignees"
                    classNames={selectClassNames}
                    unstyled
                />
            </div>

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

            <div className="ml-auto flex items-center gap-2">
                {/* Tags button */}
                <button
                    type="button"
                    onClick={onManageTagsClick}
                    className="flex items-center gap-2 rounded-xl border border-zinc-200 bg-white px-4 py-2 text-sm font-semibold text-zinc-700 transition-colors hover:bg-zinc-100 hover:text-zinc-900 focus:outline-none focus:ring-2 focus:ring-sky-500/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:hover:bg-zinc-700 dark:hover:text-zinc-100"
                >
                    <Tags className="h-4 w-4" />
                    Tags
                </button>

                {/* Sprints button */}
                <button
                    type="button"
                    onClick={onAddSprintClick}
                    className="flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm font-semibold text-emerald-700 transition-colors hover:bg-emerald-100 hover:text-emerald-900 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 dark:border-emerald-700/50 dark:bg-emerald-500/10 dark:text-emerald-300 dark:hover:bg-emerald-500/20 dark:hover:text-emerald-100"
                >
                    <Layers className="h-4 w-4" />
                    Sprints
                </button>

                {/* Create button */}
                <button
                    type="button"
                    onClick={onCreateClick}
                    className="flex items-center gap-2 rounded-xl bg-sky-500 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/50 dark:hover:bg-sky-400"
                >
                    <Plus className="h-4 w-4" />
                    New Task
                </button>
            </div>
        </div>
    );
}
