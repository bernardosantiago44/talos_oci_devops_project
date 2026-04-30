export function cx(...classes) {
    return classes.filter(Boolean).join(' ');
}
export function getInitials(name) {
    return name
        .split(' ')
        .map((part) => part[0])
        .join('')
        .slice(0, 2)
        .toUpperCase();
}
export function formatStatusLabel(status) {
    switch (status) {
        case 'TODO': return 'Todo';
        case 'NEW': return 'Todo';
        case 'IN_PROGRESS': return 'In Progress';
        case 'BLOCKED': return 'Blocked';
        case 'DONE': return 'Done';
        default: return status;
    }
}
export function formatTypeLabel(type) {
    switch (type) {
        case 'FEATURE': return 'Feature';
        case 'BUG': return 'Bug';
        case 'ISSUE': return 'Issue';
        case 'TASK': return 'Task';
        default: return type;
    }
}
export function formatPriorityLabel(priority) {
    switch (priority) {
        case 'LOW': return 'Low';
        case 'MEDIUM': return 'Medium';
        case 'HIGH': return 'High';
        case 'CRITICAL': return 'Critical';
        default: return priority;
    }
}
export function getStatusBadgeClasses(status) {
    switch (status) {
        case 'DONE':
            return 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-400/30 dark:bg-emerald-500/15 dark:text-emerald-300';
        case 'BLOCKED':
            return 'border-rose-200 bg-rose-50 text-rose-700 dark:border-rose-400/30 dark:bg-rose-500/15 dark:text-rose-300';
        case 'IN_PROGRESS':
            return 'border-sky-200 bg-sky-50 text-sky-700 dark:border-sky-400/30 dark:bg-sky-500/15 dark:text-sky-300';
        case 'TODO':
        default:
            return 'border-zinc-200 bg-zinc-100 text-zinc-600 dark:border-zinc-400/30 dark:bg-zinc-500/15 dark:text-zinc-400';
    }
}
export function getPriorityBadgeClasses(priority) {
    switch (priority) {
        case 'CRITICAL':
            return 'border-rose-200 bg-rose-50 text-rose-700 dark:border-rose-400/30 dark:bg-rose-500/15 dark:text-rose-300';
        case 'HIGH':
            return 'border-amber-200 bg-amber-50 text-amber-700 dark:border-amber-400/30 dark:bg-amber-500/15 dark:text-amber-300';
        case 'MEDIUM':
            return 'border-violet-200 bg-violet-50 text-violet-700 dark:border-violet-400/30 dark:bg-violet-500/15 dark:text-violet-300';
        case 'LOW':
        default:
            return 'border-zinc-200 bg-zinc-100 text-zinc-500 dark:border-zinc-400/30 dark:bg-zinc-500/15 dark:text-zinc-400';
    }
}
export function getTypeBadgeClasses(type) {
    switch (type) {
        case 'FEATURE':
            return 'border-cyan-200 bg-cyan-50 text-cyan-700 dark:border-cyan-400/30 dark:bg-cyan-500/15 dark:text-cyan-300';
        case 'BUG':
            return 'border-rose-200 bg-rose-50 text-rose-700 dark:border-rose-400/30 dark:bg-rose-500/15 dark:text-rose-300';
        case 'ISSUE':
            return 'border-orange-200 bg-orange-50 text-orange-700 dark:border-orange-400/30 dark:bg-orange-500/15 dark:text-orange-300';
        case 'TASK':
        default:
            return 'border-indigo-200 bg-indigo-50 text-indigo-700 dark:border-indigo-400/30 dark:bg-indigo-500/15 dark:text-indigo-300';
    }
}
export function getStatusTextColor(status) {
    switch (status) {
        case 'DONE': return 'text-emerald-600 dark:text-emerald-300';
        case 'BLOCKED': return 'text-rose-600 dark:text-rose-300';
        case 'IN_PROGRESS': return 'text-sky-600 dark:text-sky-300';
        case 'TODO': return 'text-zinc-500 dark:text-zinc-400';
        default: return 'text-zinc-500 dark:text-zinc-300';
    }
}
export function getStatusDotColor(status) {
    switch (status) {
        case 'DONE': return 'bg-emerald-500 dark:bg-emerald-400';
        case 'BLOCKED': return 'bg-rose-500 dark:bg-rose-400';
        case 'IN_PROGRESS': return 'bg-sky-500 dark:bg-sky-400';
        case 'TODO': return 'bg-zinc-400 dark:bg-zinc-500';
        default: return 'bg-zinc-400 dark:bg-zinc-500';
    }
}
export function calcProgress(logged, estimated) {
    if (!estimated || estimated === 0)
        return 0;
    return Math.min(100, Math.round((logged / estimated) * 100));
}
export function isOverdue(dueDate, status) {
    if (!dueDate || status === 'DONE')
        return false;
    const today = new Date().toISOString().slice(0, 10);
    return dueDate < today;
}
export function formatDate(dateStr) {
    if (!dateStr)
        return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}
export function getSprintLabel(sprintId, sprintMap) {
    if (!sprintId)
        return '—';
    if (sprintMap && sprintMap[sprintId])
        return sprintMap[sprintId];
    return sprintId;
}
