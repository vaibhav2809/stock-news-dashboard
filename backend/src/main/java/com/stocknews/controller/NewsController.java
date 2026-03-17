package com.stocknews.controller;

import com.stocknews.dto.NewsArticleResponse;
import com.stocknews.dto.NewsSearchRequest;
import com.stocknews.dto.PaginatedResponse;
import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import com.stocknews.service.NewsAggregatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for news article search, retrieval, and aggregation.
 * All GET endpoints are publicly accessible. POST endpoints for fetch/backfill are also public.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "News", description = "News article search and retrieval endpoints")
public class NewsController {

    private final NewsAggregatorService newsAggregatorService;

    /**
     * Searches for news articles with optional filters.
     * Supports filtering by symbols, source, sentiment, date range, and keyword.
     *
     * @param symbols comma-separated stock ticker symbols (e.g., "AAPL,TSLA")
     * @param source filter by news source (FINNHUB or NEWSDATA_IO)
     * @param sentiment filter by sentiment (POSITIVE, NEGATIVE, NEUTRAL)
     * @param fromDate start date (inclusive, format: yyyy-MM-dd)
     * @param toDate end date (inclusive, format: yyyy-MM-dd)
     * @param keyword free-text keyword to search in article titles and summaries
     * @param page page number (zero-based, default: 0)
     * @param size results per page (default: 20)
     * @return paginated list of matching articles
     */
    @GetMapping
    @Operation(
            summary = "Search news articles",
            description = "Search and filter news articles by symbols, source, sentiment, date range, and keyword. All filters are optional."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved news articles")
    public ResponseEntity<PaginatedResponse<NewsArticleResponse>> searchNews(
            @Parameter(description = "Stock ticker symbols (comma-separated)", example = "AAPL,TSLA")
            @RequestParam(required = false) List<String> symbols,

            @Parameter(description = "Filter by news source")
            @RequestParam(required = false) NewsSource source,

            @Parameter(description = "Filter by sentiment")
            @RequestParam(required = false) Sentiment sentiment,

            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2026-03-01")
            @RequestParam(required = false) LocalDate fromDate,

            @Parameter(description = "End date (yyyy-MM-dd)", example = "2026-03-16")
            @RequestParam(required = false) LocalDate toDate,

            @Parameter(description = "Free-text keyword to search in titles and summaries", example = "semiconductor")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Results per page")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/v1/news — symbols={}, source={}, sentiment={}, keyword={}, page={}, size={}",
                symbols, source, sentiment, keyword, page, size);

        final NewsSearchRequest request = NewsSearchRequest.builder()
                .symbols(symbols)
                .source(source)
                .sentiment(sentiment)
                .fromDate(fromDate)
                .toDate(toDate)
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();

        final PaginatedResponse<NewsArticleResponse> response = newsAggregatorService.searchNews(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for news articles by keyword in their title or summary.
     * This is a dedicated endpoint for keyword-only searches without other filters.
     *
     * @param keyword the search keyword (case-insensitive partial match)
     * @param page page number (zero-based, default: 0)
     * @param size results per page (default: 20)
     * @return paginated list of matching articles
     */
    @GetMapping("/search")
    @Operation(
            summary = "Search news by keyword",
            description = "Search stored news articles by keyword in title and summary. Returns paginated results ordered by publication date."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching articles")
    public ResponseEntity<PaginatedResponse<NewsArticleResponse>> searchByKeyword(
            @Parameter(description = "Search keyword", example = "semiconductor")
            @RequestParam String keyword,

            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Results per page")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/v1/news/search — keyword={}, page={}, size={}", keyword, page, size);

        final String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.isEmpty()) {
            return ResponseEntity.ok(PaginatedResponse.of(List.of(), 0, 0, 0));
        }

        final PaginatedResponse<NewsArticleResponse> response =
                newsAggregatorService.searchByKeyword(trimmedKeyword, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single news article by its database ID.
     *
     * @param id the article ID
     * @return the article details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get article by ID", description = "Retrieve a single news article by its database ID")
    @ApiResponse(responseCode = "200", description = "Article found")
    @ApiResponse(responseCode = "404", description = "Article not found")
    public ResponseEntity<NewsArticleResponse> getArticleById(
            @Parameter(description = "Article ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/news/{}", id);
        final NewsArticleResponse article = newsAggregatorService.getArticleById(id);
        return ResponseEntity.ok(article);
    }

    /**
     * Retrieves trending articles (published in the last 24 hours).
     *
     * @param page page number (zero-based, default: 0)
     * @param size results per page (default: 20)
     * @return paginated list of trending articles
     */
    @GetMapping("/trending")
    @Operation(summary = "Get trending news", description = "Retrieve the most recent articles from the last 24 hours across all symbols")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved trending articles")
    public ResponseEntity<PaginatedResponse<NewsArticleResponse>> getTrendingNews(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Results per page")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/v1/news/trending — page={}, size={}", page, size);
        final PaginatedResponse<NewsArticleResponse> response = newsAggregatorService.getTrendingNews(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Manually triggers news fetching for the given symbols.
     * Useful for on-demand refresh. Runs synchronously and returns when complete.
     *
     * @param symbols list of stock ticker symbols to fetch news for
     * @return confirmation message
     */
    @PostMapping("/fetch")
    @Operation(summary = "Trigger news fetch", description = "Manually trigger news fetching from all sources for the given symbols")
    @ApiResponse(responseCode = "200", description = "News fetch completed")
    public ResponseEntity<String> triggerFetch(
            @Parameter(description = "Stock ticker symbols to fetch", example = "[\"AAPL\", \"TSLA\"]")
            @RequestBody List<String> symbols
    ) {
        log.info("POST /api/v1/news/fetch — symbols={}", symbols);
        newsAggregatorService.fetchNewsForSymbols(symbols);
        return ResponseEntity.ok("News fetch completed for symbols: " + symbols);
    }

    /**
     * Re-runs sentiment analysis on all existing articles in the database.
     * Use this when articles were stored before the sentiment analyzer was added,
     * or after updating the keyword dictionary.
     *
     * @return the number of articles updated
     */
    @PostMapping("/backfill-sentiment")
    @Operation(summary = "Backfill sentiment", description = "Re-run sentiment analysis on all existing articles")
    @ApiResponse(responseCode = "200", description = "Backfill completed")
    public ResponseEntity<String> backfillSentiment() {
        log.info("POST /api/v1/news/backfill-sentiment");
        final int updatedCount = newsAggregatorService.backfillSentiment();
        return ResponseEntity.ok("Sentiment backfill completed. Updated " + updatedCount + " articles.");
    }
}
