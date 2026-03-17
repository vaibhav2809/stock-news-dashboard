package com.stocknews.service;

import com.stocknews.model.entity.StockSymbol;
import com.stocknews.repository.StockSymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for bulk-importing stock symbols from external exchange APIs.
 * Currently supports importing equity symbols from NSE India.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockSymbolImportService {

    private final StockSymbolRepository stockSymbolRepository;
    private final RestTemplate restTemplate;

    private static final String NSE_EQUITY_CSV_URL =
            "https://archives.nseindia.com/content/equities/EQUITY_L.csv";

    private static final int BATCH_SIZE = 100;
    private static final String NSE_EXCHANGE_NAME = "NSE";

    /**
     * Fetches the complete list of equity stocks from NSE India and upserts them
     * into the stock_symbols table. Only EQ series stocks (regular equity) are imported.
     * Uses batch inserts of 100 for performance.
     *
     * @return the number of symbols imported or updated
     */
    @Transactional
    public int importNseSymbols() {
        log.info("Starting NSE stock symbol import from url={}", NSE_EQUITY_CSV_URL);
        final long startTime = System.currentTimeMillis();

        final String csvContent = fetchNseCsvContent();
        if (csvContent == null || csvContent.isBlank()) {
            log.warn("NSE CSV content is empty or null, aborting import");
            return 0;
        }

        final List<StockSymbolRecord> parsedSymbols = parseCsvContent(csvContent);
        log.info("Parsed {} EQ-series symbols from NSE CSV", parsedSymbols.size());

        final int upsertedCount = batchUpsertSymbols(parsedSymbols);

        final long duration = System.currentTimeMillis() - startTime;
        log.info("NSE symbol import complete: parsed={}, upserted={}, duration={}ms",
                parsedSymbols.size(), upsertedCount, duration);

        return upsertedCount;
    }

    /**
     * Fetches the raw CSV content from the NSE equity listing URL.
     * Returns null if the request fails, logging a warning.
     *
     * @return the CSV content as a string, or null on failure
     */
    private String fetchNseCsvContent() {
        try {
            final String content = restTemplate.getForObject(NSE_EQUITY_CSV_URL, String.class);
            log.debug("Successfully fetched NSE CSV, contentLength={}", content != null ? content.length() : 0);
            return content;
        } catch (Exception e) {
            log.warn("Failed to fetch NSE equity CSV from url={}: {}", NSE_EQUITY_CSV_URL, e.getMessage());
            return null;
        }
    }

    /**
     * Parses the NSE CSV content and extracts SYMBOL and NAME OF COMPANY columns.
     * Only rows with SERIES = "EQ" (equity stocks) are included.
     * The CSV header row is:
     * SYMBOL, NAME OF COMPANY, SERIES, DATE OF LISTING, PAID UP VALUE, MARKET LOT, ISIN NUMBER, FACE VALUE
     *
     * @param csvContent the raw CSV string
     * @return list of parsed stock symbol records
     */
    private List<StockSymbolRecord> parseCsvContent(String csvContent) {
        final List<StockSymbolRecord> symbols = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            // Skip header line
            String line = reader.readLine();
            if (line == null) {
                log.warn("NSE CSV is empty, no header found");
                return symbols;
            }

            while ((line = reader.readLine()) != null) {
                final String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                final String[] columns = trimmedLine.split(",", -1);
                if (columns.length < 3) {
                    log.debug("Skipping malformed CSV line: {}", trimmedLine);
                    continue;
                }

                final String ticker = columns[0].trim();
                final String companyName = columns[1].trim();
                final String series = columns[2].trim();

                if (!"EQ".equalsIgnoreCase(series)) {
                    continue;
                }

                if (ticker.isEmpty() || companyName.isEmpty()) {
                    log.debug("Skipping row with empty ticker or companyName: {}", trimmedLine);
                    continue;
                }

                symbols.add(new StockSymbolRecord(ticker, companyName));
            }
        } catch (Exception e) {
            log.error("Failed to parse NSE CSV content: {}", e.getMessage(), e);
        }

        return symbols;
    }

    /**
     * Upserts the parsed symbols into the database in batches.
     * If a symbol already exists (by ticker), its company name and exchange are updated.
     * If it does not exist, a new record is created.
     *
     * @param parsedSymbols the list of symbols to upsert
     * @return the total number of symbols inserted or updated
     */
    private int batchUpsertSymbols(List<StockSymbolRecord> parsedSymbols) {
        int upsertedCount = 0;
        final List<StockSymbol> batch = new ArrayList<>(BATCH_SIZE);

        for (final StockSymbolRecord record : parsedSymbols) {
            final Optional<StockSymbol> existingSymbol = stockSymbolRepository.findByTicker(record.ticker());

            final StockSymbol symbol;
            if (existingSymbol.isPresent()) {
                symbol = existingSymbol.get();
                symbol.setCompanyName(record.companyName());
                symbol.setExchange(NSE_EXCHANGE_NAME);
                symbol.setIsActive(true);
            } else {
                symbol = StockSymbol.builder()
                        .ticker(record.ticker())
                        .companyName(record.companyName())
                        .exchange(NSE_EXCHANGE_NAME)
                        .isActive(true)
                        .build();
            }

            batch.add(symbol);

            if (batch.size() >= BATCH_SIZE) {
                stockSymbolRepository.saveAll(batch);
                upsertedCount += batch.size();
                batch.clear();
            }
        }

        // Flush remaining records
        if (!batch.isEmpty()) {
            stockSymbolRepository.saveAll(batch);
            upsertedCount += batch.size();
        }

        return upsertedCount;
    }

    /**
     * Internal record for holding parsed CSV row data before entity mapping.
     *
     * @param ticker the stock ticker symbol
     * @param companyName the full company name
     */
    private record StockSymbolRecord(String ticker, String companyName) {
    }
}
