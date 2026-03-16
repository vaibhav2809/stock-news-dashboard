package com.stocknews.service;

import com.stocknews.dto.*;
import com.stocknews.exception.DuplicateResourceException;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.model.entity.SavedArticle;
import com.stocknews.model.entity.Space;
import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import com.stocknews.repository.SavedArticleRepository;
import com.stocknews.repository.SpaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SpaceService.
 * Uses Mockito to mock repository dependencies and verify business logic
 * for CRUD operations on Spaces and SavedArticles.
 */
@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private SavedArticleRepository savedArticleRepository;

    @InjectMocks
    private SpaceService spaceService;

    private static final Long USER_ID = 1L;
    private static final Long SPACE_ID = 10L;
    private static final Long ARTICLE_ID = 100L;

    @Nested
    @DisplayName("createSpace")
    class CreateSpace {

        @Test
        @DisplayName("Should create space successfully and return response")
        void shouldCreateSpaceSuccessfully() {
            SpaceRequest request = SpaceRequest.builder()
                    .name("Tech News")
                    .description("Articles about tech stocks")
                    .build();

            Space savedSpace = buildSpace(SPACE_ID, "Tech News", "Articles about tech stocks");

            when(spaceRepository.existsByUserIdAndName(USER_ID, "Tech News")).thenReturn(false);
            when(spaceRepository.save(any(Space.class))).thenReturn(savedSpace);

            SpaceResponse response = spaceService.createSpace(USER_ID, request);

            assertNotNull(response);
            assertEquals(SPACE_ID, response.getId());
            assertEquals("Tech News", response.getName());
            assertEquals("Articles about tech stocks", response.getDescription());
            verify(spaceRepository).existsByUserIdAndName(USER_ID, "Tech News");
            verify(spaceRepository).save(any(Space.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when space name already exists for user")
        void shouldThrowDuplicateResourceExceptionWhenNameExists() {
            SpaceRequest request = SpaceRequest.builder()
                    .name("Tech News")
                    .description("Duplicate space")
                    .build();

            when(spaceRepository.existsByUserIdAndName(USER_ID, "Tech News")).thenReturn(true);

            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> spaceService.createSpace(USER_ID, request)
            );

            assertTrue(exception.getMessage().contains("Tech News"));
            verify(spaceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getSpacesByUserId")
    class GetSpacesByUserId {

        @Test
        @DisplayName("Should return list of spaces for a user")
        void shouldReturnSpacesForUser() {
            Space spaceOne = buildSpace(1L, "Tech News", "Tech articles");
            Space spaceTwo = buildSpace(2L, "Finance", "Finance articles");

            when(spaceRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(List.of(spaceOne, spaceTwo));

            List<SpaceResponse> responses = spaceService.getSpacesByUserId(USER_ID);

            assertEquals(2, responses.size());
            assertEquals("Tech News", responses.get(0).getName());
            assertEquals("Finance", responses.get(1).getName());
            verify(spaceRepository).findByUserIdOrderByCreatedAtDesc(USER_ID);
        }

        @Test
        @DisplayName("Should return empty list when user has no spaces")
        void shouldReturnEmptyListWhenNoSpaces() {
            when(spaceRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(List.of());

            List<SpaceResponse> responses = spaceService.getSpacesByUserId(USER_ID);

            assertTrue(responses.isEmpty());
        }
    }

    @Nested
    @DisplayName("getSpaceById")
    class GetSpaceById {

        @Test
        @DisplayName("Should return space when it exists and belongs to user")
        void shouldReturnSpaceWhenFound() {
            Space space = buildSpace(SPACE_ID, "Tech News", "Tech articles");

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(space));

            SpaceResponse response = spaceService.getSpaceById(SPACE_ID, USER_ID);

            assertNotNull(response);
            assertEquals(SPACE_ID, response.getId());
            assertEquals("Tech News", response.getName());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when space does not exist")
        void shouldThrowResourceNotFoundExceptionWhenNotFound() {
            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> spaceService.getSpaceById(SPACE_ID, USER_ID)
            );

            assertTrue(exception.getMessage().contains(String.valueOf(SPACE_ID)));
        }
    }

    @Nested
    @DisplayName("updateSpace")
    class UpdateSpace {

        @Test
        @DisplayName("Should update space name and description successfully")
        void shouldUpdateSpaceSuccessfully() {
            Space existingSpace = buildSpace(SPACE_ID, "Old Name", "Old description");
            Space updatedSpace = buildSpace(SPACE_ID, "New Name", "New description");

            SpaceRequest request = SpaceRequest.builder()
                    .name("New Name")
                    .description("New description")
                    .build();

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(existingSpace));
            when(spaceRepository.existsByUserIdAndName(USER_ID, "New Name")).thenReturn(false);
            when(spaceRepository.save(any(Space.class))).thenReturn(updatedSpace);

            SpaceResponse response = spaceService.updateSpace(SPACE_ID, USER_ID, request);

            assertEquals("New Name", response.getName());
            assertEquals("New description", response.getDescription());
            verify(spaceRepository).save(existingSpace);
        }

        @Test
        @DisplayName("Should allow update when name is unchanged")
        void shouldAllowUpdateWhenNameUnchanged() {
            Space existingSpace = buildSpace(SPACE_ID, "Same Name", "Old description");
            Space updatedSpace = buildSpace(SPACE_ID, "Same Name", "Updated description");

            SpaceRequest request = SpaceRequest.builder()
                    .name("Same Name")
                    .description("Updated description")
                    .build();

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(existingSpace));
            when(spaceRepository.save(any(Space.class))).thenReturn(updatedSpace);

            SpaceResponse response = spaceService.updateSpace(SPACE_ID, USER_ID, request);

            assertEquals("Same Name", response.getName());
            assertEquals("Updated description", response.getDescription());
            // Should NOT check for duplicate name when name hasn't changed
            verify(spaceRepository, never()).existsByUserIdAndName(anyLong(), anyString());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when new name conflicts")
        void shouldThrowDuplicateWhenNewNameConflicts() {
            Space existingSpace = buildSpace(SPACE_ID, "Old Name", "Description");

            SpaceRequest request = SpaceRequest.builder()
                    .name("Taken Name")
                    .description("Description")
                    .build();

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(existingSpace));
            when(spaceRepository.existsByUserIdAndName(USER_ID, "Taken Name")).thenReturn(true);

            assertThrows(
                    DuplicateResourceException.class,
                    () -> spaceService.updateSpace(SPACE_ID, USER_ID, request)
            );

            verify(spaceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteSpace")
    class DeleteSpace {

        @Test
        @DisplayName("Should delete space successfully when it exists and belongs to user")
        void shouldDeleteSpaceSuccessfully() {
            Space space = buildSpace(SPACE_ID, "To Delete", "Will be deleted");

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(space));

            spaceService.deleteSpace(SPACE_ID, USER_ID);

            verify(spaceRepository).delete(space);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when space to delete does not exist")
        void shouldThrowResourceNotFoundWhenDeletingNonexistentSpace() {
            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> spaceService.deleteSpace(SPACE_ID, USER_ID)
            );

            verify(spaceRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("saveArticleToSpace")
    class SaveArticleToSpace {

        @Test
        @DisplayName("Should save article to space successfully")
        void shouldSaveArticleSuccessfully() {
            Space space = buildSpace(SPACE_ID, "Tech News", "Tech articles");
            SaveArticleRequest request = buildSaveArticleRequest();

            SavedArticle savedArticle = SavedArticle.builder()
                    .id(ARTICLE_ID)
                    .space(space)
                    .externalId("ext-123")
                    .source(NewsSource.FINNHUB)
                    .symbol("AAPL")
                    .title("Apple stock surges")
                    .summary("Apple Inc. stock rose 5%")
                    .sourceUrl("https://example.com/apple")
                    .imageUrl("https://example.com/image.jpg")
                    .sentiment(Sentiment.POSITIVE)
                    .sentimentScore(0.85)
                    .publishedAt(OffsetDateTime.now())
                    .savedAt(OffsetDateTime.now())
                    .build();

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(space));
            when(savedArticleRepository.existsBySpaceIdAndSourceUrl(SPACE_ID, "https://example.com/apple"))
                    .thenReturn(false);
            when(savedArticleRepository.save(any(SavedArticle.class))).thenReturn(savedArticle);

            SavedArticleResponse response = spaceService.saveArticleToSpace(SPACE_ID, USER_ID, request);

            assertNotNull(response);
            assertEquals(ARTICLE_ID, response.getId());
            assertEquals("Apple stock surges", response.getTitle());
            assertEquals("FINNHUB", response.getSource());
            assertEquals("POSITIVE", response.getSentiment());
            verify(savedArticleRepository).save(any(SavedArticle.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when article is already saved in space")
        void shouldThrowDuplicateWhenArticleAlreadySaved() {
            Space space = buildSpace(SPACE_ID, "Tech News", "Tech articles");
            SaveArticleRequest request = buildSaveArticleRequest();

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(space));
            when(savedArticleRepository.existsBySpaceIdAndSourceUrl(SPACE_ID, "https://example.com/apple"))
                    .thenReturn(true);

            assertThrows(
                    DuplicateResourceException.class,
                    () -> spaceService.saveArticleToSpace(SPACE_ID, USER_ID, request)
            );

            verify(savedArticleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getArticlesInSpace")
    class GetArticlesInSpace {

        @Test
        @DisplayName("Should return articles for a space")
        void shouldReturnArticlesForSpace() {
            Space space = buildSpace(SPACE_ID, "Tech News", "Tech articles");
            SavedArticle article = SavedArticle.builder()
                    .id(ARTICLE_ID)
                    .space(space)
                    .source(NewsSource.FINNHUB)
                    .title("Test article")
                    .sourceUrl("https://example.com/test")
                    .sentimentScore(0.0)
                    .savedAt(OffsetDateTime.now())
                    .build();

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(space));
            when(savedArticleRepository.findBySpaceIdOrderBySavedAtDesc(SPACE_ID))
                    .thenReturn(List.of(article));

            List<SavedArticleResponse> responses = spaceService.getArticlesInSpace(SPACE_ID, USER_ID);

            assertEquals(1, responses.size());
            assertEquals("Test article", responses.get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("removeArticleFromSpace")
    class RemoveArticleFromSpace {

        @Test
        @DisplayName("Should remove article from space successfully")
        void shouldRemoveArticleSuccessfully() {
            Space space = buildSpace(SPACE_ID, "Tech News", "Tech articles");
            SavedArticle article = SavedArticle.builder()
                    .id(ARTICLE_ID)
                    .space(space)
                    .source(NewsSource.FINNHUB)
                    .title("To remove")
                    .sourceUrl("https://example.com/remove")
                    .sentimentScore(0.0)
                    .savedAt(OffsetDateTime.now())
                    .build();

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(space));
            when(savedArticleRepository.findByIdAndSpaceId(ARTICLE_ID, SPACE_ID))
                    .thenReturn(Optional.of(article));

            spaceService.removeArticleFromSpace(SPACE_ID, ARTICLE_ID, USER_ID);

            verify(savedArticleRepository).delete(article);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when article does not exist in space")
        void shouldThrowResourceNotFoundWhenArticleNotInSpace() {
            Space space = buildSpace(SPACE_ID, "Tech News", "Tech articles");

            when(spaceRepository.findByIdAndUserId(SPACE_ID, USER_ID))
                    .thenReturn(Optional.of(space));
            when(savedArticleRepository.findByIdAndSpaceId(ARTICLE_ID, SPACE_ID))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> spaceService.removeArticleFromSpace(SPACE_ID, ARTICLE_ID, USER_ID)
            );

            assertTrue(exception.getMessage().contains(String.valueOf(ARTICLE_ID)));
            verify(savedArticleRepository, never()).delete(any(SavedArticle.class));
        }
    }

    /**
     * Builds a Space entity with pre-set timestamps and an empty article list.
     *
     * @param id          the space ID
     * @param name        the space name
     * @param description the space description
     * @return a fully initialized Space entity
     */
    private Space buildSpace(Long id, String name, String description) {
        return Space.builder()
                .id(id)
                .userId(USER_ID)
                .name(name)
                .description(description)
                .savedArticles(new ArrayList<>())
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
                .publishedAt(OffsetDateTime.now())
                .build();
    }
}
