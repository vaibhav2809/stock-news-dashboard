package com.stocknews.controller;

import com.stocknews.dto.SentimentDistribution;
import com.stocknews.dto.SentimentTimelineEntry;
import com.stocknews.service.SentimentService;
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
 * REST controller for sentiment analysis endpoints.
 * Provides per-symbol sentiment distributions and timeline data for charting.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sentiment")
@RequiredArgsConstructor
@Tag(name = "Sentiment", description = "Sentiment analysis and trend endpoints")
public class SentimentController {

    private final SentimentService sentimentService;

    /**
     * Returns the sentiment distribution for a specific stock symbol.
     *
     * @param symbol the stock ticker
     * @return distribution with positive/negative/neutral counts and percentages
     */
    @GetMapping("/{symbol}")
    @Operation(summary = "Get sentiment distribution", description = "Returns sentiment breakdown for a stock symbol")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved sentiment distribution")
    public ResponseEntity<SentimentDistribution> getDistribution(
            @Parameter(description = "Stock ticker symbol", example = "AAPL")
            @PathVariable String symbol
    ) {
        log.info("GET /api/v1/sentiment/{}", symbol);
        final SentimentDistribution distribution = sentimentService.getDistribution(symbol.toUpperCase());
        return ResponseEntity.ok(distribution);
    }

    /**
     * Returns sentiment distributions for multiple symbols at once.
     *
     * @param symbols comma-separated stock tickers
     * @return list of distributions, one per symbol
     */
    @GetMapping
    @Operation(summary = "Get sentiment for multiple symbols",
            description = "Returns sentiment distributions for multiple stock symbols")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved sentiment data")
    public ResponseEntity<List<SentimentDistribution>> getDistributions(
            @Parameter(description = "Comma-separated stock tickers", example = "AAPL,TSLA,MSFT")
            @RequestParam List<String> symbols
    ) {
        log.info("GET /api/v1/sentiment — symbols={}", symbols);
        final List<String> normalizedSymbols = symbols.stream()
                .map(String::toUpperCase)
                .toList();
        final List<SentimentDistribution> distributions = sentimentService.getDistributions(normalizedSymbols);
        return ResponseEntity.ok(distributions);
    }

    /**
     * Returns a day-by-day sentiment timeline for a stock symbol.
     * Used to render line/bar charts showing sentiment trends over time.
     *
     * @param symbol the stock ticker
     * @param days number of days to look back (default 30, max 90)
     * @return list of daily sentiment data points
     */
    @GetMapping("/{symbol}/timeline")
    @Operation(summary = "Get sentiment timeline", description = "Returns day-by-day sentiment data for charting")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved timeline")
    public ResponseEntity<List<SentimentTimelineEntry>> getTimeline(
            @Parameter(description = "Stock ticker symbol", example = "AAPL")
            @PathVariable String symbol,
            @Parameter(description = "Number of days to look back (max 90)", example = "30")
            @RequestParam(defaultValue = "30") int days
    ) {
        log.info("GET /api/v1/sentiment/{}/timeline — days={}", symbol, days);
        final int clampedDays = Math.min(Math.max(days, 1), 90);
        final List<SentimentTimelineEntry> timeline = sentimentService.getTimeline(symbol.toUpperCase(), clampedDays);
        return ResponseEntity.ok(timeline);
    }
}
