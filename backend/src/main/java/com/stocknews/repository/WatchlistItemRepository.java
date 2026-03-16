package com.stocknews.repository;

import com.stocknews.model.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for watchlist items.
 * Provides queries for per-user symbol tracking.
 */
@Repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

    /**
     * Finds all symbols a user is watching, ordered by when they were added (newest first).
     * @param userId the user's ID
     * @return list of watchlist items
     */
    List<WatchlistItem> findByUserIdOrderByAddedAtDesc(Long userId);

    /**
     * Finds a specific watchlist item by user and symbol.
     * @param userId the user's ID
     * @param symbol the stock ticker
     * @return the watchlist item if it exists
     */
    Optional<WatchlistItem> findByUserIdAndSymbol(Long userId, String symbol);

    /**
     * Checks if a user is already watching a specific symbol.
     * @param userId the user's ID
     * @param symbol the stock ticker
     * @return true if the symbol is already in the user's watchlist
     */
    boolean existsByUserIdAndSymbol(Long userId, String symbol);

    /**
     * Deletes a watchlist item by user and symbol.
     * @param userId the user's ID
     * @param symbol the stock ticker
     */
    void deleteByUserIdAndSymbol(Long userId, String symbol);

    /**
     * Counts how many symbols a user is watching.
     * @param userId the user's ID
     * @return the count
     */
    long countByUserId(Long userId);
}
