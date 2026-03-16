import type { Sentiment } from '@/types';
import { cn } from '@/utils';
import { TrendingUp, TrendingDown, Minus } from 'lucide-react';

interface SentimentBadgeProps {
  sentiment: Sentiment;
  className?: string;
}

/** Color and icon mapping for each sentiment type. */
const SENTIMENT_CONFIG: Record<Sentiment, { label: string; className: string; icon: typeof TrendingUp }> = {
  POSITIVE: {
    label: 'Positive',
    className: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400',
    icon: TrendingUp,
  },
  NEGATIVE: {
    label: 'Negative',
    className: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400',
    icon: TrendingDown,
  },
  NEUTRAL: {
    label: 'Neutral',
    className: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400',
    icon: Minus,
  },
};

/**
 * Displays a color-coded sentiment pill with an icon.
 * Green for positive, red for negative, gray for neutral.
 *
 * @param sentiment - The sentiment classification
 * @param className - Additional CSS classes
 */
export function SentimentBadge({ sentiment, className }: SentimentBadgeProps) {
  const config = SENTIMENT_CONFIG[sentiment];
  const Icon = config.icon;

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium',
        config.className,
        className,
      )}
    >
      <Icon className="h-3 w-3" />
      {config.label}
    </span>
  );
}
