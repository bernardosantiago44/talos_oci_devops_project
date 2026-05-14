import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { PersonStack } from './shared/person-stack';
export function WorkItemMetaCard({ item }) {
    return (_jsxs("div", { className: "rounded-3xl border border-white/10 bg-white/[0.04] p-5", children: [_jsx(PersonStack, { people: item.assignees }), _jsx("div", { className: "mt-4 flex flex-wrap gap-2", children: item.tags.map((tag) => (_jsxs("span", { className: "inline-flex items-center rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-medium text-zinc-300", children: ["#", tag] }, tag))) })] }));
}
