import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import {
  addAssignee,
  createWorkItem,
  deleteWorkItem,
  getAllWorkItems,
  getAssignees,
  getById,
  getWorkItemsByTelegramUser,
  removeAssignee,
  updateWorkItem,
} from '@/api/generated';
import type { CreateWorkItemRequest, UpdateWorkItemRequest } from '@/api/generated';
import { apiQueryKeys } from './query-keys';
import { readData } from './request';

type CreateWorkItemPayload = Omit<CreateWorkItemRequest, 'priority'> & {
  priority: string;
  tagIds?: string[];
};

type UpdateWorkItemPayload = Omit<UpdateWorkItemRequest, 'priority'> & {
  priority?: string;
  tagIds?: string[];
};

export function useWorkItemList() {
  return useQuery({
    queryKey: apiQueryKeys.workItems.list(),
    queryFn: () => readData(getAllWorkItems({ client: apiClient, throwOnError: true })),
  });
}

export function useWorkItemGet(id?: string) {
  return useQuery({
    queryKey: apiQueryKeys.workItems.detail(id),
    queryFn: () =>
      readData(getById({
        client: apiClient,
        path: { id: id as string },
        throwOnError: true,
      })),
    enabled: Boolean(id),
  });
}

export function useWorkItemListByTelegramUser(telegramUserId?: string) {
  return useQuery({
    queryKey: apiQueryKeys.workItems.byTelegramUser(telegramUserId),
    queryFn: () =>
      readData(getWorkItemsByTelegramUser({
        client: apiClient,
        path: { telegramUserId: telegramUserId as string },
        throwOnError: true,
      })),
    enabled: Boolean(telegramUserId),
  });
}

export function useWorkItemAssigneeList(id?: string) {
  return useQuery({
    queryKey: apiQueryKeys.workItems.assignees(id),
    queryFn: () =>
      readData(getAssignees({
        client: apiClient,
        path: { id: id as string },
        throwOnError: true,
      })),
    enabled: Boolean(id),
  });
}

export function useWorkItemCreate() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: CreateWorkItemPayload) =>
      readData(createWorkItem({ client: apiClient, body: body as CreateWorkItemRequest, throwOnError: true })),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.all });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.analytics.all });
    },
  });
}

export function useWorkItemUpdate() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateWorkItemPayload }) =>
      readData(updateWorkItem({
        client: apiClient,
        path: { id },
        body: body as UpdateWorkItemRequest,
        throwOnError: true,
      })),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.all });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.detail(variables.id) });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.analytics.all });
    },
  });
}

export function useWorkItemDelete() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) =>
      readData(deleteWorkItem({ client: apiClient, path: { id }, throwOnError: true })),
    onSuccess: (_data, id) => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.all });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.detail(id) });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.analytics.all });
    },
  });
}

export function useWorkItemAssigneeAdd() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, userId }: { id: string; userId: string }) =>
      readData(addAssignee({ client: apiClient, path: { id, userId }, throwOnError: true })),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.all });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.detail(variables.id) });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.assignees(variables.id) });
    },
  });
}

export function useWorkItemAssigneeRemove() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, userId }: { id: string; userId: string }) =>
      readData(removeAssignee({ client: apiClient, path: { id, userId }, throwOnError: true })),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.all });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.detail(variables.id) });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.assignees(variables.id) });
    },
  });
}
