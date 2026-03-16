package com.stocknews.repository;

import com.stocknews.model.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Space CRUD operations.
 */
@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {

    /**
     * Finds all spaces belonging to a specific user, ordered by creation date.
     * @param userId the user's ID
     * @return list of spaces
     */
    List<Space> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds a specific space by ID and user ID (for ownership verification).
     * @param id the space ID
     * @param userId the user's ID
     * @return the space if it exists and belongs to the user
     */
    Optional<Space> findByIdAndUserId(Long id, Long userId);

    /**
     * Checks if a space with the given name already exists for a user.
     * @param userId the user's ID
     * @param name the space name
     * @return true if a duplicate exists
     */
    boolean existsByUserIdAndName(Long userId, String name);
}
