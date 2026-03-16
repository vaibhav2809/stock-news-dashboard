package com.stocknews.controller;

import com.stocknews.dto.NewsArticleResponse;
import com.stocknews.dto.PaginatedResponse;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import com.stocknews.service.NewsAggregatorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for NewsController.
 * Uses MockMvc to test HTTP layer without starting full Spring context.
 */
@WebMvcTest(NewsController.class)
@AutoConfigureMockMvc(addFilters = false)
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsAggregatorService newsAggregatorService;

    @Test
    @DisplayName("GET /api/v1/news — should return paginated news articles")
    void shouldReturnPaginatedNewsArticles() throws Exception {
        NewsArticleResponse article = createTestResponse(1L, "Apple stock surges");
        PaginatedResponse<NewsArticleResponse> response = PaginatedResponse.of(
                List.of(article), 0, 1, 1
        );

        when(newsAggregatorService.searchNews(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/news")
                        .param("symbols", "AAPL")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Apple stock surges"))
                .andExpect(jsonPath("$.data[0].source").value("FINNHUB"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/news — should return empty list when no articles match")
    void shouldReturnEmptyListWhenNoArticlesMatch() throws Exception {
        PaginatedResponse<NewsArticleResponse> emptyResponse = PaginatedResponse.of(
                List.of(), 0, 0, 0
        );

        when(newsAggregatorService.searchNews(any())).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/news — should pass all filter parameters correctly")
    void shouldPassAllFilterParameters() throws Exception {
        PaginatedResponse<NewsArticleResponse> response = PaginatedResponse.of(
                List.of(), 0, 0, 0
        );

        when(newsAggregatorService.searchNews(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/news")
                        .param("symbols", "AAPL", "TSLA")
                        .param("source", "FINNHUB")
                        .param("sentiment", "POSITIVE")
                        .param("fromDate", "2026-03-01")
                        .param("toDate", "2026-03-16")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(newsAggregatorService).searchNews(any());
    }

    @Test
    @DisplayName("GET /api/v1/news/{id} — should return article when found")
    void shouldReturnArticleWhenFound() throws Exception {
        NewsArticleResponse article = createTestResponse(42L, "Found article");

        when(newsAggregatorService.getArticleById(42L)).thenReturn(article);

        mockMvc.perform(get("/api/v1/news/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").value("Found article"));
    }

    @Test
    @DisplayName("GET /api/v1/news/{id} — should return 404 when not found")
    void shouldReturn404WhenArticleNotFound() throws Exception {
        when(newsAggregatorService.getArticleById(999L))
                .thenThrow(new ResourceNotFoundException("News article not found with id=999"));

        mockMvc.perform(get("/api/v1/news/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/news/trending — should return trending articles")
    void shouldReturnTrendingArticles() throws Exception {
        NewsArticleResponse article = createTestResponse(1L, "Trending news");
        PaginatedResponse<NewsArticleResponse> response = PaginatedResponse.of(
                List.of(article), 0, 1, 1
        );

        when(newsAggregatorService.getTrendingNews(0, 20)).thenReturn(response);

        mockMvc.perform(get("/api/v1/news/trending")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Trending news"));
    }

    @Test
    @DisplayName("POST /api/v1/news/fetch — should trigger news fetch for symbols")
    void shouldTriggerNewsFetch() throws Exception {
        doNothing().when(newsAggregatorService).fetchNewsForSymbols(anyList());

        mockMvc.perform(post("/api/v1/news/fetch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"AAPL\", \"TSLA\"]"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AAPL")));

        verify(newsAggregatorService).fetchNewsForSymbols(List.of("AAPL", "TSLA"));
    }

    /**
     * Helper: creates a test NewsArticleResponse.
     */
    private NewsArticleResponse createTestResponse(Long id, String title) {
        return NewsArticleResponse.builder()
                .id(id)
                .title(title)
                .summary("Test summary")
                .sourceUrl("https://example.com/test")
                .source(NewsSource.FINNHUB)
                .symbol("AAPL")
                .sentiment(Sentiment.NEUTRAL)
                .sentimentScore(0.0)
                .publishedAt(OffsetDateTime.now())
                .build();
    }
}
