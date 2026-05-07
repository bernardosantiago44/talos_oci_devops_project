import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { formatStatus, getPriorityClasses, getStatusClasses, getTypeClasses, joinClasses, } from '../../lib/work-item-ui';
function Pill({ children, className }) {
    return (_jsx("span", { className: joinClasses('inline-flex items-center rounded-full border px-3 py-1 text-xs font-medium capitalize', className), children: children }));
}
export function WorkItemBadgeRow({ id, type, status, priority, }) {
    return (_jsxs("div", { className: "flex flex-wrap items-center gap-2", children: [_jsx(Pill, { className: getTypeClasses(type), children: type }), _jsx(Pill, { className: getStatusClasses(status), children: formatStatus(status) }), _jsxs(Pill, { className: getPriorityClasses(priority), children: [priority, " priority"] }), _jsx(Pill, { className: "border-white/10 bg-white/5 text-zinc-300", children: id })] }));
}
