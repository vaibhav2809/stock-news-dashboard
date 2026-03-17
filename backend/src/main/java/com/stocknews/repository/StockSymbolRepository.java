package com.stocknews.repository;

import com.stocknews.model.entity.StockSymbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for stock symbol lookups, autocomplete search, and import operations.
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
     * Checks whether a stock symbol with the given ticker already exists.
     * @param ticker the stock ticker to check
     * @return true if a symbol with this ticker exists
     */
    boolean existsByTicker(String ticker);

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
     * Searches for active symbols whose ticker or company name matches the query,
     * optionally filtered by exchange. When exchange is null, all exchanges are included.
     *
     * @param query the search query (matched against ticker and company name)
     * @param exchange the exchange filter (e.g., "NSE"), or null for all exchanges
     * @return list of matching active symbols
     */
    @Query("""
            SELECT s FROM StockSymbol s
            WHERE s.isActive = true
              AND (LOWER(s.ticker) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(s.companyName) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:#{#exchange == null} = true OR s.exchange = :exchange)
            ORDER BY s.ticker ASC
            """)
    List<StockSymbol> searchByQueryAndExchange(
            @Param("query") String query,
            @Param("exchange") String exchange
    );

    /**
     * Finds all symbols listed on a specific exchange.
     * @param exchange the exchange name (e.g., "NSE", "NASDAQ")
     * @return list of symbols on that exchange
     */
    List<StockSymbol> findByExchange(String exchange);

    /**
     * Finds all active symbols (used for scheduled polling).
     * @return list of active symbols
     */
    List<StockSymbol> findByIsActiveTrue();
}
