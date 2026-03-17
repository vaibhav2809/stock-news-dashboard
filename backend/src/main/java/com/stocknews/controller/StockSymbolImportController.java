package com.stocknews.controller;

import com.stocknews.service.StockSymbolImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin controller for bulk-importing stock symbols from external exchanges.
 * All endpoints require authentication (protected by SecurityConfig's .anyRequest().authenticated()).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/import-symbols")
@RequiredArgsConstructor
@Tag(name = "Admin - Symbol Import", description = "Bulk import stock symbols from exchanges (requires authentication)")
public class StockSymbolImportController {

    private final StockSymbolImportService stockSymbolImportService;

    /**
     * Triggers a bulk import of all NSE India equity symbols into the stock_symbols table.
     * Fetches the official NSE equity CSV, parses EQ-series stocks, and upserts them.
     * This endpoint requires authentication.
     *
     * @return a JSON object containing the count of imported symbols and a status message
     */
    @PostMapping("/nse")
    @Operation(
            summary = "Import NSE stock symbols",
            description = "Fetches all equity (EQ series) stock symbols from NSE India and imports them into the database. Requires authentication."
    )
    @ApiResponse(responseCode = "200", description = "Import completed successfully")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<Map<String, Object>> importNseSymbols() {
        log.info("POST /api/v1/admin/import-symbols/nse — triggered NSE symbol import");

        final int importedCount = stockSymbolImportService.importNseSymbols();

        final Map<String, Object> response = Map.of(
                "imported", importedCount,
                "message", "Successfully imported NSE symbols"
        );

        log.info("NSE symbol import completed: imported={}", importedCount);
        return ResponseEntity.ok(response);
    }
}
