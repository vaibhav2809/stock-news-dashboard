-- Master list of trackable stock symbols
CREATE TABLE stock_symbols (
    id              BIGSERIAL       PRIMARY KEY,
    ticker          VARCHAR(20)     NOT NULL UNIQUE,
    company_name    VARCHAR(255)    NOT NULL,
    exchange        VARCHAR(50),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Index on ticker for fast lookups
CREATE INDEX idx_stock_symbols_ticker ON stock_symbols (ticker);

-- Index on active symbols (most queries filter by is_active=true)
CREATE INDEX idx_stock_symbols_active ON stock_symbols (is_active) WHERE is_active = TRUE;

-- Seed popular stock symbols
INSERT INTO stock_symbols (ticker, company_name, exchange) VALUES
    ('AAPL', 'Apple Inc.', 'NASDAQ'),
    ('MSFT', 'Microsoft Corporation', 'NASDAQ'),
    ('GOOGL', 'Alphabet Inc.', 'NASDAQ'),
    ('AMZN', 'Amazon.com Inc.', 'NASDAQ'),
    ('TSLA', 'Tesla Inc.', 'NASDAQ'),
    ('META', 'Meta Platforms Inc.', 'NASDAQ'),
    ('NVDA', 'NVIDIA Corporation', 'NASDAQ'),
    ('JPM', 'JPMorgan Chase & Co.', 'NYSE'),
    ('V', 'Visa Inc.', 'NYSE'),
    ('JNJ', 'Johnson & Johnson', 'NYSE'),
    ('WMT', 'Walmart Inc.', 'NYSE'),
    ('PG', 'Procter & Gamble Co.', 'NYSE'),
    ('MA', 'Mastercard Inc.', 'NYSE'),
    ('UNH', 'UnitedHealth Group Inc.', 'NYSE'),
    ('HD', 'Home Depot Inc.', 'NYSE'),
    ('DIS', 'Walt Disney Co.', 'NYSE'),
    ('BAC', 'Bank of America Corp.', 'NYSE'),
    ('NFLX', 'Netflix Inc.', 'NASDAQ'),
    ('ADBE', 'Adobe Inc.', 'NASDAQ'),
    ('CRM', 'Salesforce Inc.', 'NYSE');
