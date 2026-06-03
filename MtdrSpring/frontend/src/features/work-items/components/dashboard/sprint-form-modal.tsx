import React, { useEffect, useState } from 'react';
import { Loader2, X } from 'lucide-react';
import { useSprintCreate } from '@/hooks/api';
import type { SprintDto } from '../../viewModels/useWorkItemsViewModel';

interface SprintFormModalProps {
    isOpen: boolean;
    sprints: SprintDto[];
    onClose: () => void;
    onCreated?: () => void;
}

export function SprintFormModal({ isOpen, sprints, onClose, onCreated }: SprintFormModalProps) {
    const createSprintMutation = useSprintCreate();
    const [sprintNumber, setSprintNumber] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        if (!isOpen) return;
        setSprintNumber('');
        setError('');
    }, [isOpen]);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        const num = parseInt(sprintNumber.trim(), 10);
        if (!sprintNumber.trim() || isNaN(num) || num <= 0) {
            setError('Please enter a valid positive number.');
            return;
        }
        setError('');

        // Reuse teamId from existing sprints so we don't guess a wrong FK value
        const teamId = sprints.find(s => s.teamId)?.teamId ?? undefined;

        try {
            await createSprintMutation.mutateAsync({
                sprintId: `sprint-${num}`,
                teamId,
                name: `Sprint ${num}`,
                status: 'ACTIVE',
            });
            onCreated?.();
            onClose();
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

    if (!isOpen) return null;

    return (
        <div
            role="dialog"
            aria-modal="true"
            aria-label="Create sprint"
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
        >
            <div className="absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70" onClick={onClose} />

            <form
                onSubmit={handleSubmit}
                className="relative z-10 w-full max-w-sm rounded-2xl border border-zinc-200 bg-white shadow-xl dark:border-zinc-700 dark:bg-zinc-900"
            >
                <div className="flex items-center justify-between border-b border-zinc-100 px-5 py-4 dark:border-zinc-800">
                    <h3 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">New Sprint</h3>
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-lg p-1.5 text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-700 dark:text-zinc-500 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
                        aria-label="Close"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                <div className="grid gap-4 p-5">
                    {error && (
                        <p className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm text-rose-600 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-400">
                            {error}
                        </p>
                    )}

                    <div>
                        <label className="mb-1 block text-xs font-semibold uppercase tracking-wider text-zinc-500 dark:text-zinc-400">
                            Sprint Number *
                        </label>
                        <input
                            type="number"
                            min={1}
                            value={sprintNumber}
                            onChange={(e) => setSprintNumber(e.target.value)}
                            placeholder="e.g. 4"
                            className="w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                            autoFocus
                        />
                        {sprintNumber && !isNaN(parseInt(sprintNumber, 10)) && parseInt(sprintNumber, 10) > 0 && (
                            <p className="mt-1.5 text-xs text-zinc-400 dark:text-zinc-500">
                                Will create: <span className="font-medium text-zinc-600 dark:text-zinc-300">Sprint {sprintNumber}</span>
                            </p>
                        )}
                    </div>
                </div>

                <div className="flex items-center justify-end gap-3 border-t border-zinc-100 px-5 py-4 dark:border-zinc-800">
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-100 hover:text-zinc-900 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:hover:bg-zinc-700 dark:hover:text-zinc-100"
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        disabled={createSprintMutation.isPending}
                        className="inline-flex items-center gap-2 rounded-xl bg-emerald-500 px-5 py-2 text-sm font-semibold text-white transition-colors hover:bg-emerald-600 disabled:opacity-50 dark:hover:bg-emerald-400"
                    >
                        {createSprintMutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                        Create Sprint
                    </button>
                </div>
            </form>
        </div>
    );
}
