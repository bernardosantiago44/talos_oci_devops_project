import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { CheckSquare, Clock, AlertCircle, CheckCircle2, AlertTriangle } from 'lucide-react';
import { isOverdue } from '../../lib/dashboard-ui';
export function DashboardSummaryCards({ items }) {
    const total = items.length;
    const todo = items.filter((i) => i.status === 'TODO').length;
    const inProgress = items.filter((i) => i.status === 'IN_PROGRESS').length;
    const blocked = items.filter((i) => i.status === 'BLOCKED').length;
    const done = items.filter((i) => i.status === 'DONE').length;
    const overdue = items.filter((i) => isOverdue(i.dueDate, i.status)).length;
    const cards = [
        {
            label: 'Total Tasks',
            value: total,
            icon: _jsx(CheckSquare, { className: "h-5 w-5" }),
            color: 'text-zinc-600 dark:text-zinc-300',
            bg: 'bg-zinc-100 dark:bg-zinc-800/60',
            border: 'border-zinc-200 dark:border-zinc-700/50',
        },
        {
            label: 'Todo',
            value: todo,
            icon: _jsx(Clock, { className: "h-5 w-5" }),
            color: 'text-zinc-500 dark:text-zinc-400',
            bg: 'bg-zinc-100 dark:bg-zinc-800/60',
            border: 'border-zinc-200 dark:border-zinc-700/50',
        },
        {
            label: 'In Progress',
            value: inProgress,
            icon: _jsx(AlertCircle, { className: "h-5 w-5 text-sky-500 dark:text-sky-400" }),
            color: 'text-sky-600 dark:text-sky-300',
            bg: 'bg-sky-50 dark:bg-sky-500/10',
            border: 'border-sky-200 dark:border-sky-500/20',
        },
        {
            label: 'Blocked',
            value: blocked,
            icon: _jsx(AlertTriangle, { className: "h-5 w-5 text-rose-500 dark:text-rose-400" }),
            color: 'text-rose-600 dark:text-rose-300',
            bg: 'bg-rose-50 dark:bg-rose-500/10',
            border: 'border-rose-200 dark:border-rose-500/20',
        },
        {
            label: 'Done',
            value: done,
            icon: _jsx(CheckCircle2, { className: "h-5 w-5 text-emerald-500 dark:text-emerald-400" }),
            color: 'text-emerald-600 dark:text-emerald-300',
            bg: 'bg-emerald-50 dark:bg-emerald-500/10',
            border: 'border-emerald-200 dark:border-emerald-500/20',
        },
        {
            label: 'Overdue',
            value: overdue,
            icon: _jsx(AlertTriangle, { className: "h-5 w-5 text-amber-500 dark:text-amber-400" }),
            color: 'text-amber-600 dark:text-amber-300',
            bg: 'bg-amber-50 dark:bg-amber-500/10',
            border: 'border-amber-200 dark:border-amber-500/20',
        },
    ];
    return (_jsx("div", { className: "grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6", children: cards.map((card) => (_jsxs("div", { className: `flex flex-col gap-2 rounded-2xl border p-4 ${card.bg} ${card.border}`, children: [_jsxs("div", { className: "flex items-center justify-between", children: [_jsx("span", { className: `${card.color} opacity-70`, children: card.icon }), _jsx("span", { className: `text-2xl font-bold ${card.color}`, children: card.value })] }), _jsx("p", { className: "text-xs font-medium text-zinc-500 dark:text-zinc-400", children: card.label })] }, card.label))) }));
}
