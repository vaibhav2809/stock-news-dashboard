package com.stocknews.service;

import com.stocknews.dto.NewsArticleResponse;
import com.stocknews.dto.NewsSearchRequest;
import com.stocknews.dto.PaginatedResponse;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.model.entity.NewsArticle;
import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import com.stocknews.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Orchestrates news fetching from multiple sources (Finnhub, NewsData.io),
 * deduplicates results, persists new articles, and serves paginated queries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsAggregatorService {

    private final FinnhubNewsService finnhubNewsService;
    private final NewsDataIoService newsDataIoService;
    private final NewsArticleRepository newsArticleRepository;
    private final KeywordSentimentAnalyzer sentimentAnalyzer;

    private static final int TRENDING_HOURS_WINDOW = 24;

    /**
     * Fetches news from all configured sources for the given symbol,
     * deduplicates, and persists new articles to the database.
     * Both API calls run in parallel for faster response times.
     *
     * @param symbol the stock ticker to fetch news for
     */
    public void fetchAndStoreNews(String symbol) {
        log.info("Starting news aggregation for symbol={}", symbol);
        final long startTime = System.currentTimeMillis();

        final LocalDate toDate = LocalDate.now();
        final LocalDate fromDate = toDate.minusDays(7);

        // Fetch from both sources in parallel
        final CompletableFuture<List<NewsArticle>> finnhubFuture = CompletableFuture.supplyAsync(
                () -> finnhubNewsService.fetchNews(symbol, fromDate, toDate)
        );
        final CompletableFuture<List<NewsArticle>> newsdataFuture = CompletableFuture.supplyAsync(
                () -> newsDataIoService.fetchNews(symbol)
        );

        // Wait for both to complete, handle failures individually
        final List<NewsArticle> finnhubArticles = finnhubFuture
                .exceptionally(e -> {
                    log.error("Finnhub fetch failed for symbol={}: {}", symbol, e.getMessage());
                    return List.of();
                })
                .join();

        final List<NewsArticle> newsdataArticles = newsdataFuture
                .exceptionally(e -> {
                    log.error("NewsData.io fetch failed for symbol={}: {}", symbol, e.getMessage());
                    return List.of();
                })
                .join();

        // Merge results
        final List<NewsArticle> allArticles = new ArrayList<>();
        allArticles.addAll(finnhubArticles);
        allArticles.addAll(newsdataArticles);

        // Deduplicate, analyze sentiment, and persist
        int savedCount = 0;
        int duplicateCount = 0;
        for (final NewsArticle article : allArticles) {
            if (!newsArticleRepository.existsByFingerprint(article.getFingerprint())) {
                // Run keyword-based sentiment analysis before saving
                final KeywordSentimentAnalyzer.SentimentResult sentimentResult =
                        sentimentAnalyzer.analyze(article.getTitle(), article.getSummary());
                article.setSentiment(sentimentResult.sentiment());
                article.setSentimentScore(sentimentResult.score());

                newsArticleRepository.save(article);
                savedCount++;
            } else {
                duplicateCount++;
            }
        }

        final long duration = System.currentTimeMillis() - startTime;
        log.info("Aggregation complete for symbol={}: fetched={}, saved={}, duplicates={}, duration={}ms",
                symbol, allArticles.size(), savedCount, duplicateCount, duration);
    }

    /**
     * Searches for news articles with optional filters (symbols, source, sentiment, date range).
     * Results are cached in Redis for 15 minutes.
     *
     * @param request the search parameters
     * @return paginated list of matching articles
     */
    @Cacheable(value = "news", key = "#request.hashCode()", unless = "#result.totalElements == 0")
    public PaginatedResponse<NewsArticleResponse> searchNews(NewsSearchRequest request) {
        log.debug("Searching news with filters: symbols={}, source={}, sentiment={}, keyword={}, page={}, size={}",
                request.getSymbols(), request.getSource(), request.getSentiment(),
                request.getKeyword(), request.getPage(), request.getSize());

        final Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        final OffsetDateTime fromDateTime = request.getFromDate() != null
                ? request.getFromDate().atStartOfDay().atOffset(ZoneOffset.UTC)
                : null;

        final OffsetDateTime toDateTime = request.getToDate() != null
                ? request.getToDate().plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)
                : null;

        final boolean hasSymbols = request.getSymbols() != null && !request.getSymbols().isEmpty();
        final List<String> symbols = hasSymbols ? request.getSymbols() : List.of();

        final String keyword = request.getKeyword() != null ? request.getKeyword().trim() : "";
        final boolean hasKeyword = !keyword.isEmpty();

        final Page<NewsArticle> page = newsArticleRepository.searchArticles(
                symbols,
                hasSymbols,
                request.getSource(),
                request.getSentiment(),
                fromDateTime,
                toDateTime,
                keyword,
                hasKeyword,
                pageable
        );

        final List<NewsArticleResponse> articles = page.getContent().stream()
                .map(NewsArticleResponse::fromEntity)
                .toList();

        return PaginatedResponse.of(articles, page.getNumber(), page.getTotalPages(), page.getTotalElements());
    }

    /**
     * Searches for news articles by keyword in title and summary.
     * Returns paginated results ordered by publication date descending.
     *
     * @param keyword the search keyword (case-insensitive partial match)
     * @param page page number (zero-based)
     * @param size results per page
     * @return paginated list of matching articles
     */
    public PaginatedResponse<NewsArticleResponse> searchByKeyword(String keyword, int page, int size) {
        log.debug("Keyword search: keyword={}, page={}, size={}", keyword, page, size);

        final Pageable pageable = PageRequest.of(page, size);
        final Page<NewsArticle> articlePage = newsArticleRepository.searchByKeyword(keyword, pageable);

        final List<NewsArticleResponse> articles = articlePage.getContent().stream()
                .map(NewsArticleResponse::fromEntity)
                .toList();

        return PaginatedResponse.of(articles, articlePage.getNumber(),
                articlePage.getTotalPages(), articlePage.getTotalElements());
    }

    /**
     * Retrieves a single news article by its ID.
     *
     * @param articleId the article's database ID
     * @return the article response DTO
     * @throws ResourceNotFoundException if no article exists with the given ID
     */
    public NewsArticleResponse getArticleById(Long articleId) {
        final NewsArticle article = newsArticleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("News article not found with id=" + articleId));
        return NewsArticleResponse.fromEntity(article);
    }

    /**
     * Retrieves trending articles (published in the last 24 hours) across all symbols.
     * Cached in Redis for 1 hour.
     *
     * @param page page number (zero-based)
     * @param size results per page
     * @return paginated list of trending articles
     */
    @Cacheable(value = "trending", key = "'trending-' + #page + '-' + #size")
    public PaginatedResponse<NewsArticleResponse> getTrendingNews(int page, int size) {
        log.debug("Fetching trending news: page={}, size={}", page, size);

        final OffsetDateTime since = OffsetDateTime.now().minusHours(TRENDING_HOURS_WINDOW);
        final Pageable pageable = PageRequest.of(page, size);

        final Page<NewsArticle> articlePage = newsArticleRepository
                .findByPublishedAtAfterOrderByPublishedAtDesc(since, pageable);

        final List<NewsArticleResponse> articles = articlePage.getContent().stream()
                .map(NewsArticleResponse::fromEntity)
                .toList();

        return PaginatedResponse.of(articles, articlePage.getNumber(),
                articlePage.getTotalPages(), articlePage.getTotalElements());
    }

    /**
     * Triggers a fetch-and-store cycle for multiple symbols.
     * Used by the scheduled polling job and manual refresh endpoints.
     *
     * @param symbols list of stock tickers to fetch news for
     */
    public void fetchNewsForSymbols(List<String> symbols) {
        log.info("Batch fetching news for {} symbols", symbols.size());
        for (final String symbol : symbols) {
            try {
                fetchAndStoreNews(symbol);
            } catch (Exception e) {
                log.error("Failed to fetch news for symbol={}: {}", symbol, e.getMessage(), e);
            }
        }
    }

    /**
     * Re-runs sentiment analysis on all existing articles in the database.
     * Useful when articles were stored before the sentiment analyzer was integrated,
     * or after the keyword dictionary is updated. Processes in batches of 100.
     *
     * @return the number of articles that had their sentiment updated
     */
    @Transactional
    public int backfillSentiment() {
        log.info("Starting sentiment backfill for all articles");
        final long startTime = System.currentTimeMillis();

        final int batchSize = 100;
        int updatedCount = 0;
        int pageNumber = 0;

        Page<NewsArticle> page;
        do {
            page = newsArticleRepository.findAll(PageRequest.of(pageNumber, batchSize));
            for (final NewsArticle article : page.getContent()) {
                final KeywordSentimentAnalyzer.SentimentResult result =
                        sentimentAnalyzer.analyze(article.getTitle(), article.getSummary());

                if (article.getSentiment() != result.sentiment()
                        || !nearlyEqual(article.getSentimentScore(), result.score())) {
                    article.setSentiment(result.sentiment());
                    article.setSentimentScore(result.score());
                    newsArticleRepository.save(article);
                    updatedCount++;
                }
            }
            pageNumber++;
        } while (page.hasNext());

        final long duration = System.currentTimeMillis() - startTime;
        log.info("Sentiment backfill complete: totalArticles={}, updated={}, duration={}ms",
                page.getTotalElements(), updatedCount, duration);

        return updatedCount;
    }

    /**
     * Compares two doubles for near-equality (within epsilon of 0.0001).
     *
     * @param a first value (nullable)
     * @param b second value
     * @return true if values are within epsilon
     */
    private boolean nearlyEqual(Double a, double b) {
        if (a == null) return false;
        return Math.abs(a - b) < 0.0001;
    }
}
