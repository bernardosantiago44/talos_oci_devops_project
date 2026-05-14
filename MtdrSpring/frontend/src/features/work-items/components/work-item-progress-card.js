import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
export function WorkItemProgressCard({ item }) {
    const progress = Math.min(100, Math.round((item.loggedHours / item.estimatedHours) * 100));
    return (_jsxs("div", { className: "rounded-3xl border border-white/10 bg-white/[0.04] p-5", children: [_jsxs("div", { className: "flex items-center justify-between gap-4", children: [_jsxs("div", { children: [_jsx("p", { className: "text-sm font-medium text-white", children: "Execution progress" }), _jsx("p", { className: "mt-1 text-sm text-zinc-400", children: "Logged effort versus original estimate" })] }), _jsxs("p", { className: "text-2xl font-semibold text-white", children: [progress, "%"] })] }), _jsx("div", { className: "mt-4 h-3 overflow-hidden rounded-full bg-white/10", children: _jsx("div", { className: "h-full rounded-full bg-gradient-to-r from-cyan-300 via-sky-400 to-violet-400", style: { width: `${progress}%` } }) })] }));
}
