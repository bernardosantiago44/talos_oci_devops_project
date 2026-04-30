import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { Clock3, Link2, MessageSquare, Timer } from 'lucide-react';
import { MetricCard } from './shared/metric-card';
export function WorkItemMetrics({ item }) {
    return (_jsxs("div", { className: "mt-8 grid gap-4 md:grid-cols-2 xl:grid-cols-4", children: [_jsx(MetricCard, { icon: _jsx(Clock3, { className: "h-4 w-4" }), label: "Estimate", value: `${item.estimatedHours}h`, hint: "Original planning effort" }), _jsx(MetricCard, { icon: _jsx(Timer, { className: "h-4 w-4" }), label: "Logged", value: `${item.loggedHours}h`, hint: "Actual work captured" }), _jsx(MetricCard, { icon: _jsx(MessageSquare, { className: "h-4 w-4" }), label: "Discussion", value: `${item.commentsCount}`, hint: "Active collaboration" }), _jsx(MetricCard, { icon: _jsx(Link2, { className: "h-4 w-4" }), label: "Linked items", value: `${item.linkedItemsCount}`, hint: "Dependencies and blockers" })] }));
}
