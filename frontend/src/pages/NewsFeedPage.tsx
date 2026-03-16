import { useState, useCallback } from 'react';
import { RefreshCw } from 'lucide-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNewsSearch } from '@/hooks/useNewsSearch';
import { triggerNewsFetch } from '@/api/newsApi';
import { NewsSearchBar, NewsList, NewsFilters } from '@/components/news';
import type { Sentiment, NewsSource } from '@/types';

/** Default page size for news results. */
const DEFAULT_PAGE_SIZE = 12;

/**
 * Main news feed page.
 * Provides search by stock symbol, filtering by sentiment/source/date,
 * and displays results in a paginated card grid.
 * Includes a "Fetch News" button to manually trigger news retrieval.
 */
export function NewsFeedPage() {
  const queryClient = useQueryClient();

  // Search state
  const [selectedSymbols, setSelectedSymbols] = useState<string[]>([]);
  const [currentPage, setCurrentPage] = useState(0);

  // Filter state
  const [selectedSentiment, setSelectedSentiment] = useState<Sentiment | null>(null);
  const [selectedSource, setSelectedSource] = useState<NewsSource | null>(null);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  /** Build search params from current filter state. */
  const searchParams = {
    symbols: selectedSymbols.length > 0 ? selectedSymbols : undefined,
    sentiment: selectedSentiment ?? undefined,
    source: selectedSource ?? undefined,
    fromDate: fromDate || undefined,
    toDate: toDate || undefined,
    page: currentPage,
    size: DEFAULT_PAGE_SIZE,
  };

  const { data, isLoading, isFetching, error, refetch } = useNewsSearch(searchParams);

  /** Manual news fetch mutation. */
  const fetchMutation = useMutation({
    mutationFn: triggerNewsFetch,
    onSuccess: () => {
      // Invalidate news queries to show fresh data
      queryClient.invalidateQueries({ queryKey: ['news'] });
    },
  });

  /** Reset page to 0 when filters change. */
  const handleSentimentChange = useCallback((sentiment: Sentiment | null) => {
    setSelectedSentiment(sentiment);
    setCurrentPage(0);
  }, []);

  const handleSourceChange = useCallback((source: NewsSource | null) => {
    setSelectedSource(source);
    setCurrentPage(0);
  }, []);

  const handleFromDateChange = useCallback((date: string) => {
    setFromDate(date);
    setCurrentPage(0);
  }, []);

  const handleToDateChange = useCallback((date: string) => {
    setToDate(date);
    setCurrentPage(0);
  }, []);

  const handleSymbolsChange = useCallback((symbols: string[]) => {
    setSelectedSymbols(symbols);
    setCurrentPage(0);
  }, []);

  const handleClearAllFilters = useCallback(() => {
    setSelectedSentiment(null);
    setSelectedSource(null);
    setFromDate('');
    setToDate('');
    setCurrentPage(0);
  }, []);

  /** Triggers a manual fetch for the currently selected symbols. */
  const handleFetchNews = useCallback(() => {
    if (selectedSymbols.length > 0) {
      fetchMutation.mutate(selectedSymbols);
    }
  }, [selectedSymbols, fetchMutation]);

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">News Feed</h1>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Latest stock market news from multiple sources
          </p>
        </div>

        {/* Fetch button -- only shown when symbols are selected */}
        {selectedSymbols.length > 0 && (
          <button
            onClick={handleFetchNews}
            disabled={fetchMutation.isPending}
            className="flex w-full items-center justify-center gap-2 rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-700 disabled:opacity-50 sm:w-auto dark:bg-primary-500 dark:hover:bg-primary-600"
          >
            <RefreshCw className={`h-4 w-4 ${fetchMutation.isPending ? 'animate-spin' : ''}`} />
            {fetchMutation.isPending ? 'Fetching...' : 'Fetch Latest'}
          </button>
        )}
      </div>

      {/* Fetch success/error message */}
      {fetchMutation.isSuccess && (
        <div className="rounded-lg border border-green-200 bg-green-50 px-4 py-2 text-sm text-green-700 dark:border-green-900/50 dark:bg-green-950/30 dark:text-green-400">
          News fetch completed. Results should appear below shortly.
        </div>
      )}
      {fetchMutation.isError && (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700 dark:border-red-900/50 dark:bg-red-950/30 dark:text-red-400">
          Failed to fetch news. Please try again.
        </div>
      )}

      {/* Search bar */}
      <NewsSearchBar
        selectedSymbols={selectedSymbols}
        onSymbolsChange={handleSymbolsChange}
      />

      {/* Filters */}
      <NewsFilters
        selectedSentiment={selectedSentiment}
        selectedSource={selectedSource}
        fromDate={fromDate}
        toDate={toDate}
        onSentimentChange={handleSentimentChange}
        onSourceChange={handleSourceChange}
        onFromDateChange={handleFromDateChange}
        onToDateChange={handleToDateChange}
        onClearAll={handleClearAllFilters}
      />

      {/* Results */}
      <NewsList
        articles={data?.data ?? []}
        isLoading={isLoading}
        isFetching={isFetching && !isLoading}
        errorMessage={error ? (error as Error).message : undefined}
        onRetry={() => refetch()}
        currentPage={currentPage}
        totalPages={data?.totalPages ?? 0}
        totalElements={data?.totalElements ?? 0}
        onPageChange={setCurrentPage}
      />
    </div>
  );
}
