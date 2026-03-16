package com.stocknews.controller;

import com.stocknews.dto.StockSymbolResponse;
import com.stocknews.repository.StockSymbolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for stock symbol search and autocomplete.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/symbols")
@RequiredArgsConstructor
@Tag(name = "Symbols", description = "Stock symbol search and autocomplete")
public class StockSymbolController {

    private final StockSymbolRepository stockSymbolRepository;

    /**
     * Searches for stock symbols by ticker or company name.
     * Used by the autocomplete search input on the frontend.
     *
     * @param query the search query (matches ticker or company name)
     * @return list of matching symbols
     */
    @GetMapping("/search")
    @Operation(summary = "Search stock symbols", description = "Search for stock symbols by ticker or company name (autocomplete)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching symbols")
    public ResponseEntity<List<StockSymbolResponse>> searchSymbols(
            @Parameter(description = "Search query (ticker or company name)", example = "AAPL")
            @RequestParam String query
    ) {
        log.debug("GET /api/v1/symbols/search — query={}", query);

        final List<StockSymbolResponse> results = stockSymbolRepository
                .findByTickerContainingIgnoreCaseOrCompanyNameContainingIgnoreCaseAndIsActiveTrue(query, query)
                .stream()
                .map(StockSymbolResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(results);
    }
}
