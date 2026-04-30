import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import { addAssignee, createWorkItem, deleteWorkItem, getAllWorkItems, getAssignees, getById, getWorkItemsByTelegramUser, removeAssignee, updateWorkItem, } from '@/api/generated';
import { apiQueryKeys } from './query-keys';
import { readData } from './request';
export function useWorkItemList() {
    return useQuery({
        queryKey: apiQueryKeys.workItems.list(),
        queryFn: () => readData(getAllWorkItems({ client: apiClient, throwOnError: true })),
    });
}
export function useWorkItemGet(id) {
    return useQuery({
        queryKey: apiQueryKeys.workItems.detail(id),
        queryFn: () => readData(getById({
            client: apiClient,
            path: { id: id },
            throwOnError: true,
        })),
        enabled: Boolean(id),
    });
}
export function useWorkItemListByTelegramUser(telegramUserId) {
    return useQuery({
        queryKey: apiQueryKeys.workItems.byTelegramUser(telegramUserId),
        queryFn: () => readData(getWorkItemsByTelegramUser({
            client: apiClient,
            path: { telegramUserId: telegramUserId },
            throwOnError: true,
        })),
        enabled: Boolean(telegramUserId),
    });
}
export function useWorkItemAssigneeList(id) {
    return useQuery({
        queryKey: apiQueryKeys.workItems.assignees(id),
        queryFn: () => readData(getAssignees({
            client: apiClient,
            path: { id: id },
            throwOnError: true,
        })),
        enabled: Boolean(id),
    });
}
export function useWorkItemCreate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (body) => readData(createWorkItem({ client: apiClient, body, throwOnError: true })),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.all });
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.analytics.all });
        },
    });
}
export function useWorkItemUpdate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, body }) => readData(updateWorkItem({
            client: apiClient,
            path: { id },
            body,
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
        mutationFn: (id) => readData(deleteWorkItem({ client: apiClient, path: { id }, throwOnError: true })),
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
        mutationFn: ({ id, userId }) => readData(addAssignee({ client: apiClient, path: { id, userId }, throwOnError: true })),
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
        mutationFn: ({ id, userId }) => readData(removeAssignee({ client: apiClient, path: { id, userId }, throwOnError: true })),
        onSuccess: (_data, variables) => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.all });
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.detail(variables.id) });
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.assignees(variables.id) });
        },
    });
}
