import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchSpaces,
  fetchSpaceById,
  createSpace,
  updateSpace,
  deleteSpace,
  fetchArticlesInSpace,
  saveArticleToSpace,
  removeArticleFromSpace,
} from '@/api/spacesApi';
import type { SpaceRequest, SaveArticleRequest } from '@/types';

/** Cache key for all spaces list. */
const SPACES_KEY = 'spaces';

/** Cache key builder for a single space. */
const spaceKey = (id: number) => ['space', id];

/** Cache key builder for articles in a space. */
const spaceArticlesKey = (spaceId: number) => ['space-articles', spaceId];

/**
 * TanStack Query hook for fetching all spaces.
 * @returns Query result with spaces list
 */
export function useSpaces() {
  return useQuery({
    queryKey: [SPACES_KEY],
    queryFn: fetchSpaces,
    staleTime: 60 * 1000, // 1 minute
  });
}

/**
 * TanStack Query hook for fetching a single space.
 * @param spaceId - The space ID to fetch
 * @returns Query result with space details
 */
export function useSpace(spaceId: number) {
  return useQuery({
    queryKey: spaceKey(spaceId),
    queryFn: () => fetchSpaceById(spaceId),
    enabled: spaceId > 0,
  });
}

/**
 * TanStack Query hook for fetching articles in a space.
 * @param spaceId - The space ID
 * @returns Query result with saved articles list
 */
export function useSpaceArticles(spaceId: number) {
  return useQuery({
    queryKey: spaceArticlesKey(spaceId),
    queryFn: () => fetchArticlesInSpace(spaceId),
    enabled: spaceId > 0,
  });
}

/**
 * Mutation hook for creating a new space.
 * Invalidates the spaces list on success.
 * @returns Mutation object for creating spaces
 */
export function useCreateSpace() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: SpaceRequest) => createSpace(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [SPACES_KEY] });
    },
  });
}

/**
 * Mutation hook for updating a space.
 * Invalidates both the spaces list and the specific space cache.
 * @returns Mutation object for updating spaces
 */
export function useUpdateSpace() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ spaceId, request }: { spaceId: number; request: SpaceRequest }) =>
      updateSpace(spaceId, request),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: [SPACES_KEY] });
      queryClient.invalidateQueries({ queryKey: spaceKey(variables.spaceId) });
    },
  });
}

/**
 * Mutation hook for deleting a space.
 * Invalidates the spaces list on success.
 * @returns Mutation object for deleting spaces
 */
export function useDeleteSpace() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (spaceId: number) => deleteSpace(spaceId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [SPACES_KEY] });
    },
  });
}

/**
 * Mutation hook for saving an article to a space.
 * Invalidates the space articles and the spaces list (article count changes).
 * @returns Mutation object for saving articles
 */
export function useSaveArticle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ spaceId, request }: { spaceId: number; request: SaveArticleRequest }) =>
      saveArticleToSpace(spaceId, request),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: spaceArticlesKey(variables.spaceId) });
      queryClient.invalidateQueries({ queryKey: [SPACES_KEY] });
    },
  });
}

/**
 * Mutation hook for removing an article from a space.
 * Invalidates the space articles and the spaces list (article count changes).
 * @returns Mutation object for removing articles
 */
export function useRemoveArticle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ spaceId, articleId }: { spaceId: number; articleId: number }) =>
      removeArticleFromSpace(spaceId, articleId),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: spaceArticlesKey(variables.spaceId) });
      queryClient.invalidateQueries({ queryKey: [SPACES_KEY] });
    },
  });
}
