import { Folder, MoreVertical, Pencil, Trash2 } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import type { Space } from '@/types';
import { formatRelativeTime } from '@/utils';

interface SpaceCardProps {
  /** The space data to display */
  space: Space;
  /** Callback when edit is clicked */
  onEdit: (space: Space) => void;
  /** Callback when delete is clicked */
  onDelete: (space: Space) => void;
}

/**
 * Card component for displaying a single Space in the spaces grid.
 * Shows name, description, article count, and a three-dot menu
 * for edit/delete actions.
 *
 * @param space - The space to render
 * @param onEdit - Edit handler
 * @param onDelete - Delete handler
 */
export function SpaceCard({ space, onEdit, onDelete }: SpaceCardProps) {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  /** Close menu when clicking outside. */
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="group relative rounded-xl border border-gray-200 bg-white p-5 transition-shadow hover:shadow-md dark:border-gray-800 dark:bg-gray-900">
      {/* Three-dot menu */}
      <div className="absolute right-3 top-3" ref={menuRef}>
        <button
          onClick={() => setIsMenuOpen(!isMenuOpen)}
          className="rounded-md p-1 text-gray-400 opacity-0 transition-opacity hover:bg-gray-100 hover:text-gray-600 group-hover:opacity-100 dark:hover:bg-gray-800 dark:hover:text-gray-300"
          aria-label="Space options"
        >
          <MoreVertical className="h-4 w-4" />
        </button>

        {isMenuOpen && (
          <div className="absolute right-0 top-full z-10 mt-1 w-36 rounded-lg border border-gray-200 bg-white py-1 shadow-lg dark:border-gray-700 dark:bg-gray-800">
            <button
              onClick={() => {
                setIsMenuOpen(false);
                onEdit(space);
              }}
              className="flex w-full items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 dark:text-gray-300 dark:hover:bg-gray-700"
            >
              <Pencil className="h-3.5 w-3.5" />
              Edit
            </button>
            <button
              onClick={() => {
                setIsMenuOpen(false);
                onDelete(space);
              }}
              className="flex w-full items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20"
            >
              <Trash2 className="h-3.5 w-3.5" />
              Delete
            </button>
          </div>
        )}
      </div>

      {/* Content (clickable link to detail page) */}
      <Link to={`/spaces/${space.id}`} className="block">
        <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-lg bg-primary-100 dark:bg-primary-900/30">
          <Folder className="h-5 w-5 text-primary-600 dark:text-primary-400" />
        </div>

        <h3 className="mb-1 text-base font-semibold text-gray-900 dark:text-gray-100">
          {space.name}
        </h3>

        {space.description && (
          <p className="mb-3 line-clamp-2 text-sm text-gray-500 dark:text-gray-400">
            {space.description}
          </p>
        )}

        <div className="flex items-center gap-3 text-xs text-gray-400 dark:text-gray-500">
          <span>
            {space.articleCount} {space.articleCount === 1 ? 'article' : 'articles'}
          </span>
          <span>·</span>
          <span>Updated {formatRelativeTime(space.updatedAt)}</span>
        </div>
      </Link>
    </div>
  );
}
