import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/api/client';
import { createTag, deleteTag, get as getTags, updateTag } from '@/api/generated';
import type { CreateTagRequest, TagResponse, UpdateTagRequest } from '@/api/generated';
import type { TagDto } from '@/shared/dtos/tag.dto';
import { apiQueryKeys } from './query-keys';
import { readData } from './request';

function mapTag(response: TagResponse): TagDto | null {
  if (!response.tagId || !response.name) return null;

  return {
    id: response.tagId,
    name: response.name,
    color: response.color ?? '#3B82F6',
    description: response.description,
  };
}

function requireTag(response: TagResponse): TagDto {
  const tag = mapTag(response);
  if (!tag) throw new Error('Tag response did not include a tagId and name.');
  return tag;
}

export function useTagList() {
  return useQuery({
    queryKey: apiQueryKeys.tags.list(),
    queryFn: async () => {
      const tags = await readData(getTags({ client: apiClient, throwOnError: true }));
      return tags.map(mapTag).filter((tag): tag is TagDto => Boolean(tag));
    },
  });
}

export function useTagCreate() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (body: CreateTagRequest) => {
      const tag = await readData(createTag({ client: apiClient, body, throwOnError: true }));
      return requireTag(tag);
    },
    onSuccess: (tag) => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.tags.all });
      queryClient.setQueryData<TagDto[]>(apiQueryKeys.tags.list(), (current) => {
        if (!current) return [tag];
        if (current.some((item) => item.id === tag.id)) return current;
        return [...current, tag];
      });
    },
  });
}

export function useTagUpdate() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, body }: { id: string; body: UpdateTagRequest }) => {
      const tag = await readData(updateTag({ client: apiClient, path: { id }, body, throwOnError: true }));
      return requireTag(tag);
    },
    onSuccess: (tag) => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.tags.all });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.tags.detail(tag.id) });
      queryClient.setQueryData<TagDto[]>(apiQueryKeys.tags.list(), (current) => {
        if (!current) return [tag];
        return current.map((item) => (item.id === tag.id ? tag : item));
      });
    },
  });
}

export function useTagDelete() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      await deleteTag({ client: apiClient, path: { id }, throwOnError: true });
      return id;
    },
    onSuccess: (id) => {
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.tags.all });
      queryClient.invalidateQueries({ queryKey: apiQueryKeys.workItems.all });
      queryClient.removeQueries({ queryKey: apiQueryKeys.tags.detail(id) });
      queryClient.setQueryData<TagDto[]>(apiQueryKeys.tags.list(), (current) =>
        current?.filter((item) => item.id !== id) ?? []
      );
    },
  });
}
