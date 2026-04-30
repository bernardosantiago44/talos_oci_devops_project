import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { WorkItemContextCard } from '@/features/work-items/components/work-item-context-card';
import { WorkItemDetailHeader } from '@/features/work-items/components/work-item-detail-header';
const mockWorkItem = {
    id: 'WI-102',
    title: 'Create work item detail experience for managers and developers',
    type: 'feature',
    status: 'in_progress',
    priority: 'high',
    sprintName: 'Sprint 04 · Frontend Foundations',
    estimatedHours: 12,
    loggedHours: 7.5,
    dueDate: 'Apr 22, 2026',
    description: 'Design and implement a polished work item detail screen that consolidates task context, assignees, discussion, related items, and activity history.',
    acceptanceCriteria: [
        'Header shows title, type, status, priority, sprint, and main actions.',
        'The layout supports comments, related links, and activity in distinct reusable panels.',
        'The visual hierarchy is strong enough for manager visibility and quick developer execution.',
    ],
    tags: ['frontend', 'design-system', 'mvp'],
    assignees: [
        { id: 'u1', name: 'Bernardo', role: 'Manager' },
        { id: 'u2', name: 'Ana Torres', role: 'Frontend Dev' },
        { id: 'u3', name: 'Luis Vega', role: 'Backend Dev' },
    ],
    reporter: { id: 'u4', name: 'Sofia Ruiz', role: 'Product Owner' },
    externalLink: 'https://example.com/spec/work-item-detail',
    commentsCount: 6,
    linkedItemsCount: 3,
};
export function WorkItemPrototypePage() {
    return (_jsx("main", { className: "min-h-screen bg-zinc-950 px-6 py-8 text-white", children: _jsxs("div", { className: "mx-auto grid max-w-7xl gap-6", children: [_jsx(WorkItemDetailHeader, { item: mockWorkItem }), _jsx(WorkItemContextCard, { item: mockWorkItem })] }) }));
}
