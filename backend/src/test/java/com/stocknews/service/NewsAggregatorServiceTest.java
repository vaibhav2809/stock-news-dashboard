package com.stocknews.service;

import com.stocknews.dto.NewsArticleResponse;
import com.stocknews.dto.NewsSearchRequest;
import com.stocknews.dto.PaginatedResponse;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.model.entity.NewsArticle;
import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import com.stocknews.repository.NewsArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NewsAggregatorService.
 * Tests orchestration logic, deduplication, and query delegation.
 */
@ExtendWith(MockitoExtension.class)
class NewsAggregatorServiceTest {

    @Mock
    private FinnhubNewsService finnhubNewsService;

    @Mock
    private NewsDataIoService newsDataIoService;

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @Mock
    private KeywordSentimentAnalyzer sentimentAnalyzer;

    @InjectMocks
    private NewsAggregatorService newsAggregatorService;

    @Test
    @DisplayName("Should fetch from both sources and save non-duplicate articles")
    void shouldFetchAndSaveNonDuplicateArticles() {
        NewsArticle finnhubArticle = createTestArticle("fp-1", "Finnhub Article", NewsSource.FINNHUB);
        NewsArticle newsdataArticle = createTestArticle("fp-2", "NewsData Article", NewsSource.NEWSDATA_IO);

        when(finnhubNewsService.fetchNews(eq("AAPL"), any(), any())).thenReturn(List.of(finnhubArticle));
        when(newsDataIoService.fetchNews("AAPL")).thenReturn(List.of(newsdataArticle));
        when(newsArticleRepository.existsByFingerprint(anyString())).thenReturn(false);
        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(i -> i.getArgument(0));
        when(sentimentAnalyzer.analyze(anyString(), anyString()))
                .thenReturn(new KeywordSentimentAnalyzer.SentimentResult(Sentiment.NEUTRAL, 0.0));

        newsAggregatorService.fetchAndStoreNews("AAPL");

        verify(newsArticleRepository, times(2)).save(any(NewsArticle.class));
        verify(sentimentAnalyzer, times(2)).analyze(anyString(), anyString());
    }

    @Test
    @DisplayName("Should skip duplicate articles based on fingerprint")
    void shouldSkipDuplicateArticles() {
        NewsArticle article = createTestArticle("duplicate-fp", "Duplicate Article", NewsSource.FINNHUB);

        when(finnhubNewsService.fetchNews(eq("AAPL"), any(), any())).thenReturn(List.of(article));
        when(newsDataIoService.fetchNews("AAPL")).thenReturn(List.of());
        when(newsArticleRepository.existsByFingerprint("duplicate-fp")).thenReturn(true);

        newsAggregatorService.fetchAndStoreNews("AAPL");

        verify(newsArticleRepository, never()).save(any(NewsArticle.class));
    }

    @Test
    @DisplayName("Should continue when one source fails")
    void shouldContinueWhenOneSourceFails() {
        NewsArticle article = createTestArticle("fp-1", "Working Article", NewsSource.NEWSDATA_IO);

        when(finnhubNewsService.fetchNews(eq("AAPL"), any(), any()))
                .thenThrow(new RuntimeException("Finnhub is down"));
        when(newsDataIoService.fetchNews("AAPL")).thenReturn(List.of(article));
        when(newsArticleRepository.existsByFingerprint(anyString())).thenReturn(false);
        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(i -> i.getArgument(0));
        when(sentimentAnalyzer.analyze(anyString(), anyString()))
                .thenReturn(new KeywordSentimentAnalyzer.SentimentResult(Sentiment.NEUTRAL, 0.0));

        assertDoesNotThrow(() -> newsAggregatorService.fetchAndStoreNews("AAPL"));
        verify(newsArticleRepository, times(1)).save(any(NewsArticle.class));
    }

    @Test
    @DisplayName("Should search articles with filters and return paginated response")
    void shouldSearchArticlesWithFilters() {
        NewsArticle article = createTestArticle("fp-1", "Test Article", NewsSource.FINNHUB);
        article.setId(1L);
        Page<NewsArticle> page = new PageImpl<>(List.of(article), PageRequest.of(0, 20), 1);

        when(newsArticleRepository.searchArticles(any(), anyBoolean(), any(), any(), any(), any(), any())).thenReturn(page);

        NewsSearchRequest request = NewsSearchRequest.builder()
                .symbols(List.of("AAPL"))
                .source(NewsSource.FINNHUB)
                .page(0)
                .size(20)
                .build();

        PaginatedResponse<NewsArticleResponse> response = newsAggregatorService.searchNews(request);

        assertEquals(1, response.getData().size());
        assertEquals("Test Article", response.getData().get(0).getTitle());
        assertEquals(0, response.getPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should return empty paginated response when no articles match")
    void shouldReturnEmptyPaginatedResponse() {
        Page<NewsArticle> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(newsArticleRepository.searchArticles(any(), anyBoolean(), any(), any(), any(), any(), any())).thenReturn(emptyPage);

        NewsSearchRequest request = NewsSearchRequest.builder().page(0).size(20).build();

        PaginatedResponse<NewsArticleResponse> response = newsAggregatorService.searchNews(request);

        assertTrue(response.getData().isEmpty());
        assertEquals(0, response.getTotalElements());
    }

    @Test
    @DisplayName("Should return article by ID when it exists")
    void shouldReturnArticleById() {
        NewsArticle article = createTestArticle("fp-1", "Found Article", NewsSource.FINNHUB);
        article.setId(42L);

        when(newsArticleRepository.findById(42L)).thenReturn(Optional.of(article));

        NewsArticleResponse response = newsAggregatorService.getArticleById(42L);

        assertEquals("Found Article", response.getTitle());
        assertEquals(42L, response.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when article ID does not exist")
    void shouldThrowNotFoundWhenArticleIdMissing() {
        when(newsArticleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> newsAggregatorService.getArticleById(999L));
    }

    @Test
    @DisplayName("Should return trending articles from last 24 hours")
    void shouldReturnTrendingArticles() {
        NewsArticle article = createTestArticle("fp-1", "Trending Article", NewsSource.FINNHUB);
        article.setId(1L);
        Page<NewsArticle> page = new PageImpl<>(List.of(article), PageRequest.of(0, 20), 1);

        when(newsArticleRepository.findByPublishedAtAfterOrderByPublishedAtDesc(any(), any())).thenReturn(page);

        PaginatedResponse<NewsArticleResponse> response = newsAggregatorService.getTrendingNews(0, 20);

        assertEquals(1, response.getData().size());
        assertEquals("Trending Article", response.getData().get(0).getTitle());
    }

    @Test
    @DisplayName("Should handle date range filters correctly")
    void shouldHandleDateRangeFilters() {
        Page<NewsArticle> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(newsArticleRepository.searchArticles(any(), anyBoolean(), any(), any(), any(), any(), any())).thenReturn(emptyPage);

        NewsSearchRequest request = NewsSearchRequest.builder()
                .fromDate(LocalDate.of(2026, 3, 1))
                .toDate(LocalDate.of(2026, 3, 16))
                .page(0)
                .size(20)
                .build();

        PaginatedResponse<NewsArticleResponse> response = newsAggregatorService.searchNews(request);

        assertNotNull(response);
        verify(newsArticleRepository).searchArticles(any(), eq(false), isNull(), isNull(), any(), any(), any());
    }

    @Test
    @DisplayName("Should fetch news for multiple symbols without stopping on single failure")
    void shouldFetchForMultipleSymbolsWithoutStoppingOnFailure() {
        when(finnhubNewsService.fetchNews(eq("AAPL"), any(), any())).thenThrow(new RuntimeException("Fail"));
        when(newsDataIoService.fetchNews("AAPL")).thenReturn(List.of());
        when(finnhubNewsService.fetchNews(eq("TSLA"), any(), any())).thenReturn(List.of());
        when(newsDataIoService.fetchNews("TSLA")).thenReturn(List.of());

        assertDoesNotThrow(() -> newsAggregatorService.fetchNewsForSymbols(List.of("AAPL", "TSLA")));
    }

    /**
     * Helper: creates a test NewsArticle entity.
     */
    private NewsArticle createTestArticle(String fingerprint, String title, NewsSource source) {
        return NewsArticle.builder()
                .fingerprint(fingerprint)
                .title(title)
                .summary("Test summary for " + title)
                .sourceUrl("https://example.com/" + fingerprint)
                .source(source)
                .symbol("AAPL")
                .sentiment(Sentiment.NEUTRAL)
                .sentimentScore(0.0)
                .publishedAt(OffsetDateTime.now())
                .fetchedAt(OffsetDateTime.now())
                .build();
    }
}
