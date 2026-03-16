package com.stocknews.controller;

import com.stocknews.model.entity.StockSymbol;
import com.stocknews.repository.StockSymbolRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for StockSymbolController.
 * Tests the autocomplete search endpoint.
 */
@WebMvcTest(StockSymbolController.class)
@AutoConfigureMockMvc(addFilters = false)
class StockSymbolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockSymbolRepository stockSymbolRepository;

    @Test
    @DisplayName("GET /api/v1/symbols/search — should return matching symbols")
    void shouldReturnMatchingSymbols() throws Exception {
        StockSymbol apple = StockSymbol.builder()
                .id(1L)
                .ticker("AAPL")
                .companyName("Apple Inc.")
                .exchange("NASDAQ")
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(stockSymbolRepository
                .findByTickerContainingIgnoreCaseOrCompanyNameContainingIgnoreCaseAndIsActiveTrue("AAPL", "AAPL"))
                .thenReturn(List.of(apple));

        mockMvc.perform(get("/api/v1/symbols/search")
                        .param("query", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[0].companyName").value("Apple Inc."))
                .andExpect(jsonPath("$[0].exchange").value("NASDAQ"));
    }

    @Test
    @DisplayName("GET /api/v1/symbols/search — should return empty list when no match")
    void shouldReturnEmptyListWhenNoMatch() throws Exception {
        when(stockSymbolRepository
                .findByTickerContainingIgnoreCaseOrCompanyNameContainingIgnoreCaseAndIsActiveTrue("XYZ", "XYZ"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/symbols/search")
                        .param("query", "XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/symbols/search — should return 400 when query param is missing")
    void shouldReturn400WhenQueryParamMissing() throws Exception {
        mockMvc.perform(get("/api/v1/symbols/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/symbols/search — should match by company name")
    void shouldMatchByCompanyName() throws Exception {
        StockSymbol apple = StockSymbol.builder()
                .id(1L)
                .ticker("AAPL")
                .companyName("Apple Inc.")
                .exchange("NASDAQ")
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(stockSymbolRepository
                .findByTickerContainingIgnoreCaseOrCompanyNameContainingIgnoreCaseAndIsActiveTrue("Apple", "Apple"))
                .thenReturn(List.of(apple));

        mockMvc.perform(get("/api/v1/symbols/search")
                        .param("query", "Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"));
    }
}
