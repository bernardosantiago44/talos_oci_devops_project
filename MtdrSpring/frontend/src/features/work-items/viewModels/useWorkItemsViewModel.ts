import { useState, useCallback, useMemo } from 'react';
import { useAppUserList, useSprintList, useTagList, useTimeEntryCreate, useWorkItemCreate, useWorkItemDelete, useWorkItemList, useWorkItemUpdate } from '@/hooks/api';
import type { AppUserSummary, CreateWorkItemRequest, SprintResponse, TagResponse, UpdateWorkItemRequest, WorkItemQuery, WorkItemResponse } from '@/api/generated';
import type { ViewMode } from '@/features/work-items/components/dashboard/dashboard-toolbar';
import type { WorkItemDetailDto, Assignee } from '../dtos/work-item-detail.dto';
import type { WorkLogDto, WorkLogMode } from '../dtos/work-log.dto';
import type { WorkItemStatus } from '../enums/work-item-status.enum';
import { normalizeStatus, toBackendStatus } from '../enums/work-item-status.enum';
import type { WorkItemType } from '../enums/work-item-type.enum';
import type { WorkItemPriority } from '../enums/work-item-priority.enum';
import type { AssignmentRole } from '../enums/assignment-role.enum';
import type { UserSummaryDto } from '@/shared/dtos/user-summary.dto';
import type { TagDto } from '@/shared/dtos/tag.dto';

export type CreateWorkItemFormInput = Omit<
  CreateWorkItemRequest,
  'sprintId' | 'createdByUserId' | 'workType' | 'status' | 'priority' | 'assigneeIds'
> & {
  sprintId?: CreateWorkItemRequest['sprintId'];
  type: WorkItemType;
  status?: WorkItemStatus;
  priority: WorkItemPriority;
  assigneeUserIds?: CreateWorkItemRequest['assigneeIds'];
};
type WorkItemWithTags = WorkItemResponse & {
  tagIds?: string[];
  tags?: Array<TagResponse>;
};

export interface SprintDto {
  sprintId: string;
  name: string;
  status?: string;
  startDate?: string;
  endDate?: string;
}

function mapAppUser(user: AppUserSummary): UserSummaryDto | null {
  if (!user.userId || !user.name) return null;

  return {
    userId: user.userId,
    name: user.name,
    email: user.email,
    telegramUserId: user.telegramUserId,
  };
}

function mapSprint(sprint: SprintResponse): SprintDto | null {
  if (!sprint.sprintId || !sprint.name) return null;

  return {
    sprintId: sprint.sprintId,
    name: sprint.name,
    status: sprint.status,
    startDate: sprint.startDate,
    endDate: sprint.endDate,
  };
}

function compactFilterList<T extends string>(values: T[]): T[] | undefined {
  const filteredValues = values.filter(Boolean);
  return filteredValues.length > 0 ? filteredValues : undefined;
}

function buildBaseWorkItemQuery(filters: {
  search: string;
  status: WorkItemStatus[];
  assignee: string[];
  sprint: string[];
}): WorkItemQuery {
  return {
    search: filters.search.trim() || undefined,
    status: compactFilterList(filters.status),
    assignees: compactFilterList(filters.assignee),
    sprints: compactFilterList(filters.sprint),
  };
}

function buildWorkItemQueries(filters: {
  search: string;
  status: WorkItemStatus[];
  assignee: string[];
  sprint: string[];
  type: WorkItemType[];
}): WorkItemQuery[] {
  const baseQuery = buildBaseWorkItemQuery(filters);
  const selectedTypes = compactFilterList(filters.type);

  if (!selectedTypes) {
    return [baseQuery];
  }

  return selectedTypes.map((workType) => ({
    ...baseQuery,
    workType,
  }));
}

function mapWorkItem(
  row: WorkItemResponse,
  userById: Map<string, UserSummaryDto>,
  tagById: Map<string, TagDto>
): WorkItemDetailDto | null {
  if (!row.workItemId || !row.title) return null;
  const rowWithTags = row as WorkItemWithTags;

  const createdBy: UserSummaryDto =
    row.createdByUserId && userById.has(row.createdByUserId)
      ? userById.get(row.createdByUserId)!
      : {
          userId: row.createdByUserId ?? 'system',
          name: row.createdByUserId ?? 'System',
        };

  const assignees = (row.assignees ?? []).reduce<Assignee[]>((acc, assignment) => {
      const userId = assignment.user?.userId;
      if (!assignment.assignmentId || !userId) return acc;

      const user = userById.get(userId) ?? {
        userId,
        name: assignment.user?.name ?? userId,
        email: assignment.user?.email,
        telegramUserId: assignment.user?.telegramUserId,
      };

      const assignee: Assignee = {
        assignmentId: assignment.assignmentId,
        userId,
        user,
        assignmentRole: (assignment.assignmentRole ?? 'ASSIGNEE') as AssignmentRole,
        assignedAt: assignment.assignedAt ?? row.createdAt ?? new Date().toISOString(),
        unassignedAt: assignment.unassignedAt,
        assignedBy: assignment.assignedBy?.userId,
      };

      acc.push(assignee);
      return acc;
    }, []);

  const tags =
    rowWithTags.tags?.reduce<TagDto[]>((acc, tag) => {
      const id = tag.tagId;
      if (!id || !tag.name) return acc;

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
      .filter((tag): tag is TagDto => Boolean(tag)) ??
    [];

  return {
    id: row.workItemId,
    sprintId: row.sprintId,
    title: row.title,
    description: row.description,
    type: (row.workType ?? 'TASK') as WorkItemType,
    status: normalizeStatus(row.status),
    priority: (row.priority ?? 'MEDIUM') as WorkItemPriority,
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
  const [filters, setFilters] = useState({
    search: '',
    status: [] as WorkItemStatus[],
    assignee: [] as string[],
    sprint: [] as string[],
    type: [] as WorkItemType[],
  });
  const [viewMode, setViewMode] = useState<ViewMode>('list');

  const workItemQueries = useMemo(() => buildWorkItemQueries(filters), [filters]);
  const workItemsQuery = useWorkItemList(workItemQueries);
  const usersQuery = useAppUserList();
  const sprintsQuery = useSprintList();
  const tagsQuery = useTagList();
  const createWorkItemMutation = useWorkItemCreate();
  const updateWorkItemMutation = useWorkItemUpdate();
  const deleteWorkItemMutation = useWorkItemDelete();
  const createTimeEntryMutation = useTimeEntryCreate();

  const [modals, setModals] = useState({
    formOpen: false,
    detailOpen: false,
    workLogOpen: false,
    editingItem: null as WorkItemDetailDto | null,
    detailItem: null as WorkItemDetailDto | null,
    workLogItem: null as WorkItemDetailDto | null,
    workLogMode: 'log' as WorkLogMode,
  });

  const users = useMemo(
    () => (usersQuery.data ?? []).map(mapAppUser).filter((user): user is UserSummaryDto => Boolean(user)),
    [usersQuery.data]
  );

  const sprints = useMemo(
    () => (sprintsQuery.data ?? []).map(mapSprint).filter((sprint): sprint is SprintDto => Boolean(sprint)),
    [sprintsQuery.data]
  );

  const userById = useMemo(() => new Map(users.map((user) => [user.userId, user])), [users]);
  const tags = useMemo(() => tagsQuery.data ?? [], [tagsQuery.data]);
  const tagById = useMemo(() => new Map(tags.map((tag) => [tag.id, tag])), [tags]);

  const items = useMemo(
    () =>
      (workItemsQuery.data ?? [])
        .map((item) => mapWorkItem(item, userById, tagById))
        .filter((item): item is WorkItemDetailDto => Boolean(item)),
    [workItemsQuery.data, userById, tagById]
  );

  const loadItems = useCallback(async () => {
    await workItemsQuery.refetch();
  }, [workItemsQuery]);

  const handleCreate = useCallback(
    async (dto: CreateWorkItemFormInput) => {
      const sprintId = dto.sprintId || sprints[0]?.sprintId;
      const createdByUserId = users[0]?.userId;

      if (!sprintId || !createdByUserId) {
        throw new Error('A sprint and creator user are required before creating work items.');
      }

      const body: CreateWorkItemRequest = {
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
    },
    [createWorkItemMutation, sprints, users]
  );

  const handleUpdate = useCallback(
    async (id: string, dto: UpdateWorkItemRequest) => {
      const body: UpdateWorkItemRequest = {
        title: dto.title,
        description: dto.description,
        status: dto.status ? toBackendStatus(dto.status) : undefined,
        priority: dto.priority,
        externalLink: dto.externalLink,
        estimatedMinutes: dto.estimatedMinutes,
        dueDate: dto.dueDate,
        completedAt: dto.completedAt,
        assigneeIds: dto.assigneeIds,
        tagIds: dto.tagIds,
        sprintId: dto.sprintId,
      };

      await updateWorkItemMutation.mutateAsync({ id, body });
    },
    [updateWorkItemMutation]
  );

  const handleDelete = useCallback(async (item: WorkItemDetailDto) => {
    if (!window.confirm(`¿Eliminar "${item.title}"?`)) return;
    await deleteWorkItemMutation.mutateAsync(item.id);
  }, [deleteWorkItemMutation]);

  const handleEdit = useCallback((item: WorkItemDetailDto) => {
    setModals(prev => ({
      ...prev,
      formOpen: true,
      detailOpen: false,
      editingItem: item,
      detailItem: null,
    }));
  }, []);

  const handleComplete = useCallback((item: WorkItemDetailDto) => {
    setModals(prev => ({
      ...prev,
      workLogOpen: true,
      workLogItem: item,
      workLogMode: 'complete',
    }));
  }, []);

  const handleLogWork = useCallback((item: WorkItemDetailDto) => {
    setModals(prev => ({
      ...prev,
      workLogOpen: true,
      workLogItem: item,
      workLogMode: 'log',
    }));
  }, []);

  const handleWorkLogSubmit = useCallback(async (dto: WorkLogDto) => {
    await createTimeEntryMutation.mutateAsync(dto);

    if (modals.workLogMode === 'complete') {
      await handleUpdate(dto.workItemId, {
        status: 'DONE',
        completedAt: new Date().toISOString()
      });
    }
  }, [createTimeEntryMutation, handleUpdate, modals.workLogMode]);

  const openNew = () =>
    setModals(prev => ({ ...prev, editingItem: null, formOpen: true }));

  const openEdit = (item: WorkItemDetailDto) =>
    setModals(prev => ({ ...prev, editingItem: item, formOpen: true, detailOpen: false }));

  const openDetail = (item: WorkItemDetailDto) =>
    setModals(prev => ({ ...prev, detailItem: item, detailOpen: true }));

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

  const handleEditFromDetail = useCallback((item: WorkItemDetailDto) => {
    closeAll();
    handleEdit(item);
  }, [handleEdit, closeAll]);

  const handleCompleteFromDetail = useCallback(async (item: WorkItemDetailDto) => {
    handleComplete(item);
  }, [handleComplete]);

  const detailItem = modals.detailItem
    ? items.find((item) => item.id === modals.detailItem?.id) ?? modals.detailItem
    : null;

  return {
    items,
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
    typeFilter: filters.type,
    sprintFilter: filters.sprint,
    setSearch: (search: string) => setFilters(f => ({ ...f, search })),
    setStatusFilter: (status: WorkItemStatus[]) => setFilters(f => ({ ...f, status })),
    setAssigneeFilter: (assignee: string[]) => setFilters(f => ({ ...f, assignee })),
    setTypeFilter: (type: WorkItemType[]) => setFilters(f => ({ ...f, type })),
    setSprintFilter: (sprint: string[]) => setFilters(f => ({ ...f, sprint })),

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
      handleDelete,
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

export type IWorkItemsViewModel = ReturnType<typeof useWorkItemsViewModel>;
