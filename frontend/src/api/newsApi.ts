import apiClient from './apiClient';
import type { NewsArticle, NewsSearchParams, PaginatedResponse } from '@/types';

/**
 * Fetches paginated news articles matching the given search criteria.
 * @param params - Search parameters (symbols, sources, sentiment, date range, pagination)
 * @returns Paginated list of news articles
 */
export async function fetchNewsArticles(
  params: NewsSearchParams,
): Promise<PaginatedResponse<NewsArticle>> {
  const queryParams = new URLSearchParams();

  if (params.symbols?.length) {
    queryParams.set('symbols', params.symbols.join(','));
  }
  if (params.source) {
    queryParams.set('source', params.source);
  }
  if (params.sentiment) {
    queryParams.set('sentiment', params.sentiment);
  }
  if (params.fromDate) {
    queryParams.set('fromDate', params.fromDate);
  }
  if (params.toDate) {
    queryParams.set('toDate', params.toDate);
  }
  if (params.page !== undefined) {
    queryParams.set('page', params.page.toString());
  }
  if (params.size !== undefined) {
    queryParams.set('size', params.size.toString());
  }

  const response = await apiClient.get<PaginatedResponse<NewsArticle>>(
    `/news?${queryParams.toString()}`,
  );
  return response.data;
}

/**
 * Fetches a single news article by its ID.
 * @param articleId - The unique article ID
 * @returns The full news article details
 */
export async function fetchNewsArticleById(articleId: number): Promise<NewsArticle> {
  const response = await apiClient.get<NewsArticle>(`/news/${articleId}`);
  return response.data;
}

/**
 * Fetches trending news articles across all tracked symbols.
 * @param page - Page number (zero-based, default: 0)
 * @param size - Results per page (default: 20)
 * @returns Paginated list of trending articles
 */
export async function fetchTrendingNews(page = 0, size = 20): Promise<PaginatedResponse<NewsArticle>> {
  const response = await apiClient.get<PaginatedResponse<NewsArticle>>(
    `/news/trending?page=${page}&size=${size}`,
  );
  return response.data;
}

/**
 * Triggers a manual news fetch for the given stock symbols.
 * @param symbols - Array of stock ticker symbols to fetch news for
 * @returns Confirmation message
 */
export async function triggerNewsFetch(symbols: string[]): Promise<string> {
  const response = await apiClient.post<string>('/news/fetch', symbols);
  return response.data;
}
