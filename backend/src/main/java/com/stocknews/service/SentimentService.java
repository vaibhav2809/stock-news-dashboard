package com.stocknews.service;

import com.stocknews.dto.SentimentDistribution;
import com.stocknews.dto.SentimentTimelineEntry;
import com.stocknews.model.entity.NewsArticle;
import com.stocknews.model.enums.Sentiment;
import com.stocknews.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides sentiment analysis aggregation for stock symbols.
 * Computes distributions (positive/negative/neutral percentages) and
 * timeline data for charting sentiment trends over time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentService {

    private final NewsArticleRepository newsArticleRepository;

    private static final int MAX_TIMELINE_ARTICLES = 5000;

    /**
     * Returns the sentiment distribution for a specific stock symbol.
     * Counts articles by sentiment classification and computes percentages.
     * Cached for 30 minutes.
     *
     * @param symbol the stock ticker (e.g., "AAPL")
     * @return the sentiment distribution with counts and percentages
     */
    @Cacheable(value = "sentiment", key = "'dist-' + #symbol")
    @Transactional(readOnly = true)
    public SentimentDistribution getDistribution(String symbol) {
        log.info("Computing sentiment distribution for symbol={}", symbol);
        final long startTime = System.currentTimeMillis();

        final long positiveCount = newsArticleRepository.countBySymbolAndSentiment(symbol, Sentiment.POSITIVE);
        final long negativeCount = newsArticleRepository.countBySymbolAndSentiment(symbol, Sentiment.NEGATIVE);
        final long neutralCount = newsArticleRepository.countBySymbolAndSentiment(symbol, Sentiment.NEUTRAL);

        // Compute average score from recent articles
        final double averageScore = computeAverageScore(symbol);

        final long duration = System.currentTimeMillis() - startTime;
        log.info("Sentiment distribution for symbol={}: positive={}, negative={}, neutral={}, avgScore={}, duration={}ms",
                symbol, positiveCount, negativeCount, neutralCount, averageScore, duration);

        return SentimentDistribution.of(symbol, positiveCount, negativeCount, neutralCount, averageScore);
    }

    /**
     * Returns sentiment distributions for multiple symbols at once.
     * Useful for the dashboard overview page.
     *
     * @param symbols list of stock tickers
     * @return list of distributions, one per symbol
     */
    @Transactional(readOnly = true)
    public List<SentimentDistribution> getDistributions(List<String> symbols) {
        log.info("Computing sentiment distributions for {} symbols", symbols.size());
        return symbols.stream()
                .map(this::getDistribution)
                .toList();
    }

    /**
     * Returns a day-by-day sentiment timeline for a stock symbol over the given number of days.
     * Each entry contains article counts by sentiment and the average score for that day.
     * Cached for 30 minutes.
     *
     * @param symbol the stock ticker
     * @param days number of days to look back (default 30)
     * @return list of daily sentiment data points, sorted by date ascending
     */
    @Cacheable(value = "sentiment", key = "'timeline-' + #symbol + '-' + #days")
    @Transactional(readOnly = true)
    public List<SentimentTimelineEntry> getTimeline(String symbol, int days) {
        log.info("Computing sentiment timeline for symbol={}, days={}", symbol, days);
        final long startTime = System.currentTimeMillis();

        final OffsetDateTime toDate = OffsetDateTime.now();
        final OffsetDateTime fromDate = toDate.minusDays(days);

        // Fetch all articles in the date range (up to MAX_TIMELINE_ARTICLES)
        final Page<NewsArticle> articlePage = newsArticleRepository
                .findBySymbolAndPublishedAtBetweenOrderByPublishedAtDesc(
                        symbol, fromDate, toDate, PageRequest.of(0, MAX_TIMELINE_ARTICLES));

        // Group articles by date
        final Map<LocalDate, List<NewsArticle>> articlesByDate = articlePage.getContent().stream()
                .collect(Collectors.groupingBy(
                        article -> article.getPublishedAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        // Build timeline entries for each day
        final List<SentimentTimelineEntry> timeline = new ArrayList<>();
        LocalDate currentDate = fromDate.toLocalDate();
        final LocalDate endDate = toDate.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            final List<NewsArticle> dayArticles = articlesByDate.getOrDefault(currentDate, List.of());

            long positiveCount = 0;
            long negativeCount = 0;
            long neutralCount = 0;
            double scoreSum = 0.0;

            for (final NewsArticle article : dayArticles) {
                switch (article.getSentiment()) {
                    case POSITIVE -> positiveCount++;
                    case NEGATIVE -> negativeCount++;
                    case NEUTRAL -> neutralCount++;
                }
                scoreSum += article.getSentimentScore() != null ? article.getSentimentScore() : 0.0;
            }

            final double avgScore = dayArticles.isEmpty() ? 0.0 : scoreSum / dayArticles.size();

            timeline.add(SentimentTimelineEntry.builder()
                    .date(currentDate)
                    .articleCount(dayArticles.size())
                    .positiveCount(positiveCount)
                    .negativeCount(negativeCount)
                    .neutralCount(neutralCount)
                    .averageScore(Math.round(avgScore * 1000.0) / 1000.0)
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        final long duration = System.currentTimeMillis() - startTime;
        log.info("Sentiment timeline for symbol={}: {} days, {} articles, duration={}ms",
                symbol, timeline.size(), articlePage.getTotalElements(), duration);

        return timeline;
    }

    /**
     * Computes the average sentiment score for a symbol from recent articles.
     * Uses up to 500 recent articles to keep the computation fast.
     *
     * @param symbol the stock ticker
     * @return the average sentiment score
     */
    private double computeAverageScore(String symbol) {
        final Page<NewsArticle> recentArticles = newsArticleRepository
                .findBySymbolOrderByPublishedAtDesc(symbol, PageRequest.of(0, 500));

        if (recentArticles.isEmpty()) {
            return 0.0;
        }

        final double totalScore = recentArticles.getContent().stream()
                .mapToDouble(article -> article.getSentimentScore() != null ? article.getSentimentScore() : 0.0)
                .sum();

        return totalScore / recentArticles.getTotalElements();
    }
}
