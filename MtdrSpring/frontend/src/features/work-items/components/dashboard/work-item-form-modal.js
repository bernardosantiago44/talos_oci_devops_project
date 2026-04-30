import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { WORK_ITEM_TYPES } from '../../enums/work-item-type.enum';
import { WORK_ITEM_STATUSES } from '../../enums/work-item-status.enum';
import { WORK_ITEM_PRIORITIES } from '../../enums/work-item-priority.enum';
import { formatTypeLabel, formatStatusLabel, formatPriorityLabel, } from '../../lib/dashboard-ui';
const DEFAULT_FORM = {
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
function Label({ children }) {
    return _jsx("label", { className: "mb-1 block text-xs font-semibold uppercase tracking-wider text-zinc-500 dark:text-zinc-400", children: children });
}
function Input({ value, onChange, placeholder, type = 'text' }) {
    return (_jsx("input", { type: type, value: value, onChange: (e) => onChange(e.target.value), placeholder: placeholder, className: "w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30" }));
}
function Select({ value, onChange, children }) {
    return (_jsx("select", { value: value, onChange: (e) => onChange(e.target.value), className: "w-full rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30", children: children }));
}
export function WorkItemFormModal({ isOpen, item, users, sprints, tags, onClose, onCreate, onUpdate, }) {
    const isEditing = !!item;
    const [form, setForm] = useState(DEFAULT_FORM);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    useEffect(() => {
        if (!isOpen)
            return;
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
        }
        else {
            setForm(DEFAULT_FORM);
        }
        setError('');
    }, [isOpen, item]);
    function set(key, value) {
        setForm((prev) => ({ ...prev, [key]: value }));
    }
    function toggleArrayItem(key, id) {
        setForm((prev) => {
            const arr = prev[key];
            return {
                ...prev,
                [key]: arr.includes(id) ? arr.filter((x) => x !== id) : [...arr, id],
            };
        });
    }
    async function handleSubmit(e) {
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
                const dto = {
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
            }
            else {
                const dto = {
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
        }
        catch {
            setError('Something went wrong. Please try again.');
        }
        finally {
            setSaving(false);
        }
    }
    if (!isOpen)
        return null;
    return (_jsxs("div", { role: "dialog", "aria-modal": "true", "aria-label": isEditing ? 'Edit task' : 'Create task', className: "fixed inset-0 z-50 flex items-center justify-center p-4", children: [_jsx("div", { className: "absolute inset-0 bg-black/50 backdrop-blur-sm dark:bg-black/70", onClick: onClose }), _jsxs("div", { className: "relative z-10 w-full max-w-2xl rounded-2xl border border-zinc-200 bg-white shadow-xl dark:border-zinc-700 dark:bg-zinc-900 dark:shadow-2xl", children: [_jsxs("div", { className: "flex items-center justify-between border-b border-zinc-100 px-6 py-4 dark:border-zinc-800", children: [_jsx("h2", { className: "text-base font-semibold text-zinc-900 dark:text-zinc-100", children: isEditing ? 'Edit Task' : 'New Task' }), _jsx("button", { type: "button", onClick: onClose, className: "rounded-lg p-1.5 text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-700 dark:text-zinc-500 dark:hover:bg-zinc-800 dark:hover:text-zinc-200", "aria-label": "Close", children: _jsx(X, { className: "h-4 w-4" }) })] }), _jsxs("form", { onSubmit: handleSubmit, className: "max-h-[80vh] overflow-y-auto p-6", children: [error && (_jsx("p", { className: "mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm text-rose-600 dark:border-rose-500/20 dark:bg-rose-500/10 dark:text-rose-400", children: error })), _jsxs("div", { className: "grid gap-4", children: [_jsxs("div", { children: [_jsx(Label, { children: "Title *" }), _jsx(Input, { value: form.title, onChange: (v) => set('title', v), placeholder: "Task title\u2026" })] }), _jsxs("div", { children: [_jsx(Label, { children: "Description" }), _jsx("textarea", { value: form.description, onChange: (e) => set('description', e.target.value), placeholder: "Describe the task\u2026", rows: 3, className: "w-full resize-none rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm text-zinc-800 placeholder-zinc-400 outline-none focus:border-sky-400/60 focus:ring-1 focus:ring-sky-400/30 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-200 dark:placeholder-zinc-600 dark:focus:border-sky-500/60 dark:focus:ring-sky-500/30" })] }), _jsxs("div", { className: "grid grid-cols-3 gap-3", children: [_jsxs("div", { children: [_jsx(Label, { children: "Type" }), _jsx(Select, { value: form.type, onChange: (v) => set('type', v), children: WORK_ITEM_TYPES.map((t) => (_jsx("option", { value: t, children: formatTypeLabel(t) }, t))) })] }), _jsxs("div", { children: [_jsx(Label, { children: "Status" }), _jsx(Select, { value: form.status, onChange: (v) => set('status', v), children: WORK_ITEM_STATUSES.map((s) => (_jsx("option", { value: s, children: formatStatusLabel(s) }, s))) })] }), _jsxs("div", { children: [_jsx(Label, { children: "Priority" }), _jsx(Select, { value: form.priority, onChange: (v) => set('priority', v), children: WORK_ITEM_PRIORITIES.map((p) => (_jsx("option", { value: p, children: formatPriorityLabel(p) }, p))) })] })] }), _jsxs("div", { className: "grid grid-cols-3 gap-3", children: [_jsxs("div", { children: [_jsx(Label, { children: "Due Date" }), _jsx(Input, { type: "date", value: form.dueDate, onChange: (v) => set('dueDate', v) })] }), _jsxs("div", { children: [_jsx(Label, { children: "Estimated (min)" }), _jsx(Input, { type: "number", value: form.estimatedMinutes, onChange: (v) => set('estimatedMinutes', v), placeholder: "e.g. 120" })] }), _jsxs("div", { children: [_jsx(Label, { children: "Sprint" }), _jsxs(Select, { value: form.sprintId, onChange: (v) => set('sprintId', v), children: [_jsx("option", { value: "", children: "Default Sprint" }), sprints.map((s) => (_jsx("option", { value: s.sprintId, children: s.name }, s.sprintId)))] })] })] }), _jsxs("div", { children: [_jsx(Label, { children: "Assignees" }), _jsx("div", { className: "flex flex-wrap gap-2", children: users.map((u) => {
                                                    const selected = form.assigneeUserIds.includes(u.userId);
                                                    return (_jsx("button", { type: "button", onClick: () => toggleArrayItem('assigneeUserIds', u.userId), className: `rounded-xl border px-3 py-1.5 text-sm font-medium transition-colors ${selected
                                                            ? 'border-sky-300 bg-sky-50 text-sky-700 dark:border-sky-500/50 dark:bg-sky-500/15 dark:text-sky-300'
                                                            : 'border-zinc-200 bg-zinc-50 text-zinc-600 hover:border-zinc-300 hover:text-zinc-800 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-400 dark:hover:border-zinc-600 dark:hover:text-zinc-200'}`, children: u.name }, u.userId));
                                                }) })] }), _jsxs("div", { children: [_jsx(Label, { children: "Tags" }), _jsx("div", { className: "flex flex-wrap gap-2", children: tags.map((tag) => {
                                                    const selected = form.tagIds.includes(tag.id);
                                                    return (_jsxs("button", { type: "button", onClick: () => toggleArrayItem('tagIds', tag.id), className: `rounded-xl border px-3 py-1.5 text-sm font-medium transition-colors ${selected
                                                            ? 'border-violet-300 bg-violet-50 text-violet-700 dark:border-violet-500/50 dark:bg-violet-500/15 dark:text-violet-300'
                                                            : 'border-zinc-200 bg-zinc-50 text-zinc-600 hover:border-zinc-300 hover:text-zinc-800 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-400 dark:hover:border-zinc-600 dark:hover:text-zinc-200'}`, children: ["#", tag.name] }, tag.id));
                                                }) })] })] }), _jsxs("div", { className: "mt-6 flex items-center justify-end gap-3", children: [_jsx("button", { type: "button", onClick: onClose, className: "rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-100 hover:text-zinc-900 dark:border-zinc-700/60 dark:bg-zinc-800/60 dark:text-zinc-300 dark:hover:bg-zinc-700 dark:hover:text-zinc-100", children: "Cancel" }), _jsx("button", { type: "submit", disabled: saving, className: "rounded-xl bg-sky-500 px-5 py-2 text-sm font-semibold text-white transition-colors hover:bg-sky-600 disabled:opacity-50 dark:hover:bg-sky-400", children: saving ? 'Saving…' : isEditing ? 'Save Changes' : 'Create Task' })] })] })] })] }));
}
