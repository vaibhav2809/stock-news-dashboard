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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WatchlistService.
 * Uses Mockito to mock repository dependencies and verify business logic
 * for adding, removing, and querying watchlist symbols.
 */
@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private WatchlistItemRepository watchlistItemRepository;

    @Mock
    private StockSymbolRepository stockSymbolRepository;

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @InjectMocks
    private WatchlistService watchlistService;

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 10L;

    @Nested
    @DisplayName("addSymbol")
    class AddSymbol {

        @Test
        @DisplayName("Should add symbol successfully and return enriched response")
        void shouldAddSymbolSuccessfully() {
            AddWatchlistItemRequest request = new AddWatchlistItemRequest("AAPL");

            WatchlistItem savedItem = buildWatchlistItem(ITEM_ID, "AAPL");

            when(watchlistItemRepository.existsByUserIdAndSymbol(USER_ID, "AAPL")).thenReturn(false);
            when(watchlistItemRepository.save(any(WatchlistItem.class))).thenReturn(savedItem);
            when(stockSymbolRepository.findByTicker("AAPL"))
                    .thenReturn(Optional.of(buildStockSymbol("AAPL", "Apple Inc.")));
            when(newsArticleRepository.countBySymbol("AAPL")).thenReturn(5L);

            WatchlistItemResponse response = watchlistService.addSymbol(USER_ID, request);

            assertNotNull(response);
            assertEquals(ITEM_ID, response.getId());
            assertEquals("AAPL", response.getSymbol());
            assertEquals("Apple Inc.", response.getCompanyName());
            assertEquals(5L, response.getArticleCount());
            verify(watchlistItemRepository).existsByUserIdAndSymbol(USER_ID, "AAPL");
            verify(watchlistItemRepository).save(any(WatchlistItem.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when symbol already in watchlist")
        void shouldThrowDuplicateWhenSymbolAlreadyExists() {
            AddWatchlistItemRequest request = new AddWatchlistItemRequest("AAPL");

            when(watchlistItemRepository.existsByUserIdAndSymbol(USER_ID, "AAPL")).thenReturn(true);

            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> watchlistService.addSymbol(USER_ID, request)
            );

            assertTrue(exception.getMessage().contains("AAPL"));
            verify(watchlistItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should normalize symbol to uppercase before saving")
        void shouldNormalizeSymbolToUppercase() {
            AddWatchlistItemRequest request = new AddWatchlistItemRequest("  aapl  ");

            WatchlistItem savedItem = buildWatchlistItem(ITEM_ID, "AAPL");

            when(watchlistItemRepository.existsByUserIdAndSymbol(USER_ID, "AAPL")).thenReturn(false);
            when(watchlistItemRepository.save(any(WatchlistItem.class))).thenReturn(savedItem);
            when(stockSymbolRepository.findByTicker("AAPL")).thenReturn(Optional.empty());
            when(newsArticleRepository.countBySymbol("AAPL")).thenReturn(0L);

            WatchlistItemResponse response = watchlistService.addSymbol(USER_ID, request);

            assertEquals("AAPL", response.getSymbol());
            verify(watchlistItemRepository).existsByUserIdAndSymbol(USER_ID, "AAPL");
        }
    }

    @Nested
    @DisplayName("getWatchlist")
    class GetWatchlist {

        @Test
        @DisplayName("Should return enriched watchlist items for a user")
        void shouldReturnEnrichedWatchlistItems() {
            WatchlistItem itemOne = buildWatchlistItem(1L, "AAPL");
            WatchlistItem itemTwo = buildWatchlistItem(2L, "TSLA");

            when(watchlistItemRepository.findByUserIdOrderByAddedAtDesc(USER_ID))
                    .thenReturn(List.of(itemOne, itemTwo));
            when(stockSymbolRepository.findByTicker("AAPL"))
                    .thenReturn(Optional.of(buildStockSymbol("AAPL", "Apple Inc.")));
            when(stockSymbolRepository.findByTicker("TSLA"))
                    .thenReturn(Optional.of(buildStockSymbol("TSLA", "Tesla Inc.")));
            when(newsArticleRepository.countBySymbol("AAPL")).thenReturn(10L);
            when(newsArticleRepository.countBySymbol("TSLA")).thenReturn(3L);

            List<WatchlistItemResponse> responses = watchlistService.getWatchlist(USER_ID);

            assertEquals(2, responses.size());
            assertEquals("AAPL", responses.get(0).getSymbol());
            assertEquals("Apple Inc.", responses.get(0).getCompanyName());
            assertEquals(10L, responses.get(0).getArticleCount());
            assertEquals("TSLA", responses.get(1).getSymbol());
            assertEquals("Tesla Inc.", responses.get(1).getCompanyName());
            assertEquals(3L, responses.get(1).getArticleCount());
        }

        @Test
        @DisplayName("Should return empty list when user has no watchlist items")
        void shouldReturnEmptyListWhenNoItems() {
            when(watchlistItemRepository.findByUserIdOrderByAddedAtDesc(USER_ID))
                    .thenReturn(List.of());

            List<WatchlistItemResponse> responses = watchlistService.getWatchlist(USER_ID);

            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("removeSymbol")
    class RemoveSymbol {

        @Test
        @DisplayName("Should remove symbol successfully when it exists in watchlist")
        void shouldRemoveSymbolSuccessfully() {
            when(watchlistItemRepository.existsByUserIdAndSymbol(USER_ID, "AAPL")).thenReturn(true);

            watchlistService.removeSymbol(USER_ID, "AAPL");

            verify(watchlistItemRepository).deleteByUserIdAndSymbol(USER_ID, "AAPL");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when symbol not in watchlist")
        void shouldThrowResourceNotFoundWhenSymbolNotInWatchlist() {
            when(watchlistItemRepository.existsByUserIdAndSymbol(USER_ID, "XYZ")).thenReturn(false);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> watchlistService.removeSymbol(USER_ID, "xyz")
            );

            assertTrue(exception.getMessage().contains("XYZ"));
            verify(watchlistItemRepository, never()).deleteByUserIdAndSymbol(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("isSymbolWatched")
    class IsSymbolWatched {

        @Test
        @DisplayName("Should return true when symbol is in watchlist")
        void shouldReturnTrueWhenSymbolIsWatched() {
            when(watchlistItemRepository.existsByUserIdAndSymbol(USER_ID, "AAPL")).thenReturn(true);

            assertTrue(watchlistService.isSymbolWatched(USER_ID, "AAPL"));
        }

        @Test
        @DisplayName("Should return false when symbol is not in watchlist")
        void shouldReturnFalseWhenSymbolIsNotWatched() {
            when(watchlistItemRepository.existsByUserIdAndSymbol(USER_ID, "XYZ")).thenReturn(false);

            assertFalse(watchlistService.isSymbolWatched(USER_ID, "XYZ"));
        }
    }

    /**
     * Builds a WatchlistItem entity for testing.
     *
     * @param id     the item ID
     * @param symbol the stock ticker symbol
     * @return a fully initialized WatchlistItem
     */
    private WatchlistItem buildWatchlistItem(Long id, String symbol) {
        return WatchlistItem.builder()
                .id(id)
                .userId(USER_ID)
                .symbol(symbol)
                .addedAt(OffsetDateTime.now())
                .build();
    }

    /**
     * Builds a StockSymbol entity for testing.
     *
     * @param ticker      the stock ticker
     * @param companyName the company name
     * @return a fully initialized StockSymbol
     */
    private StockSymbol buildStockSymbol(String ticker, String companyName) {
        return StockSymbol.builder()
                .id(1L)
                .ticker(ticker)
                .companyName(companyName)
                .exchange("NASDAQ")
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
