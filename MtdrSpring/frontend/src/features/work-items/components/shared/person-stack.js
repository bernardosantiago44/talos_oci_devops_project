import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { PersonAvatar } from './person-avatar';
export function PersonStack({ people }) {
    return (_jsxs("div", { className: "flex items-center gap-3", children: [_jsx("div", { className: "flex -space-x-3", children: people.map((person) => (_jsx(PersonAvatar, { person: person, className: "h-10 w-10 border-2 border-zinc-950 shadow-md" }, person.id))) }), _jsxs("div", { children: [_jsxs("p", { className: "text-sm font-medium text-white", children: [people.length, " collaborators"] }), _jsx("p", { className: "text-xs text-zinc-400", children: "Cross-functional ownership" })] })] }));
}
