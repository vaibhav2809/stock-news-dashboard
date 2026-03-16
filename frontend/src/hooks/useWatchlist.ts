import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchWatchlist, addToWatchlist, removeFromWatchlist, checkWatchStatus } from '@/api/watchlistApi';
import type { AddWatchlistItemRequest } from '@/types/watchlist';

/** Query key factory for watchlist-related queries. */
const watchlistKeys = {
  all: ['watchlist'] as const,
  status: (symbol: string) => ['watchlist', 'status', symbol] as const,
};

/**
 * Fetches the user's full watchlist with metadata.
 * Stale time: 30 seconds (watchlist changes frequently with user interaction).
 */
export function useWatchlist() {
  return useQuery({
    queryKey: watchlistKeys.all,
    queryFn: fetchWatchlist,
    staleTime: 30_000,
  });
}

/**
 * Checks if a specific symbol is in the user's watchlist.
 * @param symbol - the stock ticker to check
 */
export function useWatchStatus(symbol: string) {
  return useQuery({
    queryKey: watchlistKeys.status(symbol),
    queryFn: () => checkWatchStatus(symbol),
    enabled: !!symbol,
    staleTime: 30_000,
  });
}

/**
 * Mutation to add a symbol to the watchlist.
 * Invalidates the watchlist query and related status queries on success.
 */
export function useAddToWatchlist() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: AddWatchlistItemRequest) => addToWatchlist(request),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: watchlistKeys.all });
      queryClient.invalidateQueries({ queryKey: watchlistKeys.status(variables.symbol) });
    },
  });
}

/**
 * Mutation to remove a symbol from the watchlist.
 * Invalidates the watchlist query and related status queries on success.
 */
export function useRemoveFromWatchlist() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (symbol: string) => removeFromWatchlist(symbol),
    onSuccess: (_data, symbol) => {
      queryClient.invalidateQueries({ queryKey: watchlistKeys.all });
      queryClient.invalidateQueries({ queryKey: watchlistKeys.status(symbol) });
    },
  });
}
