import { ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '@/utils';

interface PaginationProps {
  /** Current page number (zero-based) */
  currentPage: number;
  /** Total number of pages */
  totalPages: number;
  /** Total number of elements across all pages */
  totalElements: number;
  /** Callback when page changes */
  onPageChange: (page: number) => void;
  /** Additional CSS classes */
  className?: string;
}

/**
 * Pagination controls for paginated data views.
 * Shows page numbers, previous/next buttons, and a results summary.
 *
 * @param currentPage - Zero-based current page index
 * @param totalPages - Total number of available pages
 * @param totalElements - Total number of results across all pages
 * @param onPageChange - Callback invoked with the new page number
 * @param className - Additional CSS classes
 */
export function Pagination({
  currentPage,
  totalPages,
  totalElements,
  onPageChange,
  className,
}: PaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  const isFirstPage = currentPage === 0;
  const isLastPage = currentPage >= totalPages - 1;

  /** Generates the array of page numbers to display, with ellipsis gaps. */
  const getVisiblePageNumbers = (): (number | 'ellipsis')[] => {
    const MAX_VISIBLE_PAGES = 7;

    if (totalPages <= MAX_VISIBLE_PAGES) {
      return Array.from({ length: totalPages }, (_, index) => index);
    }

    const pages: (number | 'ellipsis')[] = [0];

    if (currentPage > 2) {
      pages.push('ellipsis');
    }

    const rangeStart = Math.max(1, currentPage - 1);
    const rangeEnd = Math.min(totalPages - 2, currentPage + 1);

    for (let i = rangeStart; i <= rangeEnd; i++) {
      pages.push(i);
    }

    if (currentPage < totalPages - 3) {
      pages.push('ellipsis');
    }

    pages.push(totalPages - 1);

    return pages;
  };

  const pageButtonClass = (isActive: boolean) =>
    cn(
      'flex h-8 w-8 items-center justify-center rounded-md text-sm font-medium transition-colors',
      isActive
        ? 'bg-primary-600 text-white dark:bg-primary-500'
        : 'text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800',
    );

  return (
    <div className={cn('flex items-center justify-between', className)}>
      <p className="text-sm text-gray-500 dark:text-gray-400">
        {totalElements.toLocaleString()} {totalElements === 1 ? 'result' : 'results'}
      </p>

      <nav className="flex items-center gap-1" aria-label="Pagination">
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={isFirstPage}
          className="flex h-8 w-8 items-center justify-center rounded-md text-gray-500 transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-40 dark:text-gray-400 dark:hover:bg-gray-800"
          aria-label="Previous page"
        >
          <ChevronLeft className="h-4 w-4" />
        </button>

        {getVisiblePageNumbers().map((pageNumber, index) =>
          pageNumber === 'ellipsis' ? (
            <span
              key={`ellipsis-${index}`}
              className="flex h-8 w-8 items-center justify-center text-sm text-gray-400"
            >
              …
            </span>
          ) : (
            <button
              key={pageNumber}
              onClick={() => onPageChange(pageNumber)}
              className={pageButtonClass(pageNumber === currentPage)}
              aria-label={`Page ${pageNumber + 1}`}
              aria-current={pageNumber === currentPage ? 'page' : undefined}
            >
              {pageNumber + 1}
            </button>
          ),
        )}

        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={isLastPage}
          className="flex h-8 w-8 items-center justify-center rounded-md text-gray-500 transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-40 dark:text-gray-400 dark:hover:bg-gray-800"
          aria-label="Next page"
        >
          <ChevronRight className="h-4 w-4" />
        </button>
      </nav>
    </div>
  );
}
