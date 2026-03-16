/** Number of milliseconds in common time intervals. */
const SECONDS_PER_MINUTE = 60;
const SECONDS_PER_HOUR = 3600;
const SECONDS_PER_DAY = 86400;
const SECONDS_PER_WEEK = 604800;
const SECONDS_PER_MONTH = 2592000;
const SECONDS_PER_YEAR = 31536000;

/**
 * Formats an ISO date string into a human-readable relative time string.
 * Examples: "just now", "5 minutes ago", "2 hours ago", "3 days ago"
 *
 * @param isoDateString - An ISO 8601 date string (e.g., "2026-03-15T10:30:00Z")
 * @returns A human-readable relative time string
 */
export function formatRelativeTime(isoDateString: string): string {
  const date = new Date(isoDateString);
  const now = new Date();
  const elapsedSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (elapsedSeconds < 0) {
    return 'just now';
  }

  if (elapsedSeconds < SECONDS_PER_MINUTE) {
    return 'just now';
  }

  if (elapsedSeconds < SECONDS_PER_HOUR) {
    const minutes = Math.floor(elapsedSeconds / SECONDS_PER_MINUTE);
    return `${minutes} ${minutes === 1 ? 'minute' : 'minutes'} ago`;
  }

  if (elapsedSeconds < SECONDS_PER_DAY) {
    const hours = Math.floor(elapsedSeconds / SECONDS_PER_HOUR);
    return `${hours} ${hours === 1 ? 'hour' : 'hours'} ago`;
  }

  if (elapsedSeconds < SECONDS_PER_WEEK) {
    const days = Math.floor(elapsedSeconds / SECONDS_PER_DAY);
    return `${days} ${days === 1 ? 'day' : 'days'} ago`;
  }

  if (elapsedSeconds < SECONDS_PER_MONTH) {
    const weeks = Math.floor(elapsedSeconds / SECONDS_PER_WEEK);
    return `${weeks} ${weeks === 1 ? 'week' : 'weeks'} ago`;
  }

  if (elapsedSeconds < SECONDS_PER_YEAR) {
    const months = Math.floor(elapsedSeconds / SECONDS_PER_MONTH);
    return `${months} ${months === 1 ? 'month' : 'months'} ago`;
  }

  const years = Math.floor(elapsedSeconds / SECONDS_PER_YEAR);
  return `${years} ${years === 1 ? 'year' : 'years'} ago`;
}
