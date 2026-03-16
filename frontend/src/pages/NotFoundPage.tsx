import { Link } from 'react-router-dom';
import { FileQuestion } from 'lucide-react';

/**
 * 404 page displayed when a route is not found.
 */
export function NotFoundPage() {
  return (
    <div className="flex flex-col items-center justify-center py-24">
      <FileQuestion className="mb-6 h-16 w-16 text-gray-400 dark:text-gray-600" />
      <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Page Not Found</h1>
      <p className="mt-2 text-gray-500 dark:text-gray-400">
        The page you are looking for does not exist.
      </p>
      <Link
        to="/"
        className="mt-6 rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-700"
      >
        Back to News Feed
      </Link>
    </div>
  );
}
