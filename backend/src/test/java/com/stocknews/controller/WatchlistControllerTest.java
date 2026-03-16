package com.stocknews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stocknews.dto.AddWatchlistItemRequest;
import com.stocknews.dto.WatchlistItemResponse;
import com.stocknews.exception.DuplicateResourceException;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.service.WatchlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for WatchlistController.
 * Uses MockMvc to test the HTTP layer (routing, status codes, request/response serialization)
 * without starting the full Spring context. Security filters are disabled since
 * authentication is not yet implemented (Phase 5).
 */
@WebMvcTest(WatchlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WatchlistService watchlistService;

    private ObjectMapper objectMapper;

    private static final Long DEFAULT_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("GET /api/v1/watchlist — should return list of watchlist items")
    void shouldReturnWatchlistItems() throws Exception {
        WatchlistItemResponse itemOne = buildWatchlistItemResponse(1L, "AAPL", "Apple Inc.", 10);
        WatchlistItemResponse itemTwo = buildWatchlistItemResponse(2L, "TSLA", "Tesla Inc.", 5);

        when(watchlistService.getWatchlist(DEFAULT_USER_ID))
                .thenReturn(List.of(itemOne, itemTwo));

        mockMvc.perform(get("/api/v1/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].companyName").value("Apple Inc."))
                .andExpect(jsonPath("$[0].articleCount").value(10))
                .andExpect(jsonPath("$[1].symbol").value("TSLA"))
                .andExpect(jsonPath("$[1].companyName").value("Tesla Inc."));
    }

    @Test
    @DisplayName("POST /api/v1/watchlist — should add symbol and return 201")
    void shouldAddSymbolAndReturn201() throws Exception {
        AddWatchlistItemRequest request = new AddWatchlistItemRequest("AAPL");

        WatchlistItemResponse response = buildWatchlistItemResponse(1L, "AAPL", "Apple Inc.", 10);

        when(watchlistService.addSymbol(eq(DEFAULT_USER_ID), any(AddWatchlistItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.companyName").value("Apple Inc."))
                .andExpect(jsonPath("$.articleCount").value(10));

        verify(watchlistService).addSymbol(eq(DEFAULT_USER_ID), any(AddWatchlistItemRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/watchlist — should return 400 when symbol is blank")
    void shouldReturn400WhenSymbolIsBlank() throws Exception {
        AddWatchlistItemRequest request = new AddWatchlistItemRequest("");

        mockMvc.perform(post("/api/v1/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.symbol").exists());

        verifyNoInteractions(watchlistService);
    }

    @Test
    @DisplayName("POST /api/v1/watchlist — should return 409 when symbol is duplicate")
    void shouldReturn409WhenSymbolIsDuplicate() throws Exception {
        AddWatchlistItemRequest request = new AddWatchlistItemRequest("AAPL");

        when(watchlistService.addSymbol(eq(DEFAULT_USER_ID), any(AddWatchlistItemRequest.class)))
                .thenThrow(new DuplicateResourceException("Symbol AAPL is already in your watchlist"));

        mockMvc.perform(post("/api/v1/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Symbol AAPL is already in your watchlist"));
    }

    @Test
    @DisplayName("DELETE /api/v1/watchlist/{symbol} — should remove symbol and return 204")
    void shouldRemoveSymbolAndReturn204() throws Exception {
        doNothing().when(watchlistService).removeSymbol(DEFAULT_USER_ID, "AAPL");

        mockMvc.perform(delete("/api/v1/watchlist/AAPL"))
                .andExpect(status().isNoContent());

        verify(watchlistService).removeSymbol(DEFAULT_USER_ID, "AAPL");
    }

    @Test
    @DisplayName("DELETE /api/v1/watchlist/{symbol} — should return 404 when symbol not found")
    void shouldReturn404WhenSymbolNotFoundOnDelete() throws Exception {
        doThrow(new ResourceNotFoundException("Symbol XYZ is not in your watchlist"))
                .when(watchlistService).removeSymbol(DEFAULT_USER_ID, "XYZ");

        mockMvc.perform(delete("/api/v1/watchlist/XYZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Symbol XYZ is not in your watchlist"));
    }

    @Test
    @DisplayName("GET /api/v1/watchlist/{symbol}/status — should return isWatched true")
    void shouldReturnIsWatchedTrue() throws Exception {
        when(watchlistService.isSymbolWatched(DEFAULT_USER_ID, "AAPL")).thenReturn(true);

        mockMvc.perform(get("/api/v1/watchlist/AAPL/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isWatched").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/watchlist/{symbol}/status — should return isWatched false")
    void shouldReturnIsWatchedFalse() throws Exception {
        when(watchlistService.isSymbolWatched(DEFAULT_USER_ID, "XYZ")).thenReturn(false);

        mockMvc.perform(get("/api/v1/watchlist/XYZ/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isWatched").value(false));
    }

    /**
     * Builds a WatchlistItemResponse for test assertions.
     *
     * @param id           the item ID
     * @param symbol       the stock ticker
     * @param companyName  the company name
     * @param articleCount number of articles
     * @return a populated WatchlistItemResponse
     */
    private WatchlistItemResponse buildWatchlistItemResponse(Long id, String symbol, String companyName, long articleCount) {
        return WatchlistItemResponse.builder()
                .id(id)
                .symbol(symbol)
                .companyName(companyName)
                .articleCount(articleCount)
                .addedAt(OffsetDateTime.now())
                .build();
    }
}
