package com.stocknews.repository;

import com.stocknews.model.entity.StockSymbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for stock symbol lookups and autocomplete search.
 */
@Repository
public interface StockSymbolRepository extends JpaRepository<StockSymbol, Long> {

    /**
     * Finds a symbol by its ticker (case-sensitive exact match).
     * @param ticker the stock ticker (e.g., "AAPL")
     * @return the symbol if found
     */
    Optional<StockSymbol> findByTicker(String ticker);

    /**
     * Searches for symbols whose ticker or company name contains the query string.
     * Used for the autocomplete search input. Case-insensitive.
     * @param ticker partial ticker match
     * @param companyName partial company name match
     * @return list of matching symbols (limited by caller)
     */
    List<StockSymbol> findByTickerContainingIgnoreCaseOrCompanyNameContainingIgnoreCaseAndIsActiveTrue(
            String ticker, String companyName
    );

    /**
     * Finds all active symbols (used for scheduled polling).
     * @return list of active symbols
     */
    List<StockSymbol> findByIsActiveTrue();
}
