/**
 * Represents a Space (reading list) for saving articles.
 */
export interface Space {
  id: number;
  name: string;
  description: string | null;
  articleCount: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * Request payload for creating or updating a Space.
 */
export interface SpaceRequest {
  name: string;
  description?: string;
}

/**
 * Represents a saved article within a Space.
 */
export interface SavedArticle {
  id: number;
  externalId: string | null;
  source: string;
  symbol: string | null;
  title: string;
  summary: string | null;
  sourceUrl: string;
  imageUrl: string | null;
  sentiment: string | null;
  sentimentScore: number;
  publishedAt: string | null;
  savedAt: string;
}

/**
 * Request payload for saving an article to a Space.
 */
export interface SaveArticleRequest {
  externalId?: string;
  source: string;
  symbol?: string;
  title: string;
  summary?: string;
  sourceUrl: string;
  imageUrl?: string;
  sentiment?: string;
  sentimentScore?: number;
  publishedAt?: string;
}
