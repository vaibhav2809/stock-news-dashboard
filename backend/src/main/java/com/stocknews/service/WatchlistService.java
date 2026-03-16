package com.stocknews.service;

import com.stocknews.dto.AddWatchlistItemRequest;
import com.stocknews.dto.WatchlistItemResponse;
import com.stocknews.exception.DuplicateResourceException;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.model.entity.StockSymbol;
import com.stocknews.model.entity.WatchlistItem;
import com.stocknews.repository.NewsArticleRepository;
import com.stocknews.repository.StockSymbolRepository;
import com.stocknews.repository.WatchlistItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing a user's stock watchlist.
 * Handles adding/removing symbols and enriching responses with company names and article counts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final StockSymbolRepository stockSymbolRepository;
    private final NewsArticleRepository newsArticleRepository;

    /**
     * Adds a stock symbol to the user's watchlist.
     * Normalizes the symbol to uppercase before saving.
     *
     * @param userId the user's ID
     * @param request contains the symbol to add
     * @return the created watchlist item with enriched data
     * @throws DuplicateResourceException if the symbol is already in the watchlist
     */
    @Transactional
    public WatchlistItemResponse addSymbol(Long userId, AddWatchlistItemRequest request) {
        final String normalizedSymbol = request.getSymbol().trim().toUpperCase();
        log.info("Adding symbol={} to watchlist for userId={}", normalizedSymbol, userId);

        if (watchlistItemRepository.existsByUserIdAndSymbol(userId, normalizedSymbol)) {
            throw new DuplicateResourceException("Symbol " + normalizedSymbol + " is already in your watchlist");
        }

        final WatchlistItem item = WatchlistItem.builder()
                .userId(userId)
                .symbol(normalizedSymbol)
                .build();

        final WatchlistItem savedItem = watchlistItemRepository.save(item);
        log.info("Added symbol={} to watchlist for userId={}, itemId={}", normalizedSymbol, userId, savedItem.getId());

        return enrichResponse(savedItem);
    }

    /**
     * Retrieves all symbols in a user's watchlist, enriched with company names and article counts.
     *
     * @param userId the user's ID
     * @return list of watchlist items with metadata
     */
    @Transactional(readOnly = true)
    public List<WatchlistItemResponse> getWatchlist(Long userId) {
        log.debug("Fetching watchlist for userId={}", userId);
        final List<WatchlistItem> items = watchlistItemRepository.findByUserIdOrderByAddedAtDesc(userId);
        return items.stream()
                .map(this::enrichResponse)
                .toList();
    }

    /**
     * Removes a symbol from the user's watchlist.
     *
     * @param userId the user's ID
     * @param symbol the stock ticker to remove
     * @throws ResourceNotFoundException if the symbol is not in the watchlist
     */
    @Transactional
    public void removeSymbol(Long userId, String symbol) {
        final String normalizedSymbol = symbol.trim().toUpperCase();
        log.info("Removing symbol={} from watchlist for userId={}", normalizedSymbol, userId);

        if (!watchlistItemRepository.existsByUserIdAndSymbol(userId, normalizedSymbol)) {
            throw new ResourceNotFoundException("Symbol " + normalizedSymbol + " is not in your watchlist");
        }

        watchlistItemRepository.deleteByUserIdAndSymbol(userId, normalizedSymbol);
        log.info("Removed symbol={} from watchlist for userId={}", normalizedSymbol, userId);
    }

    /**
     * Checks if a specific symbol is in the user's watchlist.
     *
     * @param userId the user's ID
     * @param symbol the stock ticker to check
     * @return true if the symbol is being watched
     */
    @Transactional(readOnly = true)
    public boolean isSymbolWatched(Long userId, String symbol) {
        return watchlistItemRepository.existsByUserIdAndSymbol(userId, symbol.trim().toUpperCase());
    }

    /**
     * Enriches a WatchlistItem entity with the company name (from stock_symbols table)
     * and the total article count for that symbol.
     *
     * @param item the raw watchlist item entity
     * @return the enriched response DTO
     */
    private WatchlistItemResponse enrichResponse(WatchlistItem item) {
        final String companyName = stockSymbolRepository.findByTicker(item.getSymbol())
                .map(StockSymbol::getCompanyName)
                .orElse(item.getSymbol());

        final long articleCount = newsArticleRepository.countBySymbol(item.getSymbol());

        return WatchlistItemResponse.fromEntity(item, companyName, articleCount);
    }
}
