-- Watchlist items: tracks which stock symbols a user is watching.
-- Simplified design: no separate "watchlist" entity. Each user has one implicit watchlist.
-- This keeps the API surface simple while still supporting per-user symbol tracking.

CREATE TABLE watchlist_items (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    symbol      VARCHAR(20)     NOT NULL,
    added_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- Each user can only watch a symbol once
    CONSTRAINT uq_watchlist_user_symbol UNIQUE (user_id, symbol)
);

-- Index for fast lookups by user
CREATE INDEX idx_watchlist_items_user_id ON watchlist_items(user_id);

-- Index for looking up all watchers of a symbol (useful for alerts)
CREATE INDEX idx_watchlist_items_symbol ON watchlist_items(symbol);
