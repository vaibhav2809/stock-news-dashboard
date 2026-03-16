import type { NewsSource } from '@/types';
import { cn } from '@/utils';

interface SourceBadgeProps {
  source: NewsSource;
  className?: string;
}

/** Display name and styling for each news source. */
const SOURCE_CONFIG: Record<NewsSource, { label: string; className: string }> = {
  FINNHUB: {
    label: 'Finnhub',
    className: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400',
  },
  NEWSDATA_IO: {
    label: 'NewsData',
    className: 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400',
  },
};

/**
 * Displays a color-coded badge indicating the news article source.
 *
 * @param source - The news source (FINNHUB or NEWSDATA_IO)
 * @param className - Additional CSS classes
 */
export function SourceBadge({ source, className }: SourceBadgeProps) {
  const config = SOURCE_CONFIG[source];

  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium',
        config.className,
        className,
      )}
    >
      {config.label}
    </span>
  );
}
