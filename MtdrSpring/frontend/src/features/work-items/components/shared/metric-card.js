import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
export function MetricCard({ icon, label, value, hint }) {
    return (_jsx("div", { className: "rounded-3xl border border-white/10 bg-white/5 p-4 backdrop-blur-xl", children: _jsxs("div", { className: "flex items-start justify-between gap-3", children: [_jsx("div", { className: "rounded-2xl border border-white/10 bg-white/5 p-2 text-zinc-200", children: icon }), _jsxs("div", { className: "text-right", children: [_jsx("p", { className: "text-xs uppercase tracking-[0.18em] text-zinc-400", children: label }), _jsx("p", { className: "mt-1 text-2xl font-semibold text-white", children: value }), _jsx("p", { className: "mt-1 text-xs text-zinc-400", children: hint })] })] }) }));
}
