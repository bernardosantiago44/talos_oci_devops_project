import type { ApiResult } from '@/shared/dtos/api-result.dto';
import type { WorkItemDetailDto, Assignee } from '../dtos/work-item-detail.dto';
import type { WorkItemType } from '../enums/work-item-type.enum';
import type { WorkItemPriority } from '../enums/work-item-priority.enum';
import { normalizeStatus } from '../enums/work-item-status.enum';
import { apiClient } from '@/shared/services/api-client';

// ─── Backend response types ──────────────────────────────────────

interface BackendSearchResultItem {
    workItem: {
        workItemId: string;
        title: string;
        description?: string;
        status: string;
        priority: string;
        workType: string;
        dueDate?: string;
        createdAt: string;
        estimatedMinutes?: number;
        sprintId?: string;
        assignees?: Assignee[];
    };
    relevanceScore: number;
}

interface BackendSearchResponse {
    query: string;
    totalResults: number;
    aiAvailable: boolean;
    fallbackMessage: string | null;
    results: BackendSearchResultItem[];
}

// ─── Frontend types ──────────────────────────────────────────────

export interface SemanticSearchResult {
    workItem: WorkItemDetailDto;
    relevanceScore: number;
}

export interface SemanticSearchResponse {
    query: string;
    totalResults: number;
    aiAvailable: boolean;
    fallbackMessage: string | null;
    results: SemanticSearchResult[];
}

// ─── Mapper ──────────────────────────────────────────────────────

function mapSearchResult(item: BackendSearchResultItem): SemanticSearchResult {
    const wi = item.workItem;
    return {
        workItem: {
            id: wi.workItemId,
            sprintId: wi.sprintId,
            title: wi.title,
            description: wi.description ?? undefined,
            type: (wi.workType ?? 'TASK') as WorkItemType,
            status: normalizeStatus(wi.status),
            priority: (wi.priority ?? 'MEDIUM') as WorkItemPriority,
            estimatedMinutes: wi.estimatedMinutes ?? undefined,
            totalLoggedMinutes: 0,
            dueDate: wi.dueDate ? String(wi.dueDate).slice(0, 10) : undefined,
            createdAt: wi.createdAt,
            updatedAt: wi.createdAt,
            createdBy: { userId: 'system', name: 'System' },
            assignees: wi.assignees ?? [],
            tags: [],
        },
        relevanceScore: item.relevanceScore,
    };
}

// ─── Service ─────────────────────────────────────────────────────

export const semanticSearchService = {
    /**
     * Performs a semantic search for work items matching the given query.
     *
     * @param query       Natural language search query
     * @param maxResults  Maximum number of results (defaults to 10)
     */
    async search(
        query: string,
        maxResults: number = 10
    ): Promise<ApiResult<SemanticSearchResponse>> {
        const result = await apiClient.post<BackendSearchResponse>(
            '/api/semantic-search',
            { query, maxResults }
        );

        if (!result.success) {
            return {
                success: false,
                data: {
                    query,
                    totalResults: 0,
                    aiAvailable: false,
                    fallbackMessage: result.message ?? 'Search failed',
                    results: [],
                },
                message: result.message,
            };
        }

        const mapped: SemanticSearchResponse = {
            query: result.data.query,
            totalResults: result.data.totalResults,
            aiAvailable: result.data.aiAvailable,
            fallbackMessage: result.data.fallbackMessage,
            results: result.data.results.map(mapSearchResult),
        };

        return { success: true, data: mapped };
    },

    /**
     * Triggers a re-index of all work items.
     */
    async reindex(): Promise<ApiResult<string>> {
        return apiClient.post<string>('/api/semantic-search/reindex', {});
    },
};
