import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { TrendingUp, BarChart2 } from 'lucide-react';
import { DeveloperAnalyticsCard } from '../components/dashboard/developer-analytics-card';
import { VelocityFulfillmentCard } from '../components/dashboard/velocity-fulfillment-card';
export function AnalyticsPage() {
    return (_jsxs("div", { className: "space-y-8", children: [_jsxs("div", { children: [_jsxs("div", { className: "mb-3 flex items-center gap-2", children: [_jsx(TrendingUp, { className: "h-4 w-4 text-sky-500" }), _jsx("h2", { className: "text-sm font-semibold text-zinc-600 dark:text-zinc-300 uppercase tracking-wider", children: "Velocity Fulfillment" })] }), _jsx(VelocityFulfillmentCard, {})] }), _jsx("div", { className: "border-t border-zinc-200 dark:border-zinc-800" }), _jsxs("div", { children: [_jsxs("div", { className: "mb-3 flex items-center gap-2", children: [_jsx(BarChart2, { className: "h-4 w-4 text-violet-500" }), _jsx("h2", { className: "text-sm font-semibold text-zinc-600 dark:text-zinc-300 uppercase tracking-wider", children: "Developer Performance" })] }), _jsx(DeveloperAnalyticsCard, {})] })] }));
}
