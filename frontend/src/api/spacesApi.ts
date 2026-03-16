import apiClient from './apiClient';
import type { Space, SpaceRequest, SavedArticle, SaveArticleRequest } from '@/types';

/**
 * Fetches all spaces for the current user.
 * @returns Array of spaces ordered by creation date (newest first)
 */
export async function fetchSpaces(): Promise<Space[]> {
  const response = await apiClient.get<Space[]>('/spaces');
  return response.data;
}

/**
 * Fetches a single space by ID.
 * @param spaceId - The space ID
 * @returns The space details
 */
export async function fetchSpaceById(spaceId: number): Promise<Space> {
  const response = await apiClient.get<Space>(`/spaces/${spaceId}`);
  return response.data;
}

/**
 * Creates a new space (reading list).
 * @param request - The space name and optional description
 * @returns The created space
 */
export async function createSpace(request: SpaceRequest): Promise<Space> {
  const response = await apiClient.post<Space>('/spaces', request);
  return response.data;
}

/**
 * Updates an existing space's name and/or description.
 * @param spaceId - The space ID to update
 * @param request - The updated space details
 * @returns The updated space
 */
export async function updateSpace(spaceId: number, request: SpaceRequest): Promise<Space> {
  const response = await apiClient.put<Space>(`/spaces/${spaceId}`, request);
  return response.data;
}

/**
 * Deletes a space and all its saved articles.
 * @param spaceId - The space ID to delete
 */
export async function deleteSpace(spaceId: number): Promise<void> {
  await apiClient.delete(`/spaces/${spaceId}`);
}

/**
 * Fetches all saved articles in a space.
 * @param spaceId - The space ID
 * @returns Array of saved articles ordered by save date (newest first)
 */
export async function fetchArticlesInSpace(spaceId: number): Promise<SavedArticle[]> {
  const response = await apiClient.get<SavedArticle[]>(`/spaces/${spaceId}/articles`);
  return response.data;
}

/**
 * Saves an article to a space.
 * @param spaceId - The space ID
 * @param request - The article data to save
 * @returns The saved article
 */
export async function saveArticleToSpace(
  spaceId: number,
  request: SaveArticleRequest,
): Promise<SavedArticle> {
  const response = await apiClient.post<SavedArticle>(`/spaces/${spaceId}/articles`, request);
  return response.data;
}

/**
 * Removes a saved article from a space.
 * @param spaceId - The space ID
 * @param articleId - The saved article ID
 */
export async function removeArticleFromSpace(spaceId: number, articleId: number): Promise<void> {
  await apiClient.delete(`/spaces/${spaceId}/articles/${articleId}`);
}
