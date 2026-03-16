package com.stocknews.controller;

import com.stocknews.dto.AddWatchlistItemRequest;
import com.stocknews.dto.WatchlistItemResponse;
import com.stocknews.security.AuthenticatedUser;
import com.stocknews.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing a user's stock watchlist.
 * Allows adding, removing, and listing watched stock symbols.
 * All endpoints require JWT authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
@Tag(name = "Watchlist", description = "Stock symbol watchlist management")
public class WatchlistController {

    private final WatchlistService watchlistService;

    /**
     * Lists all symbols in the user's watchlist.
     *
     * @return list of watched symbols with metadata
     */
    @GetMapping
    @Operation(summary = "Get watchlist", description = "List all stock symbols the user is watching")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved watchlist")
    public ResponseEntity<List<WatchlistItemResponse>> getWatchlist() {
        log.info("GET /api/v1/watchlist");
        final List<WatchlistItemResponse> items = watchlistService.getWatchlist(AuthenticatedUser.getUserId());
        return ResponseEntity.ok(items);
    }

    /**
     * Adds a stock symbol to the user's watchlist.
     *
     * @param request the symbol to add
     * @return the created watchlist item
     */
    @PostMapping
    @Operation(summary = "Add symbol to watchlist", description = "Start watching a stock symbol")
    @ApiResponse(responseCode = "201", description = "Symbol added to watchlist")
    @ApiResponse(responseCode = "409", description = "Symbol already in watchlist")
    public ResponseEntity<WatchlistItemResponse> addSymbol(@Valid @RequestBody AddWatchlistItemRequest request) {
        log.info("POST /api/v1/watchlist — symbol={}", request.getSymbol());
        final WatchlistItemResponse response = watchlistService.addSymbol(AuthenticatedUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Removes a stock symbol from the user's watchlist.
     *
     * @param symbol the stock ticker to remove
     * @return 204 No Content
     */
    @DeleteMapping("/{symbol}")
    @Operation(summary = "Remove symbol from watchlist", description = "Stop watching a stock symbol")
    @ApiResponse(responseCode = "204", description = "Symbol removed from watchlist")
    @ApiResponse(responseCode = "404", description = "Symbol not in watchlist")
    public ResponseEntity<Void> removeSymbol(
            @Parameter(description = "Stock ticker symbol", example = "AAPL")
            @PathVariable String symbol
    ) {
        log.info("DELETE /api/v1/watchlist/{}", symbol);
        watchlistService.removeSymbol(AuthenticatedUser.getUserId(), symbol);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if a specific symbol is in the user's watchlist.
     *
     * @param symbol the stock ticker to check
     * @return JSON with "isWatched" boolean
     */
    @GetMapping("/{symbol}/status")
    @Operation(summary = "Check if symbol is watched", description = "Check if a symbol is in the user's watchlist")
    @ApiResponse(responseCode = "200", description = "Status returned")
    public ResponseEntity<Map<String, Boolean>> checkWatchStatus(
            @Parameter(description = "Stock ticker symbol", example = "AAPL")
            @PathVariable String symbol
    ) {
        final boolean isWatched = watchlistService.isSymbolWatched(AuthenticatedUser.getUserId(), symbol);
        return ResponseEntity.ok(Map.of("isWatched", isWatched));
    }
}
