-- Spaces: personal reading lists for organizing saved articles.
-- Each space belongs to a user and has a unique name within that user's spaces.
CREATE TABLE spaces (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_spaces_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_spaces_user_id ON spaces(user_id);

-- Saved articles: denormalized copy of article data saved to a space.
-- Denormalized because external API articles are ephemeral — we store a snapshot.
CREATE TABLE saved_articles (
    id              BIGSERIAL PRIMARY KEY,
    space_id        BIGINT NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    external_id     VARCHAR(255),
    source          VARCHAR(50) NOT NULL,
    symbol          VARCHAR(20),
    title           VARCHAR(500) NOT NULL,
    summary         TEXT,
    source_url      TEXT NOT NULL,
    image_url       TEXT,
    sentiment       VARCHAR(20),
    sentiment_score DOUBLE PRECISION DEFAULT 0.0,
    published_at    TIMESTAMPTZ,
    saved_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_saved_articles_space_url UNIQUE (space_id, source_url)
);

CREATE INDEX idx_saved_articles_space_id ON saved_articles(space_id);
CREATE INDEX idx_saved_articles_saved_at ON saved_articles(saved_at DESC);
