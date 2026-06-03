import React, { useEffect, useMemo, useState } from 'react';
import { Layers, Loader2, Plus, PowerOff, Zap, Trash2, X } from 'lucide-react';
import { useSprintCreate, useSprintDelete, useSprintUpdateStatus } from '@/hooks/api';
import type { SprintDto } from '../../viewModels/useWorkItemsViewModel';
import type { UserSummaryDto } from '@/shared/dtos/user-summary.dto';

type Mode = 'list' | 'create';

interface SprintManagerModalProps {
    isOpen: boolean;
    sprints: SprintDto[];
    users: UserSummaryDto[];
    onClose: () => void;
}

const STATUS_LABELS: Record<string, string> = {
    ACTIVE: 'Active',
    INACTIVE: 'Inactive',
    COMPLETED: 'Completed',
};

const STATUS_COLORS: Record<string, string> = {
    ACTIVE: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300',
    INACTIVE: 'bg-zinc-100 text-zinc-600 dark:bg-zinc-700/50 dark:text-zinc-400',
    COMPLETED: 'bg-sky-50 text-sky-700 dark:bg-sky-500/15 dark:text-sky-300',
};

export function SprintManagerModal({ isOpen, sprints, users, onClose }: SprintManagerModalProps) {
    const createSprintMutation = useSprintCreate();
    const updateStatusMutation = useSprintUpdateStatus();
    const deleteSprintMutation = useSprintDelete();

    const [mode, setMode] = useState<Mode>('list');
    const [sprintNumber, setSprintNumber] = useState('');
    const [error, setError] = useState('');

    const deletingId = useMemo(() => {
        const variables = deleteSprintMutation.variables;
        return deleteSprintMutation.isPending && typeof variables === 'string' ? variables : null;
    }, [deleteSprintMutation.isPending, deleteSprintMutation.variables]);

    useEffect(() => {
        if (!isOpen) return;
        setMode('list');
        setSprintNumber('');
        setError('');
    }, [isOpen]);

    function openCreate() {
        setMode('create');
        setSprintNumber('');
        setError('');
    }

    function backToList() {
        setMode('list');
        setSprintNumber('');
        setError('');
    }

    async function handleCreate(e: React.FormEvent) {
        e.preventDefault();
        const num = parseInt(sprintNumber.trim(), 10);
        if (!sprintNumber.trim() || isNaN(num) || num <= 0) {
            setError('Please enter a valid positive number.');
            return;
        }
        setError('');
        const teamId = sprints.find(s => s.teamId)?.teamId ?? undefined;
        const createdByUserId = users[0]?.userId;
        const today = new Date().toISOString().slice(0, 10);
        const endDate = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);
        try {
            await createSprintMutation.mutateAsync({
                sprintId: `sprint-${num}`,
                teamId,
                name: `Sprint ${num}`,
                status: 'ACTIVE',
                startDate: today,
                endDate,
                createdByUserId,
            });
            backToList();
        } catch (err: unknown) {
            let message = 'Could not create sprint. Please try again.';
            if (err && typeof err === 'object') {
                const body = (err as Record<string, unknown>).body ?? err;
                if (body && typeof body === 'object') {
                    const m = (body as Record<string, unknown>).message;
                    if (typeof m === 'string' && m) message = m;
                } else if (err instanceof Error && err.message) {
                    message = err.message;
                }
            }
            setError(message);
        }
    }

    async function handleToggleStatus(sprint: SprintDto) {
        const next = sprint.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
        setError('');
        try {
            await updateStatusMutation.mutateAsync({ id: sprint.sprintId, status: next });
        } catch {
            setError('Could not update sprint status. Please try again.');
        }
    }

    async function handleDelete(sprint: SprintDto) {
        const confirmed = window.confirm(`Delete "${sprint.name}"? Work items in this sprint will lose their sprint assignment.`);
        if (!confirmed) return;
        setError('');
        try {
            await deleteSprintMutation.mutateAsync(sprint.sprintId);
        } catch (err: unknown) {
            let message = 'Could not delete sprint. Please try again.';
            if (err && typeof err === 'object') {
                const body = (err as Record<string, unknown>).body ?? err;
                if (body && typeof body === 'object') {
                    const m = (body as Record<string, unknown>).message;
                    if (typeof m === 'string' && m) message = m;
                }
            }
            setError(message);
        }
    }

    if (!isOpen) return null;

    const title = mode === 'create' ? 'New Sprint' : 'Manage Sprints';

    return (
        <div
            role="dialog"
            aria-modal="true"
            aria-label={title}
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
        >
            <div className="absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70" onClick={onClose} />

            <div className="relative z-10 flex max-h-[88vh] w-full max-w-lg flex-col rounded-2xl border border-zinc-200 bg-white shadow-xl dark:border-zinc-700 dark:bg-zinc-900 dark:shadow-2xl">
                {/* Header */}
                <div className="flex items-center justify-between border-b border-zinc-100 px-6 py-5 dark:border-zinc-800">
                    <h2 className="text-xl font-bold text-zinc-900 dark:text-zinc-100">{title}</h2>
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-lg p-1.5 text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-700 dark:text-zinc-500 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
                        aria-label="Close"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                {/* Body */}
                <div className="min-h-0 flex-1 overflow-y-auto p-6">
                    {error && (
                        <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm text-rose-600 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-400">
                            {error}
                        </p>
                    )}

                    {mode === 'list' ? (
                        <div className="space-y-3">
                            {sprints.length === 0 && (
                                <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-zinc-200 px-6 py-12 text-center dark:border-zinc-700">
                                    <Layers className="h-8 w-8 text-zinc-300 dark:text-zinc-600" />
                                    <p className="mt-3 text-sm font-medium text-zinc-600 dark:text-zinc-300">No sprints yet</p>
                                    <p className="mt-1 text-xs text-zinc-400 dark:text-zinc-500">Create one to start adding work items.</p>
                                </div>
                            )}

                            {sprints.map((sprint) => {
                                const statusLabel = STATUS_LABELS[sprint.status ?? ''] ?? sprint.status ?? '—';
                                const statusColor = STATUS_COLORS[sprint.status ?? ''] ?? STATUS_COLORS.INACTIVE;
                                return (
                                    <div
                                        key={sprint.sprintId}
                                        className="flex items-center gap-4 rounded-xl border border-zinc-200 bg-white px-4 py-3.5 dark:border-zinc-800 dark:bg-zinc-900/60"
                                    >
                                        <div className="min-w-0 flex-1">
                                            <p className="truncate text-sm font-semibold text-zinc-800 dark:text-zinc-100">{sprint.name}</p>
                                            <p className="mt-0.5 text-xs text-zinc-400 dark:text-zinc-500">{sprint.sprintId}</p>
                                        </div>
                                        <span className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold ${statusColor}`}>
                                            {statusLabel}
                                        </span>
                                        <button
                                            type="button"
                                            onClick={() => handleToggleStatus(sprint)}
                                            disabled={updateStatusMutation.isPending && updateStatusMutation.variables?.id === sprint.sprintId}
                                            title={sprint.status === 'ACTIVE' ? 'Deactivate sprint' : 'Activate sprint'}
                                            className={`rounded-lg p-2 transition-colors disabled:opacity-50 ${
                                                sprint.status === 'ACTIVE'
                                                    ? 'text-amber-500 hover:bg-amber-50 hover:text-amber-700 dark:text-amber-400 dark:hover:bg-amber-500/10'
                                                    : 'text-emerald-500 hover:bg-emerald-50 hover:text-emerald-700 dark:text-emerald-400 dark:hover:bg-emerald-500/10'
                                            }`}
                                        >
                                            {updateStatusMutation.isPending && updateStatusMutation.variables?.id === sprint.sprintId
                                                ? <Loader2 className="h-4 w-4 animate-spin" />
                                                : sprint.status === 'ACTIVE'
                                                    ? <PowerOff className="h-4 w-4" />
                                                    : <Zap className="h-4 w-4" />
                                            }
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => handleDelete(sprint)}
                                            disabled={deletingId === sprint.sprintId}
                                            className="rounded-lg p-2 text-red-500 transition-colors hover:bg-red-50 hover:text-red-700 disabled:opacity-50 dark:text-red-400 dark:hover:bg-red-500/10 dark:hover:text-red-300"
                                            aria-label={`Delete ${sprint.name}`}
                                            title="Delete sprint"
                                        >
                                            {deletingId === sprint.sprintId
                                                ? <Loader2 className="h-4 w-4 animate-spin" />
                                                : <Trash2 className="h-4 w-4" />
                                            }
                                        </button>
                                    </div>
                                );
                            })}
                        </div>
                    ) : (
                        <form id="sprint-create-form" onSubmit={handleCreate} className="grid gap-4">
                            <div>
                                <label className="mb-1.5 block text-sm font-medium text-zinc-600 dark:text-zinc-300">
                                    Sprint Number *
                                </label>
                                <input
                                    type="number"
                                    min={1}
                                    value={sprintNumber}
                                    onChange={(e) => setSprintNumber(e.target.value)}
                                    placeholder="e.g. 4"
                                    className="w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2.5 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                                    autoFocus
                                />
                                {sprintNumber && !isNaN(parseInt(sprintNumber, 10)) && parseInt(sprintNumber, 10) > 0 && (
                                    <p className="mt-1.5 text-xs text-zinc-400 dark:text-zinc-500">
                                        Will create: <span className="font-medium text-zinc-600 dark:text-zinc-300">Sprint {sprintNumber}</span>
                                    </p>
                                )}
                            </div>
                        </form>
                    )}
                </div>

                {/* Footer */}
                <div className="flex items-center justify-between border-t border-zinc-100 px-6 py-4 dark:border-zinc-800">
                    {mode === 'list' ? (
                        <>
                            <button
                                type="button"
                                onClick={openCreate}
                                className="inline-flex items-center gap-2 rounded-xl bg-emerald-500 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-emerald-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 dark:hover:bg-emerald-400"
                            >
                                <Plus className="h-4 w-4" />
                                New Sprint
                            </button>
                            <p className="text-sm font-semibold text-zinc-500 dark:text-zinc-400">
                                {sprints.length} sprint{sprints.length === 1 ? '' : 's'}
                            </p>
                        </>
                    ) : (
                        <div className="ml-auto flex items-center gap-3">
                            <button
                                type="button"
                                onClick={backToList}
                                className="rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-100 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:hover:bg-zinc-700"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                form="sprint-create-form"
                                disabled={createSprintMutation.isPending}
                                className="inline-flex items-center gap-2 rounded-xl bg-emerald-500 px-5 py-2 text-sm font-semibold text-white transition-colors hover:bg-emerald-600 disabled:opacity-50 dark:hover:bg-emerald-400"
                            >
                                {createSprintMutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                                Create Sprint
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
