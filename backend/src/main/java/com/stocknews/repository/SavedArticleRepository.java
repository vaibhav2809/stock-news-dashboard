package com.stocknews.repository;

import com.stocknews.model.entity.SavedArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SavedArticle CRUD operations.
 */
@Repository
public interface SavedArticleRepository extends JpaRepository<SavedArticle, Long> {

    /**
     * Finds all saved articles in a space, ordered by save date descending.
     * @param spaceId the space ID
     * @return list of saved articles
     */
    List<SavedArticle> findBySpaceIdOrderBySavedAtDesc(Long spaceId);

    /**
     * Finds a specific saved article by ID and space ID.
     * @param id the saved article ID
     * @param spaceId the space ID
     * @return the saved article if found
     */
    Optional<SavedArticle> findByIdAndSpaceId(Long id, Long spaceId);

    /**
     * Checks if an article with the given URL already exists in a space.
     * @param spaceId the space ID
     * @param sourceUrl the article's source URL
     * @return true if the article is already saved in this space
     */
    boolean existsBySpaceIdAndSourceUrl(Long spaceId, String sourceUrl);

    /**
     * Counts the number of articles in a space.
     * @param spaceId the space ID
     * @return the article count
     */
    long countBySpaceId(Long spaceId);
}
