package com.stocknews.service;

import com.stocknews.model.entity.NewsArticle;
import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import com.stocknews.util.ArticleFingerprintGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Fetches news articles from the Finnhub API.
 * Handles API response mapping, error handling, and rate limit awareness.
 *
 * @see <a href="https://finnhub.io/docs/api/company-news">Finnhub Company News API</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinnhubNewsService {

    private final RestTemplate restTemplate;

    @Value("${app.finnhub.api-key:}")
    private String apiKey;

    @Value("${app.finnhub.base-url:https://finnhub.io/api/v1}")
    private String baseUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Fetches company news from Finnhub for a given stock symbol and date range.
     * @param symbol the stock ticker (e.g., "AAPL")
     * @param fromDate start date for the news search
     * @param toDate end date for the news search
     * @return list of mapped NewsArticle entities (not yet persisted)
     */
    public List<NewsArticle> fetchNews(String symbol, LocalDate fromDate, LocalDate toDate) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Finnhub API key is not configured, skipping Finnhub fetch for symbol={}", symbol);
            return Collections.emptyList();
        }

        final String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/company-news")
                .queryParam("symbol", symbol)
                .queryParam("from", fromDate.format(DATE_FORMATTER))
                .queryParam("to", toDate.format(DATE_FORMATTER))
                .queryParam("token", apiKey)
                .toUriString();

        log.debug("Fetching Finnhub news for symbol={}, from={}, to={}", symbol, fromDate, toDate);
        final long startTime = System.currentTimeMillis();

        try {
            @SuppressWarnings("unchecked")
            // Safe cast: Finnhub API always returns an array of objects
            final List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);

            final long duration = System.currentTimeMillis() - startTime;

            if (response == null || response.isEmpty()) {
                log.info("Finnhub returned 0 articles for symbol={} in {}ms", symbol, duration);
                return Collections.emptyList();
            }

            final List<NewsArticle> articles = response.stream()
                    .map(item -> mapToNewsArticle(item, symbol))
                    .filter(article -> article != null)
                    .toList();

            log.info("Fetched {} articles from Finnhub for symbol={} in {}ms", articles.size(), symbol, duration);
            return articles;

        } catch (RestClientException e) {
            final long duration = System.currentTimeMillis() - startTime;
            log.error("Finnhub API call failed for symbol={} after {}ms: {}", symbol, duration, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Maps a raw Finnhub API response item to a NewsArticle entity.
     * @param item the raw JSON map from Finnhub
     * @param symbol the stock symbol this article relates to
     * @return mapped NewsArticle or null if essential data is missing
     */
    private NewsArticle mapToNewsArticle(Map<String, Object> item, String symbol) {
        final String title = getStringValue(item, "headline");
        final String sourceUrl = getStringValue(item, "url");

        if (title == null || title.isBlank() || sourceUrl == null || sourceUrl.isBlank()) {
            log.debug("Skipping Finnhub article with missing title or URL for symbol={}", symbol);
            return null;
        }

        final String fingerprint = ArticleFingerprintGenerator.generate(title, sourceUrl);
        final Object datetimeValue = item.get("datetime");
        final OffsetDateTime publishedAt = parseUnixTimestamp(datetimeValue);

        return NewsArticle.builder()
                .externalId(String.valueOf(item.get("id")))
                .fingerprint(fingerprint)
                .title(title)
                .summary(getStringValue(item, "summary"))
                .sourceUrl(sourceUrl)
                .imageUrl(getStringValue(item, "image"))
                .source(NewsSource.FINNHUB)
                .symbol(symbol)
                .sentiment(Sentiment.NEUTRAL)
                .sentimentScore(0.0)
                .publishedAt(publishedAt != null ? publishedAt : OffsetDateTime.now())
                .fetchedAt(OffsetDateTime.now())
                .build();
    }

    /**
     * Safely extracts a String value from a map.
     * @param map the source map
     * @param key the key to look up
     * @return the string value, or null if missing or not a string
     */
    private String getStringValue(Map<String, Object> map, String key) {
        final Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Parses a Unix timestamp (seconds since epoch) to OffsetDateTime.
     * @param value the timestamp value (could be Integer or Long)
     * @return the parsed datetime, or null if unparseable
     */
    private OffsetDateTime parseUnixTimestamp(Object value) {
        if (value == null) {
            return null;
        }
        try {
            final long epochSeconds = ((Number) value).longValue();
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
        } catch (Exception e) {
            log.debug("Failed to parse Finnhub timestamp: {}", value);
            return null;
        }
    }
}
