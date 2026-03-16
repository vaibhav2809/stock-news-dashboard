import { Filter, X } from 'lucide-react';
import type { Sentiment, NewsSource } from '@/types';
import { cn } from '@/utils';

interface NewsFiltersProps {
  /** Currently selected sentiment filter */
  selectedSentiment: Sentiment | null;
  /** Currently selected source filter */
  selectedSource: NewsSource | null;
  /** Start date filter (yyyy-MM-dd) */
  fromDate: string;
  /** End date filter (yyyy-MM-dd) */
  toDate: string;
  /** Callback when sentiment filter changes */
  onSentimentChange: (sentiment: Sentiment | null) => void;
  /** Callback when source filter changes */
  onSourceChange: (source: NewsSource | null) => void;
  /** Callback when from date changes */
  onFromDateChange: (date: string) => void;
  /** Callback when to date changes */
  onToDateChange: (date: string) => void;
  /** Callback to clear all filters */
  onClearAll: () => void;
  /** Additional CSS classes */
  className?: string;
}

/** Available sentiment filter options. */
const SENTIMENT_OPTIONS: { value: Sentiment; label: string }[] = [
  { value: 'POSITIVE', label: 'Positive' },
  { value: 'NEGATIVE', label: 'Negative' },
  { value: 'NEUTRAL', label: 'Neutral' },
];

/** Available source filter options. */
const SOURCE_OPTIONS: { value: NewsSource; label: string }[] = [
  { value: 'FINNHUB', label: 'Finnhub' },
  { value: 'NEWSDATA_IO', label: 'NewsData' },
];

/**
 * Filter bar for news articles.
 * Provides controls for sentiment, source, and date range filtering.
 * Shows a "Clear all" button when any filter is active.
 *
 * @param selectedSentiment - Active sentiment filter
 * @param selectedSource - Active source filter
 * @param fromDate - Start date filter
 * @param toDate - End date filter
 * @param onSentimentChange - Sentiment filter change handler
 * @param onSourceChange - Source filter change handler
 * @param onFromDateChange - From date change handler
 * @param onToDateChange - To date change handler
 * @param onClearAll - Clear all filters handler
 * @param className - Additional CSS classes
 */
export function NewsFilters({
  selectedSentiment,
  selectedSource,
  fromDate,
  toDate,
  onSentimentChange,
  onSourceChange,
  onFromDateChange,
  onToDateChange,
  onClearAll,
  className,
}: NewsFiltersProps) {
  const hasActiveFilters = selectedSentiment !== null || selectedSource !== null || fromDate !== '' || toDate !== '';

  const chipClass = (isActive: boolean) =>
    cn(
      'rounded-full px-3 py-1.5 text-xs font-medium transition-colors cursor-pointer',
      isActive
        ? 'bg-primary-600 text-white dark:bg-primary-500'
        : 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700',
    );

  return (
    <div className={cn('space-y-3', className)}>
      <div className="flex flex-wrap items-center gap-3">
        {/* Filter icon */}
        <div className="flex items-center gap-1.5 text-sm font-medium text-gray-700 dark:text-gray-300">
          <Filter className="h-4 w-4" />
          Filters
        </div>

        {/* Sentiment chips */}
        <div className="flex items-center gap-1.5">
          <span className="text-xs text-gray-500 dark:text-gray-400">Sentiment:</span>
          {SENTIMENT_OPTIONS.map(({ value, label }) => (
            <button
              key={value}
              onClick={() => onSentimentChange(selectedSentiment === value ? null : value)}
              className={chipClass(selectedSentiment === value)}
            >
              {label}
            </button>
          ))}
        </div>

        {/* Divider */}
        <div className="h-5 w-px bg-gray-300 dark:bg-gray-700" />

        {/* Source chips */}
        <div className="flex items-center gap-1.5">
          <span className="text-xs text-gray-500 dark:text-gray-400">Source:</span>
          {SOURCE_OPTIONS.map(({ value, label }) => (
            <button
              key={value}
              onClick={() => onSourceChange(selectedSource === value ? null : value)}
              className={chipClass(selectedSource === value)}
            >
              {label}
            </button>
          ))}
        </div>

        {/* Divider */}
        <div className="h-5 w-px bg-gray-300 dark:bg-gray-700" />

        {/* Date range */}
        <div className="flex items-center gap-1.5">
          <span className="text-xs text-gray-500 dark:text-gray-400">From:</span>
          <input
            type="date"
            value={fromDate}
            onChange={(e) => onFromDateChange(e.target.value)}
            className="rounded-md border border-gray-200 bg-white px-2 py-1 text-xs text-gray-700 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300"
          />
          <span className="text-xs text-gray-500 dark:text-gray-400">To:</span>
          <input
            type="date"
            value={toDate}
            onChange={(e) => onToDateChange(e.target.value)}
            className="rounded-md border border-gray-200 bg-white px-2 py-1 text-xs text-gray-700 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300"
          />
        </div>

        {/* Clear all */}
        {hasActiveFilters && (
          <button
            onClick={onClearAll}
            className="flex items-center gap-1 rounded-full bg-red-100 px-3 py-1.5 text-xs font-medium text-red-700 transition-colors hover:bg-red-200 dark:bg-red-900/30 dark:text-red-400 dark:hover:bg-red-900/50"
          >
            <X className="h-3 w-3" />
            Clear all
          </button>
        )}
      </div>
    </div>
  );
}
