import { AlertTriangle, RefreshCw } from 'lucide-react';
import { cn } from '@/utils';

interface ErrorBannerProps {
  /** Error message to display */
  message: string;
  /** Optional retry callback — shows a retry button when provided */
  onRetry?: () => void;
  /** Additional CSS classes */
  className?: string;
}

/**
 * Error banner displayed when a data fetch or operation fails.
 * Shows a warning icon, error message, and optional retry button.
 *
 * @param message - The error message to display
 * @param onRetry - Optional callback to retry the failed operation
 * @param className - Additional CSS classes
 */
export function ErrorBanner({ message, onRetry, className }: ErrorBannerProps) {
  return (
    <div
      className={cn(
        'flex items-center gap-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 dark:border-red-900/50 dark:bg-red-950/30',
        className,
      )}
      role="alert"
    >
      <AlertTriangle className="h-5 w-5 flex-shrink-0 text-red-500 dark:text-red-400" />
      <p className="flex-1 text-sm text-red-700 dark:text-red-300">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="flex items-center gap-1.5 rounded-md bg-red-100 px-3 py-1.5 text-xs font-medium text-red-700 transition-colors hover:bg-red-200 dark:bg-red-900/40 dark:text-red-300 dark:hover:bg-red-900/60"
        >
          <RefreshCw className="h-3 w-3" />
          Retry
        </button>
      )}
    </div>
  );
}
