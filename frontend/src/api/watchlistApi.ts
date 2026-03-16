import apiClient from './apiClient';
import type { WatchlistItem, AddWatchlistItemRequest, WatchStatusResponse } from '@/types/watchlist';

/**
 * Fetches all symbols in the user's watchlist.
 * @returns list of watchlist items with company names and article counts
 */
export async function fetchWatchlist(): Promise<WatchlistItem[]> {
  const response = await apiClient.get<WatchlistItem[]>('/watchlist');
  return response.data;
}

/**
 * Adds a stock symbol to the user's watchlist.
 * @param request - contains the symbol to add
 * @returns the created watchlist item
 */
export async function addToWatchlist(request: AddWatchlistItemRequest): Promise<WatchlistItem> {
  const response = await apiClient.post<WatchlistItem>('/watchlist', request);
  return response.data;
}

/**
 * Removes a stock symbol from the user's watchlist.
 * @param symbol - the stock ticker to remove
 */
export async function removeFromWatchlist(symbol: string): Promise<void> {
  await apiClient.delete(`/watchlist/${symbol}`);
}

/**
 * Checks if a specific symbol is in the user's watchlist.
 * @param symbol - the stock ticker to check
 * @returns object with isWatched boolean
 */
export async function checkWatchStatus(symbol: string): Promise<WatchStatusResponse> {
  const response = await apiClient.get<WatchStatusResponse>(`/watchlist/${symbol}/status`);
  return response.data;
}
