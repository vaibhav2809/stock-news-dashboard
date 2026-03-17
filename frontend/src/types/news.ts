/**
 * Represents a single news article from any source.
 * This is the normalized shape returned by the backend.
 */
export interface NewsArticle {
  id: number;
  externalId: string;
  title: string;
  summary: string;
  sourceUrl: string;
  imageUrl: string | null;
  source: NewsSource;
  symbol: string;
  sentiment: Sentiment;
  sentimentScore: number;
  publishedAt: string;
  fetchedAt: string;
}

/**
 * Supported news data sources.
 */
export type NewsSource = 'FINNHUB' | 'NEWSDATA_IO';

/**
 * Sentiment classification for a news article.
 */
export type Sentiment = 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL';

/**
 * Query parameters for searching news articles.
 * Matches the backend GET /api/v1/news endpoint parameters.
 */
export interface NewsSearchParams {
  symbols?: string[];
  source?: NewsSource;
  sentiment?: Sentiment;
  fromDate?: string;
  toDate?: string;
  keyword?: string;
  page?: number;
  size?: number;
}
