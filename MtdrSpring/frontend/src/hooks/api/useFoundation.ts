import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import { createSprint, deleteSprint, getAll, getAll1, getSprint } from '@/api/generated';
import type { SprintCreateRequest } from '@/api/generated';
import { apiQueryKeys } from './query-keys';
import { readData } from './request';

export function useSprintList() {
  return useQuery({
    queryKey: apiQueryKeys.sprints.list(),
    queryFn: () => readData(getAll({ client: apiClient, throwOnError: true })),
  });
}

export function useSprintGet(id?: string) {
  return useQuery({
    queryKey: apiQueryKeys.sprints.detail(id),
    queryFn: () =>
      readData(getSprint({
        client: apiClient,
        path: { id: id as string },
        throwOnError: true,
      })),
    enabled: Boolean(id),
  });
}

export function useSprintCreate() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: SprintCreateRequest) =>
      readData(createSprint({ client: apiClient, body, throwOnError: true })),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.sprints.all });
    },
  });
}

export function useSprintDelete() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) =>
      deleteSprint({ client: apiClient, path: { id }, throwOnError: true }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.sprints.all });
    },
  });
}

export function useAppUserList() {
  return useQuery({
    queryKey: apiQueryKeys.appUsers.list(),
    queryFn: () => readData(getAll1({ client: apiClient, throwOnError: true })),
  });
}
