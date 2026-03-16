import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { fetchNewsArticles, fetchTrendingNews } from '@/api/newsApi';
import type { NewsSearchParams } from '@/types';

/** Cache key prefix for news queries. */
const NEWS_QUERY_KEY = 'news';

/** Cache key prefix for trending queries. */
const TRENDING_QUERY_KEY = 'trending-news';

/**
 * TanStack Query hook for searching news articles with filters.
 * Automatically refetches when parameters change.
 * Uses keepPreviousData to prevent layout shifts during pagination.
 *
 * @param params - Search parameters (symbols, source, sentiment, dates, pagination)
 * @returns Query result with data, loading state, and error
 */
export function useNewsSearch(params: NewsSearchParams) {
  return useQuery({
    queryKey: [NEWS_QUERY_KEY, params],
    queryFn: () => fetchNewsArticles(params),
    placeholderData: keepPreviousData,
    staleTime: 2 * 60 * 1000, // 2 minutes
    refetchOnWindowFocus: false,
  });
}

/**
 * TanStack Query hook for fetching trending news articles.
 * Automatically refetches every 5 minutes.
 *
 * @param page - Page number (zero-based)
 * @param size - Results per page
 * @returns Query result with trending articles
 */
export function useTrendingNews(page = 0, size = 20) {
  return useQuery({
    queryKey: [TRENDING_QUERY_KEY, page, size],
    queryFn: () => fetchTrendingNews(page, size),
    placeholderData: keepPreviousData,
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchInterval: 5 * 60 * 1000, // Auto-refresh every 5 minutes
    refetchOnWindowFocus: false,
  });
}
