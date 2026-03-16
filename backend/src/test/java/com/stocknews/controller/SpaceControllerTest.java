package com.stocknews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stocknews.dto.SaveArticleRequest;
import com.stocknews.dto.SavedArticleResponse;
import com.stocknews.dto.SpaceRequest;
import com.stocknews.dto.SpaceResponse;
import com.stocknews.exception.DuplicateResourceException;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for SpaceController.
 * Uses MockMvc to test the HTTP layer (routing, status codes, request/response serialization)
 * without starting the full Spring context. Security filters are disabled since
 * authentication is not yet implemented (Phase 5).
 */
@WebMvcTest(SpaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpaceService spaceService;

    private ObjectMapper objectMapper;

    private static final Long DEFAULT_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/v1/spaces — should create space and return 201")
    void shouldCreateSpaceAndReturn201() throws Exception {
        SpaceRequest request = SpaceRequest.builder()
                .name("Tech News")
                .description("Technology articles")
                .build();

        SpaceResponse response = buildSpaceResponse(1L, "Tech News", "Technology articles");

        when(spaceService.createSpace(eq(DEFAULT_USER_ID), any(SpaceRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tech News"))
                .andExpect(jsonPath("$.description").value("Technology articles"));

        verify(spaceService).createSpace(eq(DEFAULT_USER_ID), any(SpaceRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/spaces — should return 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        SpaceRequest request = SpaceRequest.builder()
                .name("")
                .description("Some description")
                .build();

        mockMvc.perform(post("/api/v1/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verifyNoInteractions(spaceService);
    }

    @Test
    @DisplayName("POST /api/v1/spaces — should return 409 when space name is duplicate")
    void shouldReturn409WhenDuplicateName() throws Exception {
        SpaceRequest request = SpaceRequest.builder()
                .name("Duplicate")
                .description("Duplicate space")
                .build();

        when(spaceService.createSpace(eq(DEFAULT_USER_ID), any(SpaceRequest.class)))
                .thenThrow(new DuplicateResourceException("A space named 'Duplicate' already exists"));

        mockMvc.perform(post("/api/v1/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A space named 'Duplicate' already exists"));
    }

    @Test
    @DisplayName("GET /api/v1/spaces — should return list of spaces")
    void shouldReturnListOfSpaces() throws Exception {
        SpaceResponse spaceOne = buildSpaceResponse(1L, "Tech News", "Tech articles");
        SpaceResponse spaceTwo = buildSpaceResponse(2L, "Finance", "Finance articles");

        when(spaceService.getSpacesByUserId(DEFAULT_USER_ID))
                .thenReturn(List.of(spaceOne, spaceTwo));

        mockMvc.perform(get("/api/v1/spaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Tech News"))
                .andExpect(jsonPath("$[1].name").value("Finance"));
    }

    @Test
    @DisplayName("GET /api/v1/spaces/{id} — should return space when found")
    void shouldReturnSpaceWhenFound() throws Exception {
        SpaceResponse response = buildSpaceResponse(5L, "My Space", "Personal articles");

        when(spaceService.getSpaceById(5L, DEFAULT_USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/spaces/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("My Space"))
                .andExpect(jsonPath("$.description").value("Personal articles"));
    }

    @Test
    @DisplayName("GET /api/v1/spaces/{id} — should return 404 when space not found")
    void shouldReturn404WhenSpaceNotFound() throws Exception {
        when(spaceService.getSpaceById(999L, DEFAULT_USER_ID))
                .thenThrow(new ResourceNotFoundException("Space not found with id=999"));

        mockMvc.perform(get("/api/v1/spaces/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Space not found with id=999"));
    }

    @Test
    @DisplayName("PUT /api/v1/spaces/{id} — should update space and return 200")
    void shouldUpdateSpaceAndReturn200() throws Exception {
        SpaceRequest request = SpaceRequest.builder()
                .name("Updated Name")
                .description("Updated description")
                .build();

        SpaceResponse response = buildSpaceResponse(5L, "Updated Name", "Updated description");

        when(spaceService.updateSpace(eq(5L), eq(DEFAULT_USER_ID), any(SpaceRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/spaces/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @DisplayName("DELETE /api/v1/spaces/{id} — should delete space and return 204")
    void shouldDeleteSpaceAndReturn204() throws Exception {
        doNothing().when(spaceService).deleteSpace(5L, DEFAULT_USER_ID);

        mockMvc.perform(delete("/api/v1/spaces/5"))
                .andExpect(status().isNoContent());

        verify(spaceService).deleteSpace(5L, DEFAULT_USER_ID);
    }

    @Test
    @DisplayName("POST /api/v1/spaces/{id}/articles — should save article and return 201")
    void shouldSaveArticleAndReturn201() throws Exception {
        SaveArticleRequest request = buildSaveArticleRequest();

        SavedArticleResponse response = SavedArticleResponse.builder()
                .id(100L)
                .externalId("ext-123")
                .source("FINNHUB")
                .symbol("AAPL")
                .title("Apple stock surges")
                .summary("Apple Inc. stock rose 5%")
                .sourceUrl("https://example.com/apple")
                .imageUrl("https://example.com/image.jpg")
                .sentiment("POSITIVE")
                .sentimentScore(0.85)
                .publishedAt(OffsetDateTime.parse("2026-03-15T10:00:00Z"))
                .savedAt(OffsetDateTime.now())
                .build();

        when(spaceService.saveArticleToSpace(eq(5L), eq(DEFAULT_USER_ID), any(SaveArticleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/spaces/5/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Apple stock surges"))
                .andExpect(jsonPath("$.source").value("FINNHUB"))
                .andExpect(jsonPath("$.sentiment").value("POSITIVE"));

        verify(spaceService).saveArticleToSpace(eq(5L), eq(DEFAULT_USER_ID), any(SaveArticleRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/spaces/{id}/articles — should return articles in space")
    void shouldReturnArticlesInSpace() throws Exception {
        SavedArticleResponse articleOne = SavedArticleResponse.builder()
                .id(1L)
                .source("FINNHUB")
                .title("Article One")
                .sourceUrl("https://example.com/1")
                .sentimentScore(0.0)
                .savedAt(OffsetDateTime.now())
                .build();

        SavedArticleResponse articleTwo = SavedArticleResponse.builder()
                .id(2L)
                .source("NEWSDATA_IO")
                .title("Article Two")
                .sourceUrl("https://example.com/2")
                .sentimentScore(0.5)
                .savedAt(OffsetDateTime.now())
                .build();

        when(spaceService.getArticlesInSpace(5L, DEFAULT_USER_ID))
                .thenReturn(List.of(articleOne, articleTwo));

        mockMvc.perform(get("/api/v1/spaces/5/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Article One"))
                .andExpect(jsonPath("$[1].title").value("Article Two"));
    }

    @Test
    @DisplayName("DELETE /api/v1/spaces/{id}/articles/{articleId} — should remove article and return 204")
    void shouldRemoveArticleAndReturn204() throws Exception {
        doNothing().when(spaceService).removeArticleFromSpace(5L, 100L, DEFAULT_USER_ID);

        mockMvc.perform(delete("/api/v1/spaces/5/articles/100"))
                .andExpect(status().isNoContent());

        verify(spaceService).removeArticleFromSpace(5L, 100L, DEFAULT_USER_ID);
    }

    @Test
    @DisplayName("POST /api/v1/spaces/{id}/articles — should return 409 when article is duplicate")
    void shouldReturn409WhenArticleIsDuplicate() throws Exception {
        SaveArticleRequest request = buildSaveArticleRequest();

        when(spaceService.saveArticleToSpace(eq(5L), eq(DEFAULT_USER_ID), any(SaveArticleRequest.class)))
                .thenThrow(new DuplicateResourceException("This article is already saved in this space"));

        mockMvc.perform(post("/api/v1/spaces/5/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This article is already saved in this space"));
    }

    /**
     * Builds a SpaceResponse for test assertions.
     *
     * @param id          the space ID
     * @param name        the space name
     * @param description the space description
     * @return a populated SpaceResponse
     */
    private SpaceResponse buildSpaceResponse(Long id, String name, String description) {
        return SpaceResponse.builder()
                .id(id)
                .name(name)
                .description(description)
                .articleCount(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    /**
     * Builds a standard SaveArticleRequest for testing.
     *
     * @return a populated SaveArticleRequest
     */
    private SaveArticleRequest buildSaveArticleRequest() {
        return SaveArticleRequest.builder()
                .externalId("ext-123")
                .source("FINNHUB")
                .symbol("AAPL")
                .title("Apple stock surges")
                .summary("Apple Inc. stock rose 5%")
                .sourceUrl("https://example.com/apple")
                .imageUrl("https://example.com/image.jpg")
                .sentiment("POSITIVE")
                .sentimentScore(0.85)
                .publishedAt(OffsetDateTime.parse("2026-03-15T10:00:00Z"))
                .build();
    }
}
