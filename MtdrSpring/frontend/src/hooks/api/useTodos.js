import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiQueryKeys } from './query-keys';
function missingTodoApi() {
    throw new Error('Legacy todo endpoints are not present in the generated OpenAPI client.');
}
export function useTodoList() {
    return useQuery({
        queryKey: apiQueryKeys.todos.list(),
        queryFn: missingTodoApi,
        enabled: false,
    });
}
export function useTodoGet(id) {
    return useQuery({
        queryKey: apiQueryKeys.todos.detail(id),
        queryFn: missingTodoApi,
        enabled: false,
    });
}
export function useTodoCreate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (_body) => missingTodoApi(),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.all });
        },
    });
}
export function useTodoUpdate() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (_variables) => missingTodoApi(),
        onSuccess: (_data, variables) => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.all });
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.detail(variables.id) });
        },
    });
}
export function useTodoDelete() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (_id) => missingTodoApi(),
        onSuccess: (_data, id) => {
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.all });
            queryClient.invalidateQueries({ queryKey: apiQueryKeys.todos.detail(id) });
        },
    });
}
