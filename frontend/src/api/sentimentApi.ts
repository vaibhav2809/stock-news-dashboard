import apiClient from './apiClient';
import type { SentimentDistribution, SentimentTimelineEntry } from '@/types/sentiment';

/**
 * Fetches the sentiment distribution for a single stock symbol.
 * @param symbol - the stock ticker (e.g., "AAPL")
 * @returns sentiment distribution with counts and percentages
 */
export async function fetchSentimentDistribution(symbol: string): Promise<SentimentDistribution> {
  const response = await apiClient.get<SentimentDistribution>(`/sentiment/${symbol}`);
  return response.data;
}

/**
 * Fetches sentiment distributions for multiple symbols at once.
 * @param symbols - array of stock tickers
 * @returns list of distributions, one per symbol
 */
export async function fetchSentimentDistributions(symbols: string[]): Promise<SentimentDistribution[]> {
  const response = await apiClient.get<SentimentDistribution[]>('/sentiment', {
    params: { symbols: symbols.join(',') },
  });
  return response.data;
}

/**
 * Fetches the day-by-day sentiment timeline for a symbol.
 * @param symbol - the stock ticker
 * @param days - number of days to look back (default 30, max 90)
 * @returns list of daily sentiment data points
 */
export async function fetchSentimentTimeline(
  symbol: string,
  days: number = 30,
): Promise<SentimentTimelineEntry[]> {
  const response = await apiClient.get<SentimentTimelineEntry[]>(`/sentiment/${symbol}/timeline`, {
    params: { days },
  });
  return response.data;
}
