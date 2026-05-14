import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import { logTime } from '@/api/generated';
import { apiQueryKeys } from './query-keys';
import { readData } from './request';
export function useTimeEntryCreate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (body) => readData(logTime({ client: apiClient, body, throwOnError: true })),
        onSuccess: (_data, variables) => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.detail(variables.workItemId) }).then();
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.list() }).then();
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.analytics.all }).then();
        },
    });
}
