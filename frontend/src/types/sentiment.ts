/** Sentiment distribution for a single stock symbol. */
export interface SentimentDistribution {
  symbol: string;
  totalArticles: number;
  positiveCount: number;
  negativeCount: number;
  neutralCount: number;
  positivePercent: number;
  negativePercent: number;
  neutralPercent: number;
  averageScore: number;
}

/** A single data point on the sentiment timeline chart. */
export interface SentimentTimelineEntry {
  date: string;
  articleCount: number;
  positiveCount: number;
  negativeCount: number;
  neutralCount: number;
  averageScore: number;
}
