import { useState, useCallback } from 'react';
import { FolderOpen, Plus } from 'lucide-react';
import { useSpaces, useCreateSpace, useUpdateSpace, useDeleteSpace } from '@/hooks';
import { SpaceCard, SpaceFormModal } from '@/components/spaces';
import { LoadingSpinner, EmptyState, ErrorBanner } from '@/components/ui';
import type { Space } from '@/types';

/**
 * Spaces listing page.
 * Shows a grid of space cards with create, edit, and delete functionality.
 * Each card links to a detail page showing saved articles.
 */
export function SpacesPage() {
  const { data: spaces = [], isLoading, error, refetch } = useSpaces();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingSpace, setEditingSpace] = useState<Space | null>(null);
  const [deletingSpace, setDeletingSpace] = useState<Space | null>(null);

  const createMutation = useCreateSpace();
  const updateMutation = useUpdateSpace();
  const deleteMutation = useDeleteSpace();

  /** Opens the create form modal. */
  const handleOpenCreate = useCallback(() => {
    setEditingSpace(null);
    setIsFormOpen(true);
  }, []);

  /** Opens the edit form modal with prefilled data. */
  const handleOpenEdit = useCallback((space: Space) => {
    setEditingSpace(space);
    setIsFormOpen(true);
  }, []);

  /** Handles form submission for both create and edit. */
  const handleFormSubmit = useCallback(
    (name: string, description: string) => {
      if (editingSpace) {
        updateMutation.mutate(
          { spaceId: editingSpace.id, request: { name, description } },
          {
            onSuccess: () => {
              setIsFormOpen(false);
              setEditingSpace(null);
            },
          },
        );
      } else {
        createMutation.mutate(
          { name, description },
          {
            onSuccess: () => {
              setIsFormOpen(false);
            },
          },
        );
      }
    },
    [editingSpace, createMutation, updateMutation],
  );

  /** Handles space deletion with confirmation. */
  const handleDelete = useCallback(
    (space: Space) => {
      setDeletingSpace(space);
    },
    [],
  );

  /** Confirms and executes deletion. */
  const handleConfirmDelete = useCallback(() => {
    if (!deletingSpace) return;

    deleteMutation.mutate(deletingSpace.id, {
      onSuccess: () => {
        setDeletingSpace(null);
      },
    });
  }, [deletingSpace, deleteMutation]);

  if (isLoading) {
    return <LoadingSpinner size="large" message="Loading spaces..." />;
  }

  if (error) {
    return (
      <ErrorBanner
        message={(error as Error).message}
        onRetry={() => refetch()}
      />
    );
  }

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary-100 dark:bg-primary-900/30">
            <FolderOpen className="h-5 w-5 text-primary-600 dark:text-primary-400" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Spaces</h1>
            <p className="mt-0.5 text-sm text-gray-500 dark:text-gray-400">
              Organize saved articles into reading lists
            </p>
          </div>
        </div>

        <button
          onClick={handleOpenCreate}
          className="flex items-center gap-2 rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-700 dark:bg-primary-500 dark:hover:bg-primary-600"
        >
          <Plus className="h-4 w-4" />
          New Space
        </button>
      </div>

      {/* Spaces grid */}
      {spaces.length === 0 ? (
        <EmptyState
          icon={FolderOpen}
          title="No spaces yet"
          description="Create your first reading list to start saving articles for later."
          action={
            <button
              onClick={handleOpenCreate}
              className="flex items-center gap-2 rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-700 dark:bg-primary-500 dark:hover:bg-primary-600"
            >
              <Plus className="h-4 w-4" />
              Create Space
            </button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {spaces.map((space) => (
            <SpaceCard
              key={space.id}
              space={space}
              onEdit={handleOpenEdit}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}

      {/* Create/Edit modal */}
      <SpaceFormModal
        isOpen={isFormOpen}
        onClose={() => {
          setIsFormOpen(false);
          setEditingSpace(null);
        }}
        onSubmit={handleFormSubmit}
        isPending={createMutation.isPending || updateMutation.isPending}
        editingSpace={editingSpace}
      />

      {/* Delete confirmation modal */}
      {deletingSpace && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="mx-4 w-full max-w-sm rounded-xl border border-gray-200 bg-white p-6 shadow-2xl dark:border-gray-700 dark:bg-gray-900">
            <h3 className="mb-2 text-lg font-semibold text-gray-900 dark:text-gray-100">
              Delete &ldquo;{deletingSpace.name}&rdquo;?
            </h3>
            <p className="mb-5 text-sm text-gray-500 dark:text-gray-400">
              This will permanently delete this space and all {deletingSpace.articleCount} saved{' '}
              {deletingSpace.articleCount === 1 ? 'article' : 'articles'}. This action cannot be
              undone.
            </p>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setDeletingSpace(null)}
                className="rounded-lg px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmDelete}
                disabled={deleteMutation.isPending}
                className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-red-700 disabled:opacity-50"
              >
                {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
