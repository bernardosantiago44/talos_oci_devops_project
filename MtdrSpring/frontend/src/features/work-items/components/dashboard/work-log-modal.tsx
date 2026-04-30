import React, { useEffect, useMemo, useState } from 'react';
import { Clock, X } from 'lucide-react';
import type { UserSummaryDto } from '@/shared/dtos/user-summary.dto';
import type { WorkItemDetailDto } from '../../dtos/work-item-detail.dto';
import type { WorkLogDto, WorkLogMode } from '../../dtos/work-log.dto';

interface WorkLogModalProps {
    isOpen: boolean;
    item: WorkItemDetailDto | null;
    mode: WorkLogMode;
    users: UserSummaryDto[];
    onClose: () => void;
    onSubmit: (dto: WorkLogDto) => Promise<void>;
}

function Label({ children }: { children: React.ReactNode }) {
    return <label className="mb-1 block text-xs font-semibold uppercase tracking-wider text-zinc-500 dark:text-zinc-400">{children}</label>;
}

function parseWholeNumber(value: string) {
    if (!value.trim()) return 0;
    const parsed = Number(value);
    if (!Number.isFinite(parsed) || parsed < 0) return NaN;
    return Math.floor(parsed);
}

export function WorkLogModal({
    isOpen,
    item,
    mode,
    users,
    onClose,
    onSubmit,
}: WorkLogModalProps) {
    const [hours, setHours] = useState('');
    const [minutes, setMinutes] = useState('');
    const [userId, setUserId] = useState('');
    const [note, setNote] = useState('');
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');

    const userOptions = useMemo(() => {
        const byId = new Map(users.map((user) => [user.userId, user]));
        item?.assignees.forEach((assignee) => {
            if (!byId.has(assignee.user.userId)) {
                byId.set(assignee.user.userId, assignee.user);
            }
        });
        return Array.from(byId.values());
    }, [item, users]);

    useEffect(() => {
        if (!isOpen || !item) return;
        setHours('');
        setMinutes('');
        setNote(mode === 'complete' ? 'Logged while marking task done.' : '');
        setUserId(item.assignees[0]?.user.userId ?? users[0]?.userId ?? '');
        setError('');
        setSaving(false);
    }, [isOpen, item, mode, users]);

    if (!isOpen || !item) return null;

    const currentItem = item;
    const title = mode === 'complete' ? 'Mark Task Done' : 'Register Minutes';
    const submitLabel = mode === 'complete' ? 'Log and Mark Done' : 'Register Minutes';

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();

        const parsedHours = parseWholeNumber(hours);
        const parsedMinutes = parseWholeNumber(minutes);

        if (Number.isNaN(parsedHours) || Number.isNaN(parsedMinutes) || parsedMinutes > 59) {
            setError('Enter a valid hour and minute total.');
            return;
        }

        const totalMinutes = parsedHours * 60 + parsedMinutes;
        if (totalMinutes < 1) {
            setError('Worked time must be at least one minute.');
            return;
        }

        if (!userId) {
            setError('Select the user who worked on this task.');
            return;
        }

        setSaving(true);
        setError('');
        try {
            await onSubmit({
                workItemId: currentItem.id,
                userId,
                minutes: totalMinutes,
                note: note.trim() || undefined,
            });
            onClose();
        } catch {
            setError('Something went wrong. Please try again.');
        } finally {
            setSaving(false);
        }
    }

    return (
        <div
            role="dialog"
            aria-modal="true"
            aria-label={title}
            className="fixed inset-0 z-[60] flex items-center justify-center p-4"
        >
            <div
                className="absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70"
                onClick={saving ? undefined : onClose}
            />

            <div className="relative z-10 w-full max-w-md rounded-2xl border border-zinc-200 bg-white shadow-xl dark:border-zinc-700 dark:bg-zinc-900 dark:shadow-2xl">
                <div className="flex items-start justify-between gap-4 border-b border-zinc-100 px-6 py-4 dark:border-zinc-800">
                    <div className="flex min-w-0 items-start gap-3">
                        <div className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-cyan-500/15 text-cyan-600 dark:text-cyan-400">
                            <Clock className="h-5 w-5" />
                        </div>
                        <div className="min-w-0">
                            <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">{title}</h2>
                            <p className="mt-0.5 truncate text-sm text-zinc-500 dark:text-zinc-400">{item.title}</p>
                        </div>
                    </div>
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={saving}
                        className="shrink-0 rounded-lg p-1.5 text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-700 disabled:cursor-not-allowed disabled:opacity-60 dark:text-zinc-500 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
                        aria-label="Close"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6">
                    {error && (
                        <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm text-rose-600 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-400">
                            {error}
                        </p>
                    )}

                    <div className="grid gap-4">
                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <Label>Hours</Label>
                                <input
                                    type="number"
                                    min="0"
                                    step="1"
                                    value={hours}
                                    onChange={(e) => setHours(e.target.value)}
                                    className="w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                                    placeholder="0"
                                />
                            </div>
                            <div>
                                <Label>Minutes</Label>
                                <input
                                    type="number"
                                    min="0"
                                    max="59"
                                    step="1"
                                    value={minutes}
                                    onChange={(e) => setMinutes(e.target.value)}
                                    className="w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                                    placeholder="30"
                                />
                            </div>
                        </div>

                        <div>
                            <Label>User</Label>
                            <select
                                value={userId}
                                onChange={(e) => setUserId(e.target.value)}
                                className="w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                            >
                                <option value="">Select user</option>
                                {userOptions.map((user) => (
                                    <option key={user.userId} value={user.userId}>
                                        {user.name}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <Label>Note</Label>
                            <textarea
                                value={note}
                                onChange={(e) => setNote(e.target.value)}
                                rows={3}
                                className="w-full resize-none rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                                placeholder="Optional work note"
                            />
                        </div>
                    </div>

                    <div className="mt-6 flex items-center justify-end gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={saving}
                            className="rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-100 hover:text-zinc-900 disabled:cursor-not-allowed disabled:opacity-60 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:hover:bg-zinc-700 dark:hover:text-zinc-100"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={saving}
                            className="rounded-xl bg-cyan-500 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-cyan-600 disabled:cursor-not-allowed disabled:opacity-70 dark:hover:bg-cyan-400"
                        >
                            {saving ? 'Saving...' : submitLabel}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
