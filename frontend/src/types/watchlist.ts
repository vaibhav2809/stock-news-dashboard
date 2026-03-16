/** A stock symbol in the user's watchlist with enriched metadata. */
export interface WatchlistItem {
  id: number;
  symbol: string;
  companyName: string;
  articleCount: number;
  addedAt: string;
}

/** Request body for adding a symbol to the watchlist. */
export interface AddWatchlistItemRequest {
  symbol: string;
}

/** Response for checking if a symbol is watched. */
export interface WatchStatusResponse {
  isWatched: boolean;
}
