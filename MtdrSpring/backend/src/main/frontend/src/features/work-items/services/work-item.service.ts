import type { ApiResult } from '@/shared/dtos/api-result.dto';
import type { PagedResult } from '@/shared/dtos/paged-result.dto';
import { mockApi } from '@/shared/services/mock-api';
import { mockWorkItems } from '../mock/work-items.mock';
import type { CreateWorkItemDto } from '../dtos/create-work-item.dto';
import type { UpdateWorkItemDto } from '../dtos/update-work-item.dto';
import type { Assignee, WorkItemDetailDto } from '../dtos/work-item-detail.dto';
import type { WorkItemFiltersDto } from '../dtos/work-item-filters.dto';
import type { WorkItemListItemDto } from '../dtos/work-item-list-item.dto';
import { UserSummaryDto } from "@/shared/dtos/user-summary.dto";

function toListItemDto(item: WorkItemDetailDto): WorkItemListItemDto {
    return {
        id: item.id,
        sprintId: item.sprintId,
        title: item.title,
        type: item.type,
        status: item.status,
        priority: item.priority,
        estimatedMinutes: item.estimatedMinutes,
        totalLoggedMinutes: item.totalLoggedMinutes,
        dueDate: item.dueDate,
        createdAt: item.createdAt,
        updatedAt: item.updatedAt,
        createdBy: item.createdBy,
        assignees: item.assignees.map((assignment: Assignee): UserSummaryDto => assignment.user),
        tags: item.tags
    };
}

function applyFilters(
    items: WorkItemDetailDto[],
    filters?: WorkItemFiltersDto
): WorkItemDetailDto[] {
    if (!filters) {
        return items;
    }

    return items.filter((item: WorkItemDetailDto): boolean => {
        const matchesSearch: boolean =
            !filters.search ||
            item.title.toLowerCase().includes(filters.search.toLowerCase()) ||
            (item.description ?? '').toLowerCase().includes(filters.search.toLowerCase());

        const matchesSprint: boolean = !filters.sprintId || item.sprintId === filters.sprintId;
        const matchesType: boolean = !filters.type || item.type === filters.type;
        const matchesStatus: boolean = !filters.status || item.status === filters.status;
        const matchesPriority: boolean = !filters.priority || item.priority === filters.priority;

        const matchesAssignee: boolean =
            !filters.assigneeUserId ||
            item.assignees.some((assignment) => assignment.user.id === filters.assigneeUserId);

        const matchesCreatedBy: boolean =
            !filters.createdByUserId || item.createdBy.id === filters.createdByUserId;

        const matchesTags: boolean =
            !filters.tagIds?.length ||
            filters.tagIds.every((tagId) => item.tags.some((tag) => tag.id === tagId));

        return (
            matchesSearch &&
            matchesSprint &&
            matchesType &&
            matchesStatus &&
            matchesPriority &&
            matchesAssignee &&
            matchesCreatedBy &&
            matchesTags
        );
    });
}

export const workItemService = {
    async getWorkItems(
        filters?: WorkItemFiltersDto
    ): Promise<ApiResult<PagedResult<WorkItemListItemDto>>> {
        const filtered: WorkItemDetailDto[] = applyFilters(mockWorkItems, filters);
        const page = filters?.page ?? 1;
        const pageSize = filters?.pageSize ?? 10;

        const start = (page - 1) * pageSize;
        const end = start + pageSize;
        const pagedItems: WorkItemListItemDto[] = filtered.slice(start, end).map(toListItemDto);

        return mockApi({
            items: pagedItems,
            total: filtered.length,
            page,
            pageSize,
            totalPages: Math.max(1, Math.ceil(filtered.length / pageSize))
        });
    },

    async getWorkItemById(id: string): Promise<ApiResult<WorkItemDetailDto | null>> {
        const found = mockWorkItems.find((item) => item.id === id) ?? null;

        return mockApi(found);
    },

    async createWorkItem(input: CreateWorkItemDto): Promise<ApiResult<WorkItemDetailDto>> {
        const now = new Date().toISOString();

        const created: WorkItemDetailDto = {
            id: `wrk-${crypto.randomUUID()}`,
            sprintId: input.sprintId,
            title: input.title,
            description: input.description,
            type: input.type,
            status: input.status ?? 'TODO',
            priority: input.priority,
            externalLink: input.externalLink,
            estimatedMinutes: input.estimatedMinutes,
            totalLoggedMinutes: 0,
            dueDate: input.dueDate,
            createdAt: now,
            updatedAt: now,
            createdBy: {
                id: 'usr-001',
                name: 'Bernardo Manager',
                email: 'bernardo.manager@demo.com',
                telegramUserId: 'tg_bernardo_manager'
            },
            assignees: [],
            tags: [],
            featureDetails: input.featureDetails,
            issueDetails: input.issueDetails,
            bugDetails: input.bugDetails
        };

        mockWorkItems.unshift(created);

        return mockApi(created);
    },

    async updateWorkItem(
        id: string,
        input: UpdateWorkItemDto
    ): Promise<ApiResult<WorkItemDetailDto | null>> {
        const index = mockWorkItems.findIndex((item) => item.id === id);

        if (index === -1) {
            return mockApi(null);
        }

        const current: WorkItemDetailDto = mockWorkItems[index];

        const updated: WorkItemDetailDto = {
            ...current,
            ...input,
            updatedAt: new Date().toISOString(),
            featureDetails: input.featureDetails ?? current.featureDetails,
            issueDetails: input.issueDetails ?? current.issueDetails,
            bugDetails: input.bugDetails ?? current.bugDetails
        };

        mockWorkItems[index] = updated;

        return mockApi(updated);
    }
};