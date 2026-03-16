import { useState } from 'react';
import { TrendingUp } from 'lucide-react';
import { useTrendingNews } from '@/hooks/useNewsSearch';
import { NewsList } from '@/components/news';

/** Default page size for trending results. */
const DEFAULT_PAGE_SIZE = 12;

/**
 * Trending news page showing the most recent articles across all tracked symbols.
 * Displays articles published in the last 24 hours, auto-refreshes every 5 minutes.
 */
export function TrendingPage() {
  const [currentPage, setCurrentPage] = useState(0);
  const { data, isLoading, isFetching, error, refetch } = useTrendingNews(currentPage, DEFAULT_PAGE_SIZE);

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-orange-100 dark:bg-orange-900/30">
          <TrendingUp className="h-5 w-5 text-orange-600 dark:text-orange-400" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Trending</h1>
          <p className="mt-0.5 text-sm text-gray-500 dark:text-gray-400">
            Most recent articles from the last 24 hours
          </p>
        </div>
      </div>

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
        emptyTitle="No trending articles"
        emptyDescription="No articles have been published in the last 24 hours. Try fetching news from the News Feed page first."
      />
    </div>
  );
}
