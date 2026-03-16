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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing Spaces (reading lists) and their saved articles.
 * Handles CRUD operations, ownership verification, and duplicate prevention.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final SavedArticleRepository savedArticleRepository;

    /**
     * Creates a new space for a user.
     *
     * @param userId  the owning user's ID
     * @param request the space details (name, description)
     * @return the created space response
     * @throws DuplicateResourceException if a space with the same name already exists for this user
     */
    @Transactional
    public SpaceResponse createSpace(Long userId, SpaceRequest request) {
        log.info("Creating space '{}' for userId={}", request.getName(), userId);

        if (spaceRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new DuplicateResourceException(
                    "A space named '" + request.getName() + "' already exists");
        }

        final Space space = Space.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        final Space savedSpace = spaceRepository.save(space);
        log.info("Created space id={} for userId={}", savedSpace.getId(), userId);

        return SpaceResponse.fromEntity(savedSpace);
    }

    /**
     * Lists all spaces belonging to a user.
     *
     * @param userId the user's ID
     * @return list of space responses
     */
    @Transactional(readOnly = true)
    public List<SpaceResponse> getSpacesByUserId(Long userId) {
        log.debug("Fetching spaces for userId={}", userId);

        return spaceRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(SpaceResponse::fromEntity)
                .toList();
    }

    /**
     * Gets a space by ID with ownership verification.
     *
     * @param spaceId the space ID
     * @param userId  the user's ID (for ownership check)
     * @return the space response
     * @throws ResourceNotFoundException if the space doesn't exist or doesn't belong to the user
     */
    @Transactional(readOnly = true)
    public SpaceResponse getSpaceById(Long spaceId, Long userId) {
        log.debug("Fetching space id={} for userId={}", spaceId, userId);

        final Space space = findSpaceByIdAndUserId(spaceId, userId);
        return SpaceResponse.fromEntity(space);
    }

    /**
     * Updates a space's name and/or description.
     *
     * @param spaceId the space ID
     * @param userId  the user's ID (for ownership check)
     * @param request the updated space details
     * @return the updated space response
     * @throws ResourceNotFoundException  if the space doesn't exist or doesn't belong to the user
     * @throws DuplicateResourceException if the new name conflicts with an existing space
     */
    @Transactional
    public SpaceResponse updateSpace(Long spaceId, Long userId, SpaceRequest request) {
        log.info("Updating space id={} for userId={}", spaceId, userId);

        final Space space = findSpaceByIdAndUserId(spaceId, userId);

        // Check for name conflict (only if name is changing)
        if (!space.getName().equals(request.getName())
                && spaceRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new DuplicateResourceException(
                    "A space named '" + request.getName() + "' already exists");
        }

        space.setName(request.getName());
        space.setDescription(request.getDescription());

        final Space updatedSpace = spaceRepository.save(space);
        log.info("Updated space id={}", updatedSpace.getId());

        return SpaceResponse.fromEntity(updatedSpace);
    }

    /**
     * Deletes a space and all its saved articles.
     *
     * @param spaceId the space ID
     * @param userId  the user's ID (for ownership check)
     * @throws ResourceNotFoundException if the space doesn't exist or doesn't belong to the user
     */
    @Transactional
    public void deleteSpace(Long spaceId, Long userId) {
        log.info("Deleting space id={} for userId={}", spaceId, userId);

        final Space space = findSpaceByIdAndUserId(spaceId, userId);
        spaceRepository.delete(space);
        log.info("Deleted space id={} and its saved articles", spaceId);
    }

    /**
     * Saves an article to a space.
     *
     * @param spaceId the space ID
     * @param userId  the user's ID (for ownership check)
     * @param request the article data to save
     * @return the saved article response
     * @throws ResourceNotFoundException  if the space doesn't exist or doesn't belong to the user
     * @throws DuplicateResourceException if the article is already saved in this space
     */
    @Transactional
    public SavedArticleResponse saveArticleToSpace(Long spaceId, Long userId, SaveArticleRequest request) {
        log.info("Saving article to space id={} for userId={}", spaceId, userId);

        final Space space = findSpaceByIdAndUserId(spaceId, userId);

        if (savedArticleRepository.existsBySpaceIdAndSourceUrl(spaceId, request.getSourceUrl())) {
            throw new DuplicateResourceException("This article is already saved in this space");
        }

        final SavedArticle article = SavedArticle.builder()
                .space(space)
                .externalId(request.getExternalId())
                .source(NewsSource.valueOf(request.getSource()))
                .symbol(request.getSymbol())
                .title(request.getTitle())
                .summary(request.getSummary())
                .sourceUrl(request.getSourceUrl())
                .imageUrl(request.getImageUrl())
                .sentiment(request.getSentiment() != null ? Sentiment.valueOf(request.getSentiment()) : null)
                .sentimentScore(request.getSentimentScore() != null ? request.getSentimentScore() : 0.0)
                .publishedAt(request.getPublishedAt())
                .build();

        final SavedArticle savedArticle = savedArticleRepository.save(article);
        log.info("Saved article id={} to space id={}", savedArticle.getId(), spaceId);

        return SavedArticleResponse.fromEntity(savedArticle);
    }

    /**
     * Gets all saved articles in a space.
     *
     * @param spaceId the space ID
     * @param userId  the user's ID (for ownership check)
     * @return list of saved article responses
     * @throws ResourceNotFoundException if the space doesn't exist or doesn't belong to the user
     */
    @Transactional(readOnly = true)
    public List<SavedArticleResponse> getArticlesInSpace(Long spaceId, Long userId) {
        log.debug("Fetching articles for space id={}", spaceId);

        findSpaceByIdAndUserId(spaceId, userId); // Verify ownership

        return savedArticleRepository.findBySpaceIdOrderBySavedAtDesc(spaceId).stream()
                .map(SavedArticleResponse::fromEntity)
                .toList();
    }

    /**
     * Removes a saved article from a space.
     *
     * @param spaceId   the space ID
     * @param articleId the saved article ID
     * @param userId    the user's ID (for ownership check)
     * @throws ResourceNotFoundException if the space or article doesn't exist
     */
    @Transactional
    public void removeArticleFromSpace(Long spaceId, Long articleId, Long userId) {
        log.info("Removing article id={} from space id={} for userId={}", articleId, spaceId, userId);

        findSpaceByIdAndUserId(spaceId, userId); // Verify ownership

        final SavedArticle article = savedArticleRepository.findByIdAndSpaceId(articleId, spaceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Saved article not found with id=" + articleId + " in space id=" + spaceId));

        savedArticleRepository.delete(article);
        log.info("Removed article id={} from space id={}", articleId, spaceId);
    }

    /**
     * Finds a space by ID and verifies ownership.
     *
     * @param spaceId the space ID
     * @param userId  the expected owner's ID
     * @return the space entity
     * @throws ResourceNotFoundException if not found or not owned by the user
     */
    private Space findSpaceByIdAndUserId(Long spaceId, Long userId) {
        return spaceRepository.findByIdAndUserId(spaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Space not found with id=" + spaceId));
    }
}
