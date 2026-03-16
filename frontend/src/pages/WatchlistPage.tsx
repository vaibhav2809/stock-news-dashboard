import { useState } from 'react';
import { Bookmark, Plus, Trash2, Newspaper, Search, Loader2, AlertCircle } from 'lucide-react';
import { useWatchlist, useAddToWatchlist, useRemoveFromWatchlist } from '@/hooks/useWatchlist';

/**
 * Watchlist management page where users can add/remove stock symbols
 * and see metadata (company name, article count) for each watched symbol.
 */
export function WatchlistPage() {
  const [symbolInput, setSymbolInput] = useState('');
  const [symbolToDelete, setSymbolToDelete] = useState<string | null>(null);

  const { data: watchlist, isLoading, isError, error } = useWatchlist();
  const addMutation = useAddToWatchlist();
  const removeMutation = useRemoveFromWatchlist();

  /** Handles adding a new symbol to the watchlist. */
  const handleAddSymbol = () => {
    const trimmed = symbolInput.trim().toUpperCase();
    if (!trimmed) return;

    addMutation.mutate(
      { symbol: trimmed },
      {
        onSuccess: () => setSymbolInput(''),
      },
    );
  };

  /** Handles form submission via Enter key. */
  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter') {
      handleAddSymbol();
    }
  };

  /** Confirms and executes symbol removal. */
  const handleConfirmDelete = () => {
    if (!symbolToDelete) return;
    removeMutation.mutate(symbolToDelete, {
      onSuccess: () => setSymbolToDelete(null),
    });
  };

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Watchlist</h1>
        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Track your favorite stocks and their latest news
        </p>
      </div>

      {/* Add Symbol Input */}
      <div className="mb-6 flex gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={symbolInput}
            onChange={(e) => setSymbolInput(e.target.value.toUpperCase())}
            onKeyDown={handleKeyDown}
            placeholder="Enter stock symbol (e.g., AAPL, TSLA)"
            className="w-full rounded-lg border border-gray-300 bg-white py-2.5 pl-10 pr-4 text-sm text-gray-900 placeholder-gray-400 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100 dark:placeholder-gray-500"
            maxLength={20}
          />
        </div>
        <button
          onClick={handleAddSymbol}
          disabled={!symbolInput.trim() || addMutation.isPending}
          className="flex items-center gap-2 rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {addMutation.isPending ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <Plus className="h-4 w-4" />
          )}
          Add
        </button>
      </div>

      {/* Error from add mutation */}
      {addMutation.isError && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-800 dark:bg-red-900/20 dark:text-red-400">
          {addMutation.error?.message || 'Failed to add symbol'}
        </div>
      )}

      {/* Loading State */}
      {isLoading && (
        <div className="flex items-center justify-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-primary-500" />
          <span className="ml-3 text-gray-500 dark:text-gray-400">Loading watchlist...</span>
        </div>
      )}

      {/* Error State */}
      {isError && (
        <div className="flex flex-col items-center justify-center py-16">
          <AlertCircle className="mb-3 h-10 w-10 text-red-500" />
          <p className="text-sm text-red-600 dark:text-red-400">
            {(error as Error)?.message || 'Failed to load watchlist'}
          </p>
        </div>
      )}

      {/* Empty State */}
      {!isLoading && !isError && watchlist && watchlist.length === 0 && (
        <div className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-gray-300 py-16 dark:border-gray-700">
          <Bookmark className="mb-4 h-12 w-12 text-gray-400 dark:text-gray-600" />
          <h2 className="text-lg font-semibold text-gray-600 dark:text-gray-400">
            Your watchlist is empty
          </h2>
          <p className="mt-2 max-w-md text-center text-sm text-gray-500 dark:text-gray-500">
            Add stock symbols above to start tracking their news and sentiment.
          </p>
        </div>
      )}

      {/* Watchlist Grid */}
      {watchlist && watchlist.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {watchlist.map((item) => (
            <div
              key={item.id}
              className="group relative rounded-xl border border-gray-200 bg-white p-5 transition-shadow hover:shadow-md dark:border-gray-700 dark:bg-gray-800"
            >
              {/* Delete Button */}
              <button
                onClick={() => setSymbolToDelete(item.symbol)}
                className="absolute right-3 top-3 rounded-lg p-1.5 text-gray-400 opacity-0 transition-all hover:bg-red-50 hover:text-red-500 group-hover:opacity-100 dark:hover:bg-red-900/20 dark:hover:text-red-400"
                title={`Remove ${item.symbol}`}
              >
                <Trash2 className="h-4 w-4" />
              </button>

              {/* Symbol Badge */}
              <div className="mb-3 flex items-center gap-3">
                <span className="inline-flex items-center rounded-lg bg-primary-100 px-3 py-1.5 text-sm font-bold text-primary-700 dark:bg-primary-900/30 dark:text-primary-400">
                  {item.symbol}
                </span>
              </div>

              {/* Company Name */}
              <h3 className="mb-2 text-sm font-medium text-gray-900 dark:text-gray-100">
                {item.companyName}
              </h3>

              {/* Metadata Row */}
              <div className="flex items-center gap-4 text-xs text-gray-500 dark:text-gray-400">
                <span className="flex items-center gap-1">
                  <Newspaper className="h-3.5 w-3.5" />
                  {item.articleCount} articles
                </span>
                <span>
                  Added {new Date(item.addedAt).toLocaleDateString()}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {symbolToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="mx-4 w-full max-w-sm rounded-xl bg-white p-6 shadow-xl dark:bg-gray-800">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
              Remove {symbolToDelete}?
            </h3>
            <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
              This will remove {symbolToDelete} from your watchlist. You can always add it back later.
            </p>
            <div className="mt-5 flex justify-end gap-3">
              <button
                onClick={() => setSymbolToDelete(null)}
                className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:text-gray-300 dark:hover:bg-gray-700"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmDelete}
                disabled={removeMutation.isPending}
                className="flex items-center gap-2 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
              >
                {removeMutation.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                Remove
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
