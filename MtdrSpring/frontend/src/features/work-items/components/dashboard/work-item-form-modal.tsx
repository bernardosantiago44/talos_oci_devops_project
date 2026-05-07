import React, { useEffect, useState } from 'react';
import { Loader2, Plus, X } from 'lucide-react';
import { useTagCreate } from '@/hooks/api';
import type { WorkItemDetailDto } from '../../dtos/work-item-detail.dto';
import type { CreateWorkItemDto } from '../../dtos/create-work-item.dto';
import type { UpdateWorkItemDto } from '../../dtos/update-work-item.dto';
import type { WorkItemType } from '../../enums/work-item-type.enum';
import type { WorkItemStatus } from '../../enums/work-item-status.enum';
import type { WorkItemPriority } from '../../enums/work-item-priority.enum';
import type { UserSummaryDto } from '@/shared/dtos/user-summary.dto';
import type { TagDto } from '@/shared/dtos/tag.dto';
import type { SprintDto } from '../../viewModels/useWorkItemsViewModel';
import { WORK_ITEM_TYPES } from '../../enums/work-item-type.enum';
import { WORK_ITEM_STATUSES } from '../../enums/work-item-status.enum';
import { WORK_ITEM_PRIORITIES } from '../../enums/work-item-priority.enum';
import {
    formatTypeLabel,
    formatStatusLabel,
    formatPriorityLabel,
} from '../../lib/dashboard-ui';

const HEX_COLOR_PATTERN = /^#[0-9A-Fa-f]{6}$/;
const DEFAULT_TAG_COLOR = '#3B82F6';
const TAG_DESCRIPTION_MAX_LENGTH = 300;

interface WorkItemFormModalProps {
    isOpen: boolean;
    item?: WorkItemDetailDto | null;
    users: UserSummaryDto[];
    sprints: SprintDto[];
    tags: TagDto[];
    onClose: () => void;
    onCreate: (dto: CreateWorkItemDto) => Promise<void>;
    onUpdate: (id: string, dto: UpdateWorkItemDto) => Promise<void>;
}

interface FormState {
    title: string;
    description: string;
    type: WorkItemType;
    status: WorkItemStatus;
    priority: WorkItemPriority;
    dueDate: string;
    estimatedMinutes: string;
    sprintId: string;
    assigneeUserIds: string[];
    tagIds: string[];
}

const DEFAULT_FORM: FormState = {
    title: '',
    description: '',
    type: 'TASK',
    status: 'TODO',
    priority: 'MEDIUM',
    dueDate: '',
    estimatedMinutes: '',
    sprintId: '',
    assigneeUserIds: [],
    tagIds: [],
};

function Label({ children }: { children: React.ReactNode }) {
    return <label className="mb-1 block text-xs font-semibold uppercase tracking-wider text-zinc-500 dark:text-zinc-400">{children}</label>;
}

function Input({ value, onChange, placeholder, type = 'text' }: {
    value: string;
    onChange: (v: string) => void;
    placeholder?: string;
    type?: string;
}) {
    return (
        <input
            type={type}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            placeholder={placeholder}
            className="w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
        />
    );
}

function Select({ value, onChange, children }: {
    value: string;
    onChange: (v: string) => void;
    children: React.ReactNode;
}) {
    return (
        <select
            value={value}
            onChange={(e) => onChange(e.target.value)}
            className="w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
        >
            {children}
        </select>
    );
}

function CreateTagModal({
    isOpen,
    onClose,
    onCreated,
}: {
    isOpen: boolean;
    onClose: () => void;
    onCreated: (tag: TagDto) => void;
}) {
    const createTagMutation = useTagCreate();
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [color, setColor] = useState(DEFAULT_TAG_COLOR);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!isOpen) return;
        setName('');
        setDescription('');
        setColor(DEFAULT_TAG_COLOR);
        setError('');
    }, [isOpen]);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        const trimmedName = name.trim();
        const trimmedDescription = description.trim();
        const normalizedColor = color.trim();

        if (!trimmedName) {
            setError('Tag name is required.');
            return;
        }
        if (trimmedDescription.length > TAG_DESCRIPTION_MAX_LENGTH) {
            setError(`Description must be ${TAG_DESCRIPTION_MAX_LENGTH} characters or fewer.`);
            return;
        }
        if (!HEX_COLOR_PATTERN.test(normalizedColor)) {
            setError('Color must be a valid HEX value, for example #3B82F6.');
            return;
        }

        setError('');
        try {
            const tag = await createTagMutation.mutateAsync({
                name: trimmedName,
                color: normalizedColor,
                description: trimmedDescription || undefined,
            });
            onCreated(tag);
            onClose();
        } catch {
            setError('Could not create tag. Please try again.');
        }
    }

    if (!isOpen) return null;

    return (
        <div
            role="dialog"
            aria-modal="true"
            aria-label="Create tag"
            className="fixed inset-0 z-[60] flex items-center justify-center p-4"
        >
            <div className="absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70" onClick={onClose} />

            <form
                onSubmit={handleSubmit}
                className="relative z-10 w-full max-w-md rounded-2xl border border-zinc-200 bg-white shadow-xl dark:border-zinc-700 dark:bg-zinc-900"
            >
                <div className="flex items-center justify-between border-b border-zinc-100 px-5 py-4 dark:border-zinc-800">
                    <h3 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">New Tag</h3>
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-lg p-1.5 text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-700 dark:text-zinc-500 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
                        aria-label="Close tag modal"
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
                        <Label>Name *</Label>
                        <Input value={name} onChange={setName} placeholder="e.g. Frontend" />
                    </div>

                    <div>
                        <Label>Description</Label>
                        <textarea
                            value={description}
                            onChange={(event) => setDescription(event.target.value)}
                            maxLength={TAG_DESCRIPTION_MAX_LENGTH}
                            rows={3}
                            placeholder="Describe when this tag should be used..."
                            className="w-full resize-none rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                        />
                        <p className="mt-1 text-right text-[11px] text-zinc-400 dark:text-zinc-500">
                            {description.length}/{TAG_DESCRIPTION_MAX_LENGTH}
                        </p>
                    </div>

                    <div>
                        <Label>Color *</Label>
                        <div className="flex gap-3">
                            <input
                                type="color"
                                value={HEX_COLOR_PATTERN.test(color) ? color : DEFAULT_TAG_COLOR}
                                onChange={(event) => setColor(event.target.value.toUpperCase())}
                                className="h-10 w-12 shrink-0 cursor-pointer rounded-xl border border-zinc-200 bg-zinc-50 p-1 dark:border-zinc-700/60 dark:bg-zinc-800/60"
                                aria-label="Pick tag color"
                            />
                            <Input value={color} onChange={(value) => setColor(value.toUpperCase())} placeholder="#3B82F6" />
                        </div>
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
                        disabled={createTagMutation.isPending}
                        className="inline-flex items-center gap-2 rounded-xl bg-sky-500 px-5 py-2 text-sm font-semibold text-white transition-colors hover:bg-sky-600 disabled:opacity-50 dark:hover:bg-sky-400"
                    >
                        {createTagMutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                        Create Tag
                    </button>
                </div>
            </form>
        </div>
    );
}

export function WorkItemFormModal({
    isOpen,
    item,
    users,
    sprints,
    tags,
    onClose,
    onCreate,
    onUpdate,
}: WorkItemFormModalProps) {
    const isEditing = !!item;
    const [form, setForm] = useState<FormState>(DEFAULT_FORM);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [createTagOpen, setCreateTagOpen] = useState(false);

    useEffect(() => {
        if (!isOpen) return;
        if (item) {
            setForm({
                title: item.title,
                description: item.description ?? '',
                type: item.type,
                status: item.status,
                priority: item.priority,
                dueDate: item.dueDate ?? '',
                estimatedMinutes: item.estimatedMinutes?.toString() ?? '',
                sprintId: item.sprintId ?? '',
                assigneeUserIds: item.assignees.map((a) => a.user.userId),
                tagIds: item.tags.map((t) => t.id),
            });
        } else {
            setForm(DEFAULT_FORM);
        }
        setError('');
    }, [isOpen, item]);

    function set<K extends keyof FormState>(key: K, value: FormState[K]) {
        setForm((prev) => ({ ...prev, [key]: value }));
    }

    function toggleArrayItem(key: 'assigneeUserIds' | 'tagIds', id: string) {
        setForm((prev) => {
            const arr = prev[key] as string[];
            return {
                ...prev,
                [key]: arr.includes(id) ? arr.filter((x) => x !== id) : [...arr, id],
            };
        });
    }

    function handleTagCreated(tag: TagDto) {
        setForm((prev) => ({
            ...prev,
            tagIds: prev.tagIds.includes(tag.id) ? prev.tagIds : [...prev.tagIds, tag.id],
        }));
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        if (!form.title.trim()) {
            setError('Title is required.');
            return;
        }
        setSaving(true);
        setError('');
        try {
            const minutes = form.estimatedMinutes ? parseInt(form.estimatedMinutes, 10) : undefined;
            if (isEditing && item) {
                const dto: UpdateWorkItemDto = {
                    title: form.title.trim(),
                    description: form.description.trim() || undefined,
                    status: form.status,
                    priority: form.priority,
                    dueDate: form.dueDate || undefined,
                    estimatedMinutes: minutes,
                    assigneeUserIds: form.assigneeUserIds,
                    tagIds: form.tagIds,
                };
                await onUpdate(item.id, dto);
            } else {
                const dto: CreateWorkItemDto = {
                    title: form.title.trim(),
                    description: form.description.trim() || undefined,
                    type: form.type,
                    status: form.status,
                    priority: form.priority,
                    dueDate: form.dueDate || undefined,
                    estimatedMinutes: minutes,
                    sprintId: form.sprintId || undefined,
                    assigneeUserIds: form.assigneeUserIds,
                    tagIds: form.tagIds,
                };
                await onCreate(dto);
            }
            onClose();
        } catch {
            setError('Something went wrong. Please try again.');
        } finally {
            setSaving(false);
        }
    }

    if (!isOpen) return null;

    return (
        <>
            <div
                role="dialog"
                aria-modal="true"
                aria-label={isEditing ? 'Edit task' : 'Create task'}
                className="fixed inset-0 z-50 flex items-center justify-center p-4"
            >
            {/* Overlay */}
            <div
                className="absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70"
                onClick={onClose}
            />

            {/* Panel */}
            <div className="relative z-10 w-full max-w-2xl rounded-2xl border border-zinc-200 bg-white shadow-xl dark:border-zinc-700 dark:bg-zinc-900 dark:shadow-2xl">
                {/* Header */}
                <div className="flex items-center justify-between border-b border-zinc-100 px-6 py-4 dark:border-zinc-800">
                    <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                        {isEditing ? 'Edit Task' : 'New Task'}
                    </h2>
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-lg p-1.5 text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-700 dark:text-zinc-500 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
                        aria-label="Close"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="max-h-[80vh] overflow-y-auto p-6">
                    {error && (
                        <p className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm text-rose-600 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-400">
                            {error}
                        </p>
                    )}

                    <div className="grid gap-4">
                        {/* Title */}
                        <div>
                            <Label>Title *</Label>
                            <Input
                                value={form.title}
                                onChange={(v) => set('title', v)}
                                placeholder="Task title…"
                            />
                        </div>

                        {/* Description */}
                        <div>
                            <Label>Description</Label>
                            <textarea
                                value={form.description}
                                onChange={(e) => set('description', e.target.value)}
                                placeholder="Describe the task…"
                                rows={3}
                                className="w-full resize-none rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30"
                            />
                        </div>

                        {/* Type / Status / Priority row */}
                        <div className="grid grid-cols-3 gap-3">
                            <div>
                                <Label>Type</Label>
                                <Select value={form.type} onChange={(v) => set('type', v as WorkItemType)}>
                                    {WORK_ITEM_TYPES.map((t) => (
                                        <option key={t} value={t}>{formatTypeLabel(t)}</option>
                                    ))}
                                </Select>
                            </div>
                            <div>
                                <Label>Status</Label>
                                <Select value={form.status} onChange={(v) => set('status', v as WorkItemStatus)}>
                                    {WORK_ITEM_STATUSES.map((s) => (
                                        <option key={s} value={s}>{formatStatusLabel(s)}</option>
                                    ))}
                                </Select>
                            </div>
                            <div>
                                <Label>Priority</Label>
                                <Select value={form.priority} onChange={(v) => set('priority', v as WorkItemPriority)}>
                                    {WORK_ITEM_PRIORITIES.map((p) => (
                                        <option key={p} value={p}>{formatPriorityLabel(p)}</option>
                                    ))}
                                </Select>
                            </div>
                        </div>

                        {/* Due Date / Estimated Minutes / Sprint */}
                        <div className="grid grid-cols-3 gap-3">
                            <div>
                                <Label>Due Date</Label>
                                <Input
                                    type="date"
                                    value={form.dueDate}
                                    onChange={(v) => set('dueDate', v)}
                                />
                            </div>
                            <div>
                                <Label>Estimated (min)</Label>
                                <Input
                                    type="number"
                                    value={form.estimatedMinutes}
                                    onChange={(v) => set('estimatedMinutes', v)}
                                    placeholder="e.g. 120"
                                />
                            </div>
                            <div>
                                <Label>Sprint</Label>
                                <Select value={form.sprintId} onChange={(v) => set('sprintId', v)}>
                                    <option value="">Default Sprint</option>
                                    {sprints.map((s) => (
                                        <option key={s.sprintId} value={s.sprintId}>{s.name}</option>
                                    ))}
                                </Select>
                            </div>
                        </div>

                        {/* Assignees */}
                        <div>
                            <Label>Assignees</Label>
                            <div className="flex flex-wrap gap-2">
                                {users.map((u) => {
                                    const selected = form.assigneeUserIds.includes(u.userId);
                                    return (
                                        <button
                                            key={u.userId}
                                            type="button"
                                            onClick={() => toggleArrayItem('assigneeUserIds', u.userId)}
                                            className={`rounded-xl border px-3 py-1.5 text-sm font-medium transition-colors ${
                                                selected
                                                    ? 'border-sky-300 bg-sky-50 text-sky-700 dark:border-sky-500/50 dark:bg-sky-500/15 dark:text-sky-300'
                                                    : 'border-zinc-200 bg-zinc-50 text-zinc-600 hover:border-zinc-300 hover:text-zinc-800 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-400 dark:hover:border-zinc-600 dark:hover:text-zinc-200'
                                            }`}
                                        >
                                            {u.name}
                                        </button>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Tags */}
                        <div>
                            <div className="mb-1 flex items-center justify-between gap-3">
                                <Label>Tags</Label>
                                <button
                                    type="button"
                                    onClick={() => setCreateTagOpen(true)}
                                    className="inline-flex items-center gap-1 rounded-lg border border-zinc-200 bg-zinc-50 px-2.5 py-1 text-xs font-semibold text-zinc-600 transition-colors hover:bg-zinc-100 hover:text-zinc-900 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:hover:bg-zinc-700 dark:hover:text-zinc-100"
                                >
                                    <Plus className="h-3.5 w-3.5" />
                                    New tag
                                </button>
                            </div>
                            <div className="flex flex-wrap gap-2">
                                {tags.length === 0 && (
                                    <p className="text-sm text-zinc-400 dark:text-zinc-500">
                                        No tags yet. Create one to classify this task.
                                    </p>
                                )}
                                {tags.map((tag) => {
                                    const selected = form.tagIds.includes(tag.id);
                                    return (
                                        <button
                                            key={tag.id}
                                            type="button"
                                            onClick={() => toggleArrayItem('tagIds', tag.id)}
                                            className={`inline-flex items-center gap-2 rounded-xl border px-3 py-1.5 text-sm font-medium transition-colors ${
                                                selected
                                                    ? 'border-violet-300 bg-violet-50 text-violet-700 dark:border-violet-500/50 dark:bg-violet-500/15 dark:text-violet-300'
                                                    : 'border-zinc-200 bg-zinc-50 text-zinc-600 hover:border-zinc-300 hover:text-zinc-800 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-400 dark:hover:border-zinc-600 dark:hover:text-zinc-200'
                                            }`}
                                        >
                                            <span
                                                className="h-2.5 w-2.5 rounded-full"
                                                style={{ backgroundColor: tag.color }}
                                                aria-hidden="true"
                                            />
                                            #{tag.name}
                                        </button>
                                    );
                                })}
                            </div>
                        </div>
                    </div>

                    {/* Footer */}
                    <div className="mt-6 flex items-center justify-end gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            className="rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-100 hover:text-zinc-900 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:hover:bg-zinc-700 dark:hover:text-zinc-100"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={saving}
                            className="rounded-xl bg-sky-500 px-5 py-2 text-sm font-semibold text-white transition-colors hover:bg-sky-600 disabled:opacity-50 dark:hover:bg-sky-400"
                        >
                            {saving ? 'Saving…' : isEditing ? 'Save Changes' : 'Create Task'}
                        </button>
                    </div>
                </form>
            </div>
            </div>

            <CreateTagModal
                isOpen={createTagOpen}
                onClose={() => setCreateTagOpen(false)}
                onCreated={handleTagCreated}
            />
        </>
    );
}
