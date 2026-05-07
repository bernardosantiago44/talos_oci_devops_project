import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import { getAll, getAll1, getSprint } from '@/api/generated';
import { apiQueryKeys } from './query-keys';
import { readData } from './request';
export function useSprintList() {
    return useQuery({
        queryKey: apiQueryKeys.sprints.list(),
        queryFn: () => readData(getAll({ client: apiClient, throwOnError: true })),
    });
}
export function useSprintGet(id) {
    return useQuery({
        queryKey: apiQueryKeys.sprints.detail(id),
        queryFn: () => readData(getSprint({
            client: apiClient,
            path: { id: id },
            throwOnError: true,
        })),
        enabled: Boolean(id),
    });
}
export function useAppUserList() {
    return useQuery({
        queryKey: apiQueryKeys.appUsers.list(),
        queryFn: () => readData(getAll1({ client: apiClient, throwOnError: true })),
    });
}
