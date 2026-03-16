import { cn } from '@/utils';

interface LoadingSpinnerProps {
  /** Size variant of the spinner */
  size?: 'small' | 'medium' | 'large';
  /** Optional message to display below the spinner */
  message?: string;
  /** Additional CSS classes */
  className?: string;
}

/** Pixel sizes for each spinner variant */
const SPINNER_SIZES = {
  small: 'h-5 w-5',
  medium: 'h-8 w-8',
  large: 'h-12 w-12',
} as const;

/**
 * Animated loading spinner with optional message.
 * Used as the loading state for all data-fetching components.
 *
 * @param size - Spinner size: small, medium, or large (default: medium)
 * @param message - Optional text shown below the spinner
 * @param className - Additional CSS classes for the container
 */
export function LoadingSpinner({ size = 'medium', message, className }: LoadingSpinnerProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center py-12', className)}>
      <div
        className={cn(
          'animate-spin rounded-full border-2 border-gray-300 border-t-primary-600 dark:border-gray-700 dark:border-t-primary-400',
          SPINNER_SIZES[size],
        )}
        role="status"
        aria-label="Loading"
      />
      {message && (
        <p className="mt-3 text-sm text-gray-500 dark:text-gray-400">{message}</p>
      )}
    </div>
  );
}
