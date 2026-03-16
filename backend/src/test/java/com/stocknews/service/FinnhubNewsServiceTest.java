package com.stocknews.service;

import com.stocknews.model.entity.NewsArticle;
import com.stocknews.model.enums.NewsSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FinnhubNewsService.
 * Uses mocked RestTemplate to avoid real API calls.
 */
@ExtendWith(MockitoExtension.class)
class FinnhubNewsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FinnhubNewsService finnhubNewsService;

    private static final String VALID_API_KEY = "test-api-key";
    private static final LocalDate FROM_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2026, 3, 16);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(finnhubNewsService, "apiKey", VALID_API_KEY);
        ReflectionTestUtils.setField(finnhubNewsService, "baseUrl", "https://finnhub.io/api/v1");
    }

    @Test
    @DisplayName("Should return mapped articles from Finnhub API response")
    void shouldReturnMappedArticles() {
        List<Map<String, Object>> mockResponse = List.of(
                Map.of(
                        "id", 12345,
                        "headline", "Apple announces new iPhone",
                        "url", "https://example.com/apple-iphone",
                        "summary", "Apple Inc. today announced...",
                        "image", "https://example.com/image.jpg",
                        "datetime", 1710000000L
                ),
                Map.of(
                        "id", 12346,
                        "headline", "Apple Q4 earnings beat expectations",
                        "url", "https://example.com/apple-earnings",
                        "summary", "Apple reported strong Q4...",
                        "datetime", 1710100000L
                )
        );

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(mockResponse);

        List<NewsArticle> articles = finnhubNewsService.fetchNews("AAPL", FROM_DATE, TO_DATE);

        assertEquals(2, articles.size());
        assertEquals("Apple announces new iPhone", articles.get(0).getTitle());
        assertEquals(NewsSource.FINNHUB, articles.get(0).getSource());
        assertEquals("AAPL", articles.get(0).getSymbol());
        assertNotNull(articles.get(0).getFingerprint());
    }

    @Test
    @DisplayName("Should return empty list when API key is not configured")
    void shouldReturnEmptyListWhenApiKeyMissing() {
        ReflectionTestUtils.setField(finnhubNewsService, "apiKey", "");

        List<NewsArticle> articles = finnhubNewsService.fetchNews("AAPL", FROM_DATE, TO_DATE);

        assertTrue(articles.isEmpty());
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should return empty list when API returns null")
    void shouldReturnEmptyListWhenApiReturnsNull() {
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(null);

        List<NewsArticle> articles = finnhubNewsService.fetchNews("AAPL", FROM_DATE, TO_DATE);

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when API returns empty array")
    void shouldReturnEmptyListWhenApiReturnsEmpty() {
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(List.of());

        List<NewsArticle> articles = finnhubNewsService.fetchNews("AAPL", FROM_DATE, TO_DATE);

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should handle API failure gracefully and return empty list")
    void shouldHandleApiFailureGracefully() {
        when(restTemplate.getForObject(anyString(), eq(List.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        List<NewsArticle> articles = finnhubNewsService.fetchNews("AAPL", FROM_DATE, TO_DATE);

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should skip articles with missing title")
    void shouldSkipArticlesWithMissingTitle() {
        List<Map<String, Object>> mockResponse = List.of(
                Map.of(
                        "id", 12345,
                        "url", "https://example.com/no-title",
                        "datetime", 1710000000L
                )
        );

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(mockResponse);

        List<NewsArticle> articles = finnhubNewsService.fetchNews("AAPL", FROM_DATE, TO_DATE);

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should skip articles with missing URL")
    void shouldSkipArticlesWithMissingUrl() {
        List<Map<String, Object>> mockResponse = List.of(
                Map.of(
                        "id", 12345,
                        "headline", "Test headline",
                        "datetime", 1710000000L
                )
        );

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(mockResponse);

        List<NewsArticle> articles = finnhubNewsService.fetchNews("AAPL", FROM_DATE, TO_DATE);

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should generate unique fingerprints for different articles")
    void shouldGenerateUniqueFingerprintsForDifferentArticles() {
        List<Map<String, Object>> mockResponse = List.of(
                Map.of("headline", "Article One", "url", "https://example.com/1", "datetime", 1710000000L),
                Map.of("headline", "Article Two", "url", "https://example.com/2", "datetime", 1710100000L)
        );

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(mockResponse);

        List<NewsArticle> articles = finnhubNewsService.fetchNews("AAPL", FROM_DATE, TO_DATE);

        assertEquals(2, articles.size());
        assertNotEquals(articles.get(0).getFingerprint(), articles.get(1).getFingerprint());
    }
}
