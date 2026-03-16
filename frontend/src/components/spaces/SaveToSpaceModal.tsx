import { useState } from 'react';
import { X, FolderPlus, Check, Plus } from 'lucide-react';
import { useSpaces, useCreateSpace, useSaveArticle } from '@/hooks';
import type { NewsArticle } from '@/types';
import { LoadingSpinner } from '@/components/ui';

interface SaveToSpaceModalProps {
  /** The article to save */
  article: NewsArticle;
  /** Whether the modal is open */
  isOpen: boolean;
  /** Callback to close the modal */
  onClose: () => void;
}

/**
 * Modal for saving a news article to a Space (reading list).
 * Lists existing spaces with a "save" button for each, and allows inline
 * creation of a new space.
 *
 * @param article - The news article to save
 * @param isOpen - Controls modal visibility
 * @param onClose - Close handler
 */
export function SaveToSpaceModal({ article, isOpen, onClose }: SaveToSpaceModalProps) {
  const [isCreating, setIsCreating] = useState(false);
  const [newSpaceName, setNewSpaceName] = useState('');
  const [savedToSpaceId, setSavedToSpaceId] = useState<number | null>(null);

  const { data: spaces = [], isLoading } = useSpaces();
  const createMutation = useCreateSpace();
  const saveMutation = useSaveArticle();

  if (!isOpen) return null;

  /** Handles saving the article to a specific space. */
  const handleSaveToSpace = (spaceId: number) => {
    saveMutation.mutate(
      {
        spaceId,
        request: {
          externalId: article.externalId,
          source: article.source,
          symbol: article.symbol,
          title: article.title,
          summary: article.summary,
          sourceUrl: article.sourceUrl,
          imageUrl: article.imageUrl ?? undefined,
          sentiment: article.sentiment,
          sentimentScore: article.sentimentScore,
          publishedAt: article.publishedAt,
        },
      },
      {
        onSuccess: () => {
          setSavedToSpaceId(spaceId);
          setTimeout(() => {
            onClose();
            setSavedToSpaceId(null);
          }, 1000);
        },
      },
    );
  };

  /** Handles creating a new space and immediately saving the article to it. */
  const handleCreateAndSave = () => {
    if (!newSpaceName.trim()) return;

    createMutation.mutate(
      { name: newSpaceName.trim() },
      {
        onSuccess: (createdSpace) => {
          setIsCreating(false);
          setNewSpaceName('');
          handleSaveToSpace(createdSpace.id);
        },
      },
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="mx-4 w-full max-w-md rounded-xl border border-gray-200 bg-white shadow-2xl dark:border-gray-700 dark:bg-gray-900">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-200 px-5 py-4 dark:border-gray-700">
          <div className="flex items-center gap-2">
            <FolderPlus className="h-5 w-5 text-primary-600 dark:text-primary-400" />
            <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
              Save to Space
            </h2>
          </div>
          <button
            onClick={onClose}
            className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 dark:hover:bg-gray-800 dark:hover:text-gray-300"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Article preview */}
        <div className="border-b border-gray-100 px-5 py-3 dark:border-gray-800">
          <p className="line-clamp-2 text-sm text-gray-600 dark:text-gray-400">
            {article.title}
          </p>
        </div>

        {/* Space list */}
        <div className="max-h-64 overflow-y-auto px-5 py-3">
          {isLoading ? (
            <LoadingSpinner size="small" message="Loading spaces..." />
          ) : spaces.length === 0 && !isCreating ? (
            <p className="py-4 text-center text-sm text-gray-500 dark:text-gray-400">
              No spaces yet. Create one below.
            </p>
          ) : (
            <div className="space-y-1">
              {spaces.map((space) => (
                <button
                  key={space.id}
                  onClick={() => handleSaveToSpace(space.id)}
                  disabled={saveMutation.isPending || savedToSpaceId !== null}
                  className="flex w-full items-center justify-between rounded-lg px-3 py-2.5 text-left transition-colors hover:bg-gray-50 disabled:opacity-50 dark:hover:bg-gray-800"
                >
                  <div>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {space.name}
                    </p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      {space.articleCount} {space.articleCount === 1 ? 'article' : 'articles'}
                    </p>
                  </div>
                  {savedToSpaceId === space.id ? (
                    <Check className="h-4 w-4 text-green-500" />
                  ) : (
                    <Plus className="h-4 w-4 text-gray-400" />
                  )}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Create new space */}
        <div className="border-t border-gray-200 px-5 py-3 dark:border-gray-700">
          {isCreating ? (
            <div className="flex items-center gap-2">
              <input
                type="text"
                value={newSpaceName}
                onChange={(e) => setNewSpaceName(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleCreateAndSave()}
                placeholder="Space name..."
                className="flex-1 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                autoFocus
              />
              <button
                onClick={handleCreateAndSave}
                disabled={!newSpaceName.trim() || createMutation.isPending}
                className="rounded-lg bg-primary-600 px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-700 disabled:opacity-50 dark:bg-primary-500 dark:hover:bg-primary-600"
              >
                {createMutation.isPending ? '...' : 'Save'}
              </button>
              <button
                onClick={() => {
                  setIsCreating(false);
                  setNewSpaceName('');
                }}
                className="rounded-lg px-3 py-2 text-sm text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800"
              >
                Cancel
              </button>
            </div>
          ) : (
            <button
              onClick={() => setIsCreating(true)}
              className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-primary-600 transition-colors hover:bg-primary-50 dark:text-primary-400 dark:hover:bg-primary-900/20"
            >
              <FolderPlus className="h-4 w-4" />
              Create new space
            </button>
          )}
        </div>

        {/* Error message */}
        {(saveMutation.isError || createMutation.isError) && (
          <div className="border-t border-red-200 bg-red-50 px-5 py-2 text-sm text-red-600 dark:border-red-900/50 dark:bg-red-950/30 dark:text-red-400">
            {(saveMutation.error as Error)?.message ||
              (createMutation.error as Error)?.message ||
              'An error occurred'}
          </div>
        )}
      </div>
    </div>
  );
}
