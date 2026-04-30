import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import { addToDoItem, deleteToDoItem, getAllToDoItems, getToDoItemById, updateToDoItem, } from '@/api/generated';
import { apiQueryKeys } from './query-keys';
import { readData } from './request';
export function useTodoList() {
    return useQuery({
        queryKey: apiQueryKeys.todos.list(),
        queryFn: () => readData(getAllToDoItems({ client: apiClient, throwOnError: true })),
    });
}
export function useTodoGet(id) {
    return useQuery({
        queryKey: apiQueryKeys.todos.detail(id),
        queryFn: () => readData(getToDoItemById({
            client: apiClient,
            path: { id: id },
            throwOnError: true,
        })),
        enabled: id !== undefined,
    });
}
export function useTodoCreate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (body) => readData(addToDoItem({ client: apiClient, body, throwOnError: true })),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.all });
        },
    });
}
export function useTodoUpdate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, body }) => readData(updateToDoItem({
            client: apiClient,
            path: { id },
            body,
            throwOnError: true,
        })),
        onSuccess: (_data, variables) => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.all });
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.detail(variables.id) });
        },
    });
}
export function useTodoDelete() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id) => readData(deleteToDoItem({ client: apiClient, path: { id }, throwOnError: true })),
        onSuccess: (_data, id) => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.all });
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.detail(id) });
        },
    });
}
