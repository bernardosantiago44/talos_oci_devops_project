import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import { reindex, search } from '@/api/generated';
import { apiQueryKeys } from './query-keys';
import { readData } from './request';
export function useSemanticSearch() {
    return useMutation({
        mutationFn: (body) => readData(search({ client: apiClient, body, throwOnError: true })),
    });
}
export function useSemanticSearchReindex() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: () => readData(reindex({ client: apiClient, throwOnError: true })),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.all });
        },
    });
}
