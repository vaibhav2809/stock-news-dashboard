import apiClient from './apiClient';

/**
 * Represents a stock symbol returned by the autocomplete API.
 */
export interface StockSymbol {
  id: number;
  ticker: string;
  companyName: string;
  exchange: string;
}

/**
 * Searches for stock symbols matching the given query.
 * Used for autocomplete in the search bar.
 *
 * @param query - Partial ticker or company name (e.g., "AAP" or "Apple")
 * @returns Array of matching stock symbols
 */
export async function searchSymbols(query: string): Promise<StockSymbol[]> {
  const response = await apiClient.get<StockSymbol[]>(
    `/symbols/search?query=${encodeURIComponent(query)}`,
  );
  return response.data;
}
