import { jsx as _jsx } from "react/jsx-runtime";
import { getPersonInitials, joinClasses } from '../../lib/work-item-ui';
export function PersonAvatar({ person, className }) {
    return (_jsx("div", { className: joinClasses('flex items-center justify-center rounded-full border border-white/10 bg-zinc-800 text-white', className), title: person.name, "aria-label": person.name, children: _jsx("span", { className: "text-xs font-semibold tracking-wide", children: getPersonInitials(person) }) }));
}
