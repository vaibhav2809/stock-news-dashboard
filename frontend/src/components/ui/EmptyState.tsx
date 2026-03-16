import type { LucideIcon } from 'lucide-react';
import { Inbox } from 'lucide-react';
import { cn } from '@/utils';

interface EmptyStateProps {
  /** Icon to display (default: Inbox) */
  icon?: LucideIcon;
  /** Primary title text */
  title: string;
  /** Secondary description text */
  description?: string;
  /** Optional action element (e.g., a button) */
  action?: React.ReactNode;
  /** Additional CSS classes */
  className?: string;
}

/**
 * Empty state placeholder shown when a list or query returns no results.
 * Provides a consistent, friendly UI across all pages.
 *
 * @param icon - Lucide icon component (default: Inbox)
 * @param title - Heading text explaining the empty state
 * @param description - Supportive text with guidance
 * @param action - Optional CTA button or link
 * @param className - Additional CSS classes
 */
export function EmptyState({
  icon: Icon = Inbox,
  title,
  description,
  action,
  className,
}: EmptyStateProps) {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-gray-300 py-16 dark:border-gray-700',
        className,
      )}
    >
      <Icon className="mb-4 h-12 w-12 text-gray-400 dark:text-gray-600" />
      <h2 className="text-lg font-semibold text-gray-600 dark:text-gray-400">{title}</h2>
      {description && (
        <p className="mt-2 max-w-md text-center text-sm text-gray-500 dark:text-gray-500">
          {description}
        </p>
      )}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
