import { normalizeStatus } from '../enums/work-item-status.enum';
import { apiClient } from '@/shared/services/api-client';
// ─── Mapper ──────────────────────────────────────────────────────
function mapSearchResult(item) {
    const wi = item.workItem;
    return {
        workItem: {
            id: wi.workItemId,
            sprintId: wi.sprintId,
            title: wi.title,
            description: wi.description ?? undefined,
            type: (wi.workType ?? 'TASK'),
            status: normalizeStatus(wi.status),
            priority: (wi.priority ?? 'MEDIUM'),
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
    async search(query, maxResults = 10) {
        const result = await apiClient.post('/api/semantic-search', { query, maxResults });
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
        const mapped = {
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
    async reindex() {
        return apiClient.post('/api/semantic-search/reindex', {});
    },
};
