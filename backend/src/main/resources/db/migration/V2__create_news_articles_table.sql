-- News articles fetched from external APIs (Finnhub, NewsData.io)
CREATE TABLE news_articles (
    id              BIGSERIAL       PRIMARY KEY,
    external_id     VARCHAR(255),
    fingerprint     VARCHAR(64)     NOT NULL UNIQUE,
    title           VARCHAR(500)    NOT NULL,
    summary         TEXT,
    source_url      VARCHAR(2000)   NOT NULL,
    image_url       VARCHAR(2000),
    source          VARCHAR(50)     NOT NULL,
    symbol          VARCHAR(20),
    sentiment       VARCHAR(20)     NOT NULL DEFAULT 'NEUTRAL',
    sentiment_score DOUBLE PRECISION DEFAULT 0.0,
    published_at    TIMESTAMPTZ     NOT NULL,
    fetched_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Index on symbol for fast lookups by ticker
CREATE INDEX idx_news_articles_symbol ON news_articles (symbol);

-- Index on published_at for chronological sorting
CREATE INDEX idx_news_articles_published_at ON news_articles (published_at DESC);

-- Index on source for filtering by provider
CREATE INDEX idx_news_articles_source ON news_articles (source);

-- Index on sentiment for filtering
CREATE INDEX idx_news_articles_sentiment ON news_articles (sentiment);

-- Composite index for the most common query pattern: symbol + date range
CREATE INDEX idx_news_articles_symbol_published ON news_articles (symbol, published_at DESC);
