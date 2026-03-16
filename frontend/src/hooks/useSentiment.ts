import { useQuery } from '@tanstack/react-query';
import { fetchSentimentDistribution, fetchSentimentTimeline } from '@/api/sentimentApi';

/** Query key factory for sentiment-related queries. */
const sentimentKeys = {
  distribution: (symbol: string) => ['sentiment', 'distribution', symbol] as const,
  timeline: (symbol: string, days: number) => ['sentiment', 'timeline', symbol, days] as const,
};

/**
 * Fetches the sentiment distribution for a single symbol.
 * Stale time: 5 minutes (sentiment data changes slowly).
 * @param symbol - the stock ticker
 */
export function useSentimentDistribution(symbol: string) {
  return useQuery({
    queryKey: sentimentKeys.distribution(symbol),
    queryFn: () => fetchSentimentDistribution(symbol),
    enabled: !!symbol,
    staleTime: 5 * 60_000,
  });
}

/**
 * Fetches the day-by-day sentiment timeline for a symbol.
 * Stale time: 5 minutes.
 * @param symbol - the stock ticker
 * @param days - number of days to look back
 */
export function useSentimentTimeline(symbol: string, days: number = 30) {
  return useQuery({
    queryKey: sentimentKeys.timeline(symbol, days),
    queryFn: () => fetchSentimentTimeline(symbol, days),
    enabled: !!symbol,
    staleTime: 5 * 60_000,
  });
}
