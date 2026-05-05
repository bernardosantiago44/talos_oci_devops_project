import { useState, useCallback, useMemo } from 'react';
import { useAppUserList, useSprintList, useTagList, useTimeEntryCreate, useWorkItemCreate, useWorkItemList, useWorkItemUpdate } from '@/hooks/api';
import { normalizeStatus, toBackendStatus } from '../enums/work-item-status.enum';
function mapAppUser(user) {
    if (!user.userId || !user.name)
        return null;
    return {
        userId: user.userId,
        name: user.name,
        email: user.email,
        telegramUserId: user.telegramUserId,
    };
}
function mapSprint(sprint) {
    if (!sprint.sprintId || !sprint.name)
        return null;
    return {
        sprintId: sprint.sprintId,
        name: sprint.name,
        status: sprint.status,
        startDate: sprint.startDate,
        endDate: sprint.endDate,
    };
}
function mapWorkItem(row, userById, tagById) {
    if (!row.workItemId || !row.title)
        return null;
    const rowWithTags = row;
    const createdBy = row.createdByUserId && userById.has(row.createdByUserId)
        ? userById.get(row.createdByUserId)
        : {
            userId: row.createdByUserId ?? 'system',
            name: row.createdByUserId ?? 'System',
        };
    const assignees = (row.assignees ?? []).reduce((acc, assignment) => {
        const userId = assignment.user?.userId;
        if (!assignment.assignmentId || !userId)
            return acc;
        const user = userById.get(userId) ?? {
            userId,
            name: assignment.user?.name ?? userId,
            email: assignment.user?.email,
            telegramUserId: assignment.user?.telegramUserId,
        };
        const assignee = {
            assignmentId: assignment.assignmentId,
            userId,
            user,
            assignmentRole: (assignment.assignmentRole ?? 'ASSIGNEE'),
            assignedAt: assignment.assignedAt ?? row.createdAt ?? new Date().toISOString(),
            unassignedAt: assignment.unassignedAt,
            assignedBy: assignment.assignedBy?.userId,
        };
        acc.push(assignee);
        return acc;
    }, []);
    const tags = rowWithTags.tags?.reduce((acc, tag) => {
        const id = tag.tagId ?? tag.id;
        if (!id || !tag.name)
            return acc;
        acc.push({
            id,
            name: tag.name,
            color: tag.color ?? '#3B82F6',
            description: tag.description,
        });
        return acc;
    }, []) ??
        rowWithTags.tagIds
            ?.map((tagId) => tagById.get(tagId))
            .filter((tag) => Boolean(tag)) ??
        [];
    return {
        id: row.workItemId,
        sprintId: row.sprintId,
        title: row.title,
        description: row.description,
        type: (row.workType ?? 'TASK'),
        status: normalizeStatus(row.status),
        priority: (row.priority ?? 'MEDIUM'),
        externalLink: row.externalLink,
        estimatedMinutes: row.estimatedMinutes,
        totalLoggedMinutes: 0,
        dueDate: row.dueDate ? String(row.dueDate).slice(0, 10) : undefined,
        createdAt: row.createdAt ?? new Date().toISOString(),
        updatedAt: row.updatedAt ?? row.createdAt ?? new Date().toISOString(),
        completedAt: row.completedAt,
        createdBy,
        assignees,
        tags,
    };
}
export const useWorkItemsViewModel = () => {
    const workItemsQuery = useWorkItemList();
    const usersQuery = useAppUserList();
    const sprintsQuery = useSprintList();
    const tagsQuery = useTagList();
    const createWorkItemMutation = useWorkItemCreate();
    const updateWorkItemMutation = useWorkItemUpdate();
    const createTimeEntryMutation = useTimeEntryCreate();
    const [filters, setFilters] = useState({
        search: '',
        status: '',
        assignee: '',
    });
    const [viewMode, setViewMode] = useState('list');
    const [modals, setModals] = useState({
        formOpen: false,
        detailOpen: false,
        workLogOpen: false,
        editingItem: null,
        detailItem: null,
        workLogItem: null,
        workLogMode: 'log',
    });
    const users = useMemo(() => (usersQuery.data ?? []).map(mapAppUser).filter((user) => Boolean(user)), [usersQuery.data]);
    const sprints = useMemo(() => (sprintsQuery.data ?? []).map(mapSprint).filter((sprint) => Boolean(sprint)), [sprintsQuery.data]);
    const userById = useMemo(() => new Map(users.map((user) => [user.userId, user])), [users]);
    const tags = useMemo(() => tagsQuery.data ?? [], [tagsQuery.data]);
    const tagById = useMemo(() => new Map(tags.map((tag) => [tag.id, tag])), [tags]);
    const items = useMemo(() => (workItemsQuery.data ?? [])
        .map((item) => mapWorkItem(item, userById, tagById))
        .filter((item) => Boolean(item)), [workItemsQuery.data, userById, tagById]);
    const filteredItems = useMemo(() => {
        return items.filter((item) => {
            const matchesSearch = !filters.search ||
                item.title.toLowerCase().includes(filters.search.toLowerCase()) ||
                (item.description ?? '').toLowerCase().includes(filters.search.toLowerCase());
            const matchesStatus = !filters.status || item.status === filters.status;
            const matchesAssignee = !filters.assignee ||
                item.assignees.some((a) => a.user.userId === filters.assignee);
            return matchesSearch && matchesStatus && matchesAssignee;
        });
    }, [items, filters]);
    const loadItems = useCallback(async () => {
        await workItemsQuery.refetch();
    }, [workItemsQuery]);
    const handleCreate = useCallback(async (dto) => {
        const sprintId = dto.sprintId || sprints[0]?.sprintId;
        const createdByUserId = users[0]?.userId;
        if (!sprintId || !createdByUserId) {
            throw new Error('A sprint and creator user are required before creating work items.');
        }
        const body = {
            sprintId,
            createdByUserId,
            workType: dto.type,
            title: dto.title,
            description: dto.description,
            status: toBackendStatus(dto.status ?? 'TODO'),
            priority: dto.priority,
            externalLink: dto.externalLink,
            estimatedMinutes: dto.estimatedMinutes,
            dueDate: dto.dueDate,
            assigneeIds: dto.assigneeUserIds,
            tagIds: dto.tagIds,
        };
        await createWorkItemMutation.mutateAsync(body);
    }, [createWorkItemMutation, sprints, users]);
    const handleUpdate = useCallback(async (id, dto) => {
        const body = {
            title: dto.title,
            description: dto.description,
            status: dto.status ? toBackendStatus(dto.status) : undefined,
            priority: dto.priority,
            externalLink: dto.externalLink,
            estimatedMinutes: dto.estimatedMinutes,
            dueDate: dto.dueDate,
            completedAt: dto.completedAt,
            assigneeIds: dto.assigneeUserIds,
            tagIds: dto.tagIds,
        };
        await updateWorkItemMutation.mutateAsync({ id, body });
    }, [updateWorkItemMutation]);
    const handleEdit = useCallback((item) => {
        setModals(prev => ({
            ...prev,
            formOpen: true,
            detailOpen: false,
            editingItem: item,
            detailItem: null,
        }));
    }, []);
    const handleComplete = useCallback((item) => {
        setModals(prev => ({
            ...prev,
            workLogOpen: true,
            workLogItem: item,
            workLogMode: 'complete',
        }));
    }, []);
    const handleLogWork = useCallback((item) => {
        setModals(prev => ({
            ...prev,
            workLogOpen: true,
            workLogItem: item,
            workLogMode: 'log',
        }));
    }, []);
    const handleWorkLogSubmit = useCallback(async (dto) => {
        await createTimeEntryMutation.mutateAsync(dto);
        if (modals.workLogMode === 'complete') {
            await handleUpdate(dto.workItemId, {
                status: 'DONE',
                completedAt: new Date().toISOString()
            });
        }
    }, [createTimeEntryMutation, handleUpdate, modals.workLogMode]);
    const openNew = () => setModals(prev => ({ ...prev, editingItem: null, formOpen: true }));
    const openEdit = (item) => setModals(prev => ({ ...prev, editingItem: item, formOpen: true, detailOpen: false }));
    const openDetail = (item) => setModals(prev => ({ ...prev, detailItem: item, detailOpen: true }));
    const closeAll = useCallback(() => {
        setModals(prev => ({
            ...prev,
            formOpen: false,
            detailOpen: false,
            workLogOpen: false,
            editingItem: null,
            detailItem: null,
            workLogItem: null,
        }));
    }, []);
    const closeWorkLog = useCallback(() => {
        setModals(prev => ({
            ...prev,
            workLogOpen: false,
            workLogItem: null,
            workLogMode: 'log',
        }));
    }, []);
    const handleEditFromDetail = useCallback((item) => {
        closeAll();
        handleEdit(item);
    }, [handleEdit, closeAll]);
    const handleCompleteFromDetail = useCallback(async (item) => {
        handleComplete(item);
    }, [handleComplete]);
    const detailItem = modals.detailItem
        ? items.find((item) => item.id === modals.detailItem?.id) ?? modals.detailItem
        : null;
    return {
        items: filteredItems,
        totalItemCount: () => items.length,
        loading: workItemsQuery.isLoading || usersQuery.isLoading || sprintsQuery.isLoading || tagsQuery.isLoading,
        viewMode,
        setViewMode,
        users,
        sprints,
        tags,
        search: filters.search,
        statusFilter: filters.status,
        assigneeFilter: filters.assignee,
        setSearch: (search) => setFilters(f => ({ ...f, search })),
        setStatusFilter: (status) => setFilters(f => ({ ...f, status })),
        setAssigneeFilter: (assignee) => setFilters(f => ({ ...f, assignee })),
        ...modals,
        detailItem,
        editingItem: modals.editingItem,
        actions: {
            loadItems,
            openNew,
            handleCreate,
            handleUpdate,
            handleComplete,
            handleLogWork,
            handleWorkLogSubmit,
            handleEdit,
            handleEditFromDetail,
            handleCompleteFromDetail,
            openEdit,
            openDetail,
            closeAll,
            closeWorkLog,
        }
    };
};
