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

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Fetches news articles from the NewsData.io API.
 * Handles API response mapping, error handling, and graceful degradation.
 *
 * @see <a href="https://newsdata.io/documentation">NewsData.io API Docs</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsDataIoService {

    private final RestTemplate restTemplate;

    @Value("${app.newsdata.api-key:}")
    private String apiKey;

    @Value("${app.newsdata.base-url:https://newsdata.io/api/1}")
    private String baseUrl;

    private static final DateTimeFormatter NEWSDATA_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Fetches news from NewsData.io for a given stock symbol query.
     * @param symbol the stock ticker or company name to search for
     * @return list of mapped NewsArticle entities (not yet persisted)
     */
    @SuppressWarnings("unchecked")
    public List<NewsArticle> fetchNews(String symbol) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("NewsData.io API key is not configured, skipping fetch for symbol={}", symbol);
            return Collections.emptyList();
        }

        final String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/latest")
                .queryParam("apikey", apiKey)
                .queryParam("q", symbol)
                .queryParam("language", "en")
                .queryParam("category", "business")
                .toUriString();

        log.debug("Fetching NewsData.io news for symbol={}", symbol);
        final long startTime = System.currentTimeMillis();

        try {
            // Safe cast: NewsData.io API returns a JSON object with "results" array
            final Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            final long duration = System.currentTimeMillis() - startTime;

            if (response == null || !response.containsKey("results")) {
                log.info("NewsData.io returned no results for symbol={} in {}ms", symbol, duration);
                return Collections.emptyList();
            }

            final List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

            if (results == null || results.isEmpty()) {
                log.info("NewsData.io returned 0 articles for symbol={} in {}ms", symbol, duration);
                return Collections.emptyList();
            }

            final List<NewsArticle> articles = results.stream()
                    .map(item -> mapToNewsArticle(item, symbol))
                    .filter(article -> article != null)
                    .toList();

            log.info("Fetched {} articles from NewsData.io for symbol={} in {}ms", articles.size(), symbol, duration);
            return articles;

        } catch (RestClientException e) {
            final long duration = System.currentTimeMillis() - startTime;
            log.error("NewsData.io API call failed for symbol={} after {}ms: {}", symbol, duration, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Maps a raw NewsData.io API response item to a NewsArticle entity.
     * @param item the raw JSON map from NewsData.io
     * @param symbol the stock symbol this article relates to
     * @return mapped NewsArticle or null if essential data is missing
     */
    private NewsArticle mapToNewsArticle(Map<String, Object> item, String symbol) {
        final String title = getStringValue(item, "title");
        final String sourceUrl = getStringValue(item, "link");

        if (title == null || title.isBlank() || sourceUrl == null || sourceUrl.isBlank()) {
            log.debug("Skipping NewsData.io article with missing title or URL for symbol={}", symbol);
            return null;
        }

        final String fingerprint = ArticleFingerprintGenerator.generate(title, sourceUrl);
        final OffsetDateTime publishedAt = parsePublishedDate(getStringValue(item, "pubDate"));

        return NewsArticle.builder()
                .externalId(getStringValue(item, "article_id"))
                .fingerprint(fingerprint)
                .title(title)
                .summary(getStringValue(item, "description"))
                .sourceUrl(sourceUrl)
                .imageUrl(getStringValue(item, "image_url"))
                .source(NewsSource.NEWSDATA_IO)
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
     * Parses NewsData.io's date format ("yyyy-MM-dd HH:mm:ss") to OffsetDateTime.
     * @param dateString the date string from the API
     * @return the parsed datetime in UTC, or null if unparseable
     */
    private OffsetDateTime parsePublishedDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return java.time.LocalDateTime.parse(dateString, NEWSDATA_DATE_FORMAT)
                    .atOffset(java.time.ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse NewsData.io date: {}", dateString);
            return null;
        }
    }
}
