import { Newspaper } from 'lucide-react';
import type { NewsArticle } from '@/types';
import { LoadingSpinner, EmptyState, ErrorBanner, Pagination } from '@/components/ui';
import { NewsCard } from './NewsCard';

interface NewsListProps {
  /** Array of news articles to display */
  articles: NewsArticle[];
  /** Whether data is currently loading */
  isLoading: boolean;
  /** Whether this is a background refetch (not initial load) */
  isFetching?: boolean;
  /** Error message if the fetch failed */
  errorMessage?: string;
  /** Retry callback for the error banner */
  onRetry?: () => void;
  /** Current page number (zero-based) */
  currentPage: number;
  /** Total number of pages */
  totalPages: number;
  /** Total number of elements */
  totalElements: number;
  /** Callback when page changes */
  onPageChange: (page: number) => void;
  /** Custom empty state message */
  emptyTitle?: string;
  /** Custom empty state description */
  emptyDescription?: string;
}

/**
 * Renders a grid of news article cards with loading, error, and empty states.
 * Includes pagination controls at the bottom.
 *
 * @param articles - News articles to render
 * @param isLoading - Whether data is loading (shows spinner)
 * @param isFetching - Whether a background refetch is happening (shows subtle indicator)
 * @param errorMessage - Error message if fetch failed
 * @param onRetry - Retry callback for error state
 * @param currentPage - Zero-based current page
 * @param totalPages - Total pages available
 * @param totalElements - Total results count
 * @param onPageChange - Page change callback
 * @param emptyTitle - Custom title for empty state
 * @param emptyDescription - Custom description for empty state
 */
export function NewsList({
  articles,
  isLoading,
  isFetching = false,
  errorMessage,
  onRetry,
  currentPage,
  totalPages,
  totalElements,
  onPageChange,
  emptyTitle = 'No articles found',
  emptyDescription = 'Try adjusting your search filters or check back later for new articles.',
}: NewsListProps) {
  if (isLoading) {
    return <LoadingSpinner size="large" message="Loading articles..." />;
  }

  if (errorMessage) {
    return <ErrorBanner message={errorMessage} onRetry={onRetry} />;
  }

  if (articles.length === 0) {
    return (
      <EmptyState
        icon={Newspaper}
        title={emptyTitle}
        description={emptyDescription}
      />
    );
  }

  return (
    <div>
      {/* Fetching indicator */}
      {isFetching && (
        <div className="mb-4 flex items-center gap-2 text-xs text-gray-500 dark:text-gray-400">
          <div className="h-2 w-2 animate-pulse rounded-full bg-primary-500" />
          Updating...
        </div>
      )}

      {/* Article grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {articles.map((article) => (
          <NewsCard key={article.id} article={article} />
        ))}
      </div>

      {/* Pagination */}
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        totalElements={totalElements}
        onPageChange={onPageChange}
        className="mt-6"
      />
    </div>
  );
}
