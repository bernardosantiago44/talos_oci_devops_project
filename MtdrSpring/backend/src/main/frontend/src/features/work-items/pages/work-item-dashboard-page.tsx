import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Layers } from 'lucide-react';
import type { WorkItemDetailDto } from '../dtos/work-item-detail.dto';
import type { CreateWorkItemDto } from '../dtos/create-work-item.dto';
import type { UpdateWorkItemDto } from '../dtos/update-work-item.dto';
import type { WorkItemStatus } from '../enums/work-item-status.enum';
import { workItemService } from '../services/work-item.service';
import { mockUsers } from '@/shared/mock/users.mock';
import { mockTags } from '@/shared/mock/tags.mock';
import { DashboardSummaryCards } from '../components/dashboard/dashboard-summary-cards';
import { DashboardToolbar } from '../components/dashboard/dashboard-toolbar';
import type { ViewMode } from '../components/dashboard/dashboard-toolbar';
import { WorkItemListView } from '../components/dashboard/work-item-list-view';
import { KanbanView } from '../components/dashboard/kanban-view';
import { WorkItemFormModal } from '../components/dashboard/work-item-form-modal';
import { WorkItemDetailModal } from '../components/dashboard/work-item-detail-modal';

export function WorkItemDashboardPage() {
    const [items, setItems] = useState<WorkItemDetailDto[]>([]);
    const [loading, setLoading] = useState(true);

    // Toolbar state
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState<WorkItemStatus | ''>('');
    const [assigneeFilter, setAssigneeFilter] = useState('');
    const [viewMode, setViewMode] = useState<ViewMode>('list');

    // Modal state
    const [formOpen, setFormOpen] = useState(false);
    const [editingItem, setEditingItem] = useState<WorkItemDetailDto | null>(null);
    const [detailItem, setDetailItem] = useState<WorkItemDetailDto | null>(null);
    const [detailOpen, setDetailOpen] = useState(false);

    // Load all items on mount (using the service so we stay in the service-layer contract)
    const loadItems = useCallback(async () => {
        setLoading(true);
        try {
            // Load a large page to get all items for the dashboard
            const result = await workItemService.getWorkItems({ page: 1, pageSize: 100 });
            if (result.success) {
                // getWorkItems returns WorkItemListItemDto[] but we need full detail for mutations.
                // Use the mock store directly by fetching each id — the service exposes getWorkItemById.
                const ids = result.data.items.map((i) => i.id);
                const details = await Promise.all(ids.map((id) => workItemService.getWorkItemById(id)));
                const fullItems = details
                    .filter((r) => r.success && r.data !== null)
                    .map((r) => r.data as WorkItemDetailDto);
                setItems(fullItems);
            }
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadItems();
    }, [loadItems]);

    // In-memory filtered items
    const filteredItems = useMemo(() => {
        return items.filter((item) => {
            const matchesSearch =
                !search ||
                item.title.toLowerCase().includes(search.toLowerCase()) ||
                (item.description ?? '').toLowerCase().includes(search.toLowerCase());

            const matchesStatus = !statusFilter || item.status === statusFilter;

            const matchesAssignee =
                !assigneeFilter ||
                item.assignees.some((a) => a.user.id === assigneeFilter);

            return matchesSearch && matchesStatus && matchesAssignee;
        });
    }, [items, search, statusFilter, assigneeFilter]);

    // Create handler
    const handleCreate = useCallback(async (dto: CreateWorkItemDto) => {
        const result = await workItemService.createWorkItem(dto);
        if (result.success) {
            setItems((prev) => [result.data, ...prev]);
        }
    }, []);

    // Update handler
    const handleUpdate = useCallback(async (id: string, dto: UpdateWorkItemDto) => {
        const result = await workItemService.updateWorkItem(id, dto);
        if (result.success && result.data) {
            const updated = result.data;
            setItems((prev) => prev.map((item) => (item.id === id ? updated : item)));
            // Refresh detail modal if open for this item
            if (detailItem?.id === id) {
                setDetailItem(updated);
            }
        }
    }, [detailItem]);

    // Complete handler
    const handleComplete = useCallback(async (item: WorkItemDetailDto) => {
        await handleUpdate(item.id, {
            status: 'DONE',
            completedAt: new Date().toISOString(),
        });
    }, [handleUpdate]);

    // Open edit
    const handleEdit = useCallback((item: WorkItemDetailDto) => {
        setEditingItem(item);
        setDetailOpen(false);
        setFormOpen(true);
    }, []);

    // Open detail
    const handleViewDetail = useCallback((item: WorkItemDetailDto) => {
        setDetailItem(item);
        setDetailOpen(true);
    }, []);

    // Close form
    const handleCloseForm = useCallback(() => {
        setFormOpen(false);
        setEditingItem(null);
    }, []);

    // Close detail
    const handleCloseDetail = useCallback(() => {
        setDetailOpen(false);
        setDetailItem(null);
    }, []);

    // Edit from detail modal
    const handleEditFromDetail = useCallback((item: WorkItemDetailDto) => {
        handleCloseDetail();
        handleEdit(item);
    }, [handleCloseDetail, handleEdit]);

    // Complete from detail modal
    const handleCompleteFromDetail = useCallback(async (item: WorkItemDetailDto) => {
        await handleComplete(item);
        handleCloseDetail();
    }, [handleComplete, handleCloseDetail]);

    return (
        <div className="min-h-screen bg-zinc-950 text-zinc-100">
            <div className="mx-auto max-w-screen-xl px-4 py-8 sm:px-6">

                {/* Page header */}
                <div className="mb-8 flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                    <div className="flex items-center gap-3">
                        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-sky-500/15 text-sky-400">
                            <Layers className="h-5 w-5" />
                        </div>
                        <div>
                            <h1 className="text-xl font-bold text-zinc-100">Work Items</h1>
                            <p className="text-sm text-zinc-500">Sprint 1 · Talos OCI DevOps Project</p>
                        </div>
                    </div>
                </div>

                {/* Summary cards */}
                <div className="mb-6">
                    <DashboardSummaryCards items={items} />
                </div>

                {/* Toolbar */}
                <div className="mb-5">
                    <DashboardToolbar
                        search={search}
                        onSearchChange={setSearch}
                        statusFilter={statusFilter}
                        onStatusFilterChange={setStatusFilter}
                        assigneeFilter={assigneeFilter}
                        onAssigneeFilterChange={setAssigneeFilter}
                        viewMode={viewMode}
                        onViewModeChange={setViewMode}
                        onCreateClick={() => {
                            setEditingItem(null);
                            setFormOpen(true);
                        }}
                        users={mockUsers}
                    />
                </div>

                {/* Results count */}
                <p className="mb-3 text-xs text-zinc-500">
                    {loading
                        ? 'Loading…'
                        : `${filteredItems.length} of ${items.length} task${items.length !== 1 ? 's' : ''}`}
                </p>

                {/* View area */}
                {loading ? (
                    <div className="flex items-center justify-center py-24">
                        <div className="h-8 w-8 animate-spin rounded-full border-2 border-zinc-700 border-t-sky-400" />
                    </div>
                ) : viewMode === 'list' ? (
                    <WorkItemListView
                        items={filteredItems}
                        onEdit={handleEdit}
                        onComplete={handleComplete}
                        onViewDetail={handleViewDetail}
                    />
                ) : (
                    <KanbanView
                        items={filteredItems}
                        onEdit={handleEdit}
                        onComplete={handleComplete}
                        onViewDetail={handleViewDetail}
                    />
                )}
            </div>

            {/* Create / Edit modal */}
            <WorkItemFormModal
                isOpen={formOpen}
                item={editingItem}
                users={mockUsers}
                tags={mockTags}
                onClose={handleCloseForm}
                onCreate={handleCreate}
                onUpdate={handleUpdate}
            />

            {/* Detail preview modal */}
            <WorkItemDetailModal
                isOpen={detailOpen}
                item={detailItem}
                onClose={handleCloseDetail}
                onEdit={handleEditFromDetail}
                onComplete={handleCompleteFromDetail}
            />
        </div>
    );
}
