import { useState, useEffect } from 'react';
import { X, FolderPlus, Pencil } from 'lucide-react';
import type { Space } from '@/types';

interface SpaceFormModalProps {
  /** Whether the modal is open */
  isOpen: boolean;
  /** Callback to close the modal */
  onClose: () => void;
  /** Callback when form is submitted */
  onSubmit: (name: string, description: string) => void;
  /** Whether the submit action is pending */
  isPending: boolean;
  /** Existing space to edit (null for create mode) */
  editingSpace?: Space | null;
}

/**
 * Modal form for creating or editing a Space.
 * Shows name and description fields with submit/cancel buttons.
 *
 * @param isOpen - Controls modal visibility
 * @param onClose - Close handler
 * @param onSubmit - Form submission handler (name, description)
 * @param isPending - Whether the submit mutation is in progress
 * @param editingSpace - If provided, the modal is in edit mode with prefilled fields
 */
export function SpaceFormModal({
  isOpen,
  onClose,
  onSubmit,
  isPending,
  editingSpace = null,
}: SpaceFormModalProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  const isEditMode = editingSpace !== null;

  /** Prefill fields when editing. */
  useEffect(() => {
    if (editingSpace) {
      setName(editingSpace.name);
      setDescription(editingSpace.description ?? '');
    } else {
      setName('');
      setDescription('');
    }
  }, [editingSpace, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    onSubmit(name.trim(), description.trim());
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="mx-4 w-full max-w-md rounded-xl border border-gray-200 bg-white shadow-2xl dark:border-gray-700 dark:bg-gray-900">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-200 px-5 py-4 dark:border-gray-700">
          <div className="flex items-center gap-2">
            {isEditMode ? (
              <Pencil className="h-5 w-5 text-primary-600 dark:text-primary-400" />
            ) : (
              <FolderPlus className="h-5 w-5 text-primary-600 dark:text-primary-400" />
            )}
            <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
              {isEditMode ? 'Edit Space' : 'Create Space'}
            </h2>
          </div>
          <button
            onClick={onClose}
            className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 dark:hover:bg-gray-800 dark:hover:text-gray-300"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4 px-5 py-4">
          <div>
            <label
              htmlFor="space-name"
              className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              Name *
            </label>
            <input
              id="space-name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g., Tech Stocks Research"
              maxLength={100}
              required
              className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
              autoFocus
            />
          </div>

          <div>
            <label
              htmlFor="space-description"
              className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              Description
            </label>
            <textarea
              id="space-description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Optional description for this reading list..."
              maxLength={1000}
              rows={3}
              className="w-full resize-none rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!name.trim() || isPending}
              className="rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-primary-700 disabled:opacity-50 dark:bg-primary-500 dark:hover:bg-primary-600"
            >
              {isPending ? 'Saving...' : isEditMode ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
