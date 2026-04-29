import type { ApiResult } from '@/shared/dtos/api-result.dto';
import type { PagedResult } from '@/shared/dtos/paged-result.dto';
import type { CreateWorkItemDto } from '../dtos/create-work-item.dto';
import type { UpdateWorkItemDto } from '../dtos/update-work-item.dto';
import type { WorkItemDetailDto, Assignee } from '../dtos/work-item-detail.dto';
import type { WorkItemFiltersDto } from '../dtos/work-item-filters.dto';
import type { WorkItemListItemDto } from '../dtos/work-item-list-item.dto';
import type { UserSummaryDto } from '@/shared/dtos/user-summary.dto';
import type { WorkItemType } from '../enums/work-item-type.enum';
import type { WorkItemPriority } from '../enums/work-item-priority.enum';
import { normalizeStatus, toBackendStatus } from '../enums/work-item-status.enum';
import { apiClient } from '@/shared/services/api-client';

// ─── Backend response shape from WorkItemController ──────────────

interface BackendWorkItemRow {
    workItemId: string;
    title: string;
    description?: string;
    status: string;
    priority: string;
    workType: string;
    dueDate?: string;
    createdAt: string;
    estimatedMinutes?: number;
    sprintName?: string;
    sprintId?: string;
    assigneeName?: string;
    assignees?: Assignee[];
}

// ─── Mappers ─────────────────────────────────────────────────────

function mapBackendRowToDetailDto(row: BackendWorkItemRow): WorkItemDetailDto {
    return {
        id: row.workItemId,
        sprintId: row.sprintId,
        title: row.title,
        description: row.description ?? undefined,
        type: (row.workType ?? 'TASK') as WorkItemType,
        status: normalizeStatus(row.status),
        priority: (row.priority ?? 'MEDIUM') as WorkItemPriority,
        estimatedMinutes: row.estimatedMinutes ?? undefined,
        totalLoggedMinutes: 0,
        dueDate: row.dueDate ? String(row.dueDate).slice(0, 10) : undefined,
        createdAt: row.createdAt,
        updatedAt: row.createdAt,
        createdBy: { userId: 'system', name: 'System' },
        assignees: row.assignees ?? [],
        tags: [],
    };
}

function mapToListItemDto(detail: WorkItemDetailDto): WorkItemListItemDto {
    return {
        id: detail.id,
        sprintId: detail.sprintId,
        title: detail.title,
        type: detail.type,
        status: detail.status,
        priority: detail.priority,
        estimatedMinutes: detail.estimatedMinutes,
        totalLoggedMinutes: detail.totalLoggedMinutes,
        dueDate: detail.dueDate,
        createdAt: detail.createdAt,
        updatedAt: detail.updatedAt,
        createdBy: detail.createdBy,
        assignees: detail.assignees.map((a: Assignee): UserSummaryDto => a.user),
        tags: detail.tags,
    };
}

function applyClientSideFilters(
    items: WorkItemDetailDto[],
    filters?: WorkItemFiltersDto
): WorkItemDetailDto[] {
    if (!filters) return items;

    return items.filter((item) => {
        const matchesSearch =
            !filters.search ||
            item.title.toLowerCase().includes(filters.search.toLowerCase()) ||
            (item.description ?? '').toLowerCase().includes(filters.search.toLowerCase());

        const matchesSprint = !filters.sprintId || item.sprintId === filters.sprintId;
        const matchesType = !filters.type || item.type === filters.type;
        const matchesStatus = !filters.status || item.status === filters.status;
        const matchesPriority = !filters.priority || item.priority === filters.priority;

        const matchesAssignee =
            !filters.assigneeUserId ||
            item.assignees.some((a) => a.user.userId === filters.assigneeUserId);

        return matchesSearch && matchesSprint && matchesType && matchesStatus && matchesPriority && matchesAssignee;
    });
}

// ─── Service (real HTTP calls) ───────────────────────────────────

export const workItemService = {
    async getWorkItems(
        filters?: WorkItemFiltersDto
    ): Promise<ApiResult<PagedResult<WorkItemListItemDto>>> {
        const result = await apiClient.get<BackendWorkItemRow[]>('/workitems');

        if (!result.success) {
            return {
                success: false,
                data: { items: [], total: 0, page: 1, pageSize: 10, totalPages: 0 },
                message: result.message,
            };
        }

        const allDetails = result.data.map(mapBackendRowToDetailDto);
        const filtered = applyClientSideFilters(allDetails, filters);

        const page = filters?.page ?? 1;
        const pageSize = filters?.pageSize ?? 100;
        const start = (page - 1) * pageSize;
        const paged = filtered.slice(start, start + pageSize).map(mapToListItemDto);

        return {
            success: true,
            data: {
                items: paged,
                total: filtered.length,
                page,
                pageSize,
                totalPages: Math.max(1, Math.ceil(filtered.length / pageSize)),
            },
        };
    },

    async getWorkItemById(id: string): Promise<ApiResult<WorkItemDetailDto | null>> {
        // The backend doesn't have a single-item endpoint, so fetch all and filter.
        const result = await apiClient.get<BackendWorkItemRow[]>('/workitems');

        if (!result.success) {
            return { success: false, data: null, message: result.message };
        }

        const row = result.data.find((r) => r.workItemId === id);
        if (!row) return { success: true, data: null };

        return { success: true, data: mapBackendRowToDetailDto(row) };
    },

    async createWorkItem(input: CreateWorkItemDto): Promise<ApiResult<WorkItemDetailDto>> {
        const body = {
            title: input.title,
            description: input.description ?? '',
            workType: input.type ?? 'TASK',
            priority: input.priority ?? 'MEDIUM',
            sprintId: input.sprintId ?? null,
            dueDateStr: input.dueDate ?? null,
            assigneeUserId: input.assigneeUserIds?.[0] ?? null,
        };

        const result = await apiClient.post<{ workItemId: string }>('/workitems', body);

        if (!result.success) {
            return { success: false, data: null as unknown as WorkItemDetailDto, message: result.message };
        }

        // Build an optimistic local object so the UI updates immediately
        const now = new Date().toISOString();
        const created: WorkItemDetailDto = {
            id: result.data.workItemId,
            sprintId: input.sprintId,
            title: input.title,
            description: input.description,
            type: input.type,
            status: 'TODO',
            priority: input.priority,
            estimatedMinutes: input.estimatedMinutes,
            totalLoggedMinutes: 0,
            dueDate: input.dueDate,
            createdAt: now,
            updatedAt: now,
            createdBy: { userId: 'current', name: 'Current User' },
            assignees: [],
            tags: [],
        };

        return { success: true, data: created };
    },

    async updateWorkItem(
        id: string,
        input: UpdateWorkItemDto
    ): Promise<ApiResult<WorkItemDetailDto | null>> {
        // The backend currently only supports status updates via PUT /workitems/{id}/status
        if (input.status) {
            const statusResult = await apiClient.put<{ workItemId: string; status: string }>(
                `/workitems/${id}/status`,
                { status: toBackendStatus(input.status) }
            );
            if (!statusResult.success) {
                return { success: false, data: null, message: statusResult.message };
            }
        }

        // Re-fetch to get the updated item
        return this.getWorkItemById(id);
    },

    async deleteWorkItem(id: string): Promise<ApiResult<boolean>> {
        const result = await apiClient.delete<void>(`/workitems/${id}`);
        return { success: result.success, data: result.success, message: result.message };
    },
};