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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NewsDataIoService.
 * Uses mocked RestTemplate to avoid real API calls.
 */
@ExtendWith(MockitoExtension.class)
class NewsDataIoServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NewsDataIoService newsDataIoService;

    private static final String VALID_API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(newsDataIoService, "apiKey", VALID_API_KEY);
        ReflectionTestUtils.setField(newsDataIoService, "baseUrl", "https://newsdata.io/api/1");
    }

    @Test
    @DisplayName("Should return mapped articles from NewsData.io API response")
    void shouldReturnMappedArticles() {
        Map<String, Object> article1 = new HashMap<>();
        article1.put("article_id", "nd-001");
        article1.put("title", "Tesla deliveries exceed expectations");
        article1.put("link", "https://example.com/tesla-deliveries");
        article1.put("description", "Tesla reported record deliveries...");
        article1.put("image_url", "https://example.com/tesla.jpg");
        article1.put("pubDate", "2026-03-15 14:30:00");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("results", List.of(article1));

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        List<NewsArticle> articles = newsDataIoService.fetchNews("TSLA");

        assertEquals(1, articles.size());
        assertEquals("Tesla deliveries exceed expectations", articles.get(0).getTitle());
        assertEquals(NewsSource.NEWSDATA_IO, articles.get(0).getSource());
        assertEquals("TSLA", articles.get(0).getSymbol());
        assertNotNull(articles.get(0).getFingerprint());
    }

    @Test
    @DisplayName("Should return empty list when API key is not configured")
    void shouldReturnEmptyListWhenApiKeyMissing() {
        ReflectionTestUtils.setField(newsDataIoService, "apiKey", "");

        List<NewsArticle> articles = newsDataIoService.fetchNews("TSLA");

        assertTrue(articles.isEmpty());
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should return empty list when API returns null")
    void shouldReturnEmptyListWhenApiReturnsNull() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        List<NewsArticle> articles = newsDataIoService.fetchNews("TSLA");

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when results key is missing")
    void shouldReturnEmptyListWhenResultsKeyMissing() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "ok");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        List<NewsArticle> articles = newsDataIoService.fetchNews("TSLA");

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should handle API failure gracefully and return empty list")
    void shouldHandleApiFailureGracefully() {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        List<NewsArticle> articles = newsDataIoService.fetchNews("TSLA");

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should skip articles with missing title")
    void shouldSkipArticlesWithMissingTitle() {
        Map<String, Object> article = new HashMap<>();
        article.put("link", "https://example.com/no-title");
        article.put("pubDate", "2026-03-15 14:30:00");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("results", List.of(article));

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        List<NewsArticle> articles = newsDataIoService.fetchNews("TSLA");

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should skip articles with missing link")
    void shouldSkipArticlesWithMissingLink() {
        Map<String, Object> article = new HashMap<>();
        article.put("title", "Test article");
        article.put("pubDate", "2026-03-15 14:30:00");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("results", List.of(article));

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        List<NewsArticle> articles = newsDataIoService.fetchNews("TSLA");

        assertTrue(articles.isEmpty());
    }

    @Test
    @DisplayName("Should handle unparseable date gracefully")
    void shouldHandleUnparseableDateGracefully() {
        Map<String, Object> article = new HashMap<>();
        article.put("title", "Test article");
        article.put("link", "https://example.com/test");
        article.put("pubDate", "not-a-valid-date");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("results", List.of(article));

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        List<NewsArticle> articles = newsDataIoService.fetchNews("TSLA");

        assertEquals(1, articles.size());
        assertNotNull(articles.get(0).getPublishedAt());
    }
}
