package com.stocknews.repository;

import com.stocknews.model.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing alert CRUD operations.
 * Provides queries for user-scoped access and scheduler-driven evaluation.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Finds all alerts for a user, ordered by most recently created first.
     *
     * @param userId the user's database ID
     * @return list of alerts sorted by creation date descending
     */
    List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds all currently active alerts across all users.
     * Used by the scheduler to determine which alerts to evaluate.
     *
     * @return list of active alerts
     */
    List<Alert> findByIsActiveTrue();

    /**
     * Finds a specific alert owned by a specific user.
     * Used to verify ownership before toggle or delete operations.
     *
     * @param userId the user's database ID
     * @param id     the alert ID
     * @return the alert if it exists and belongs to the user
     */
    Optional<Alert> findByUserIdAndId(Long userId, Long id);

    /**
     * Counts the total number of alerts a user has created.
     * Used to enforce the per-user alert limit.
     *
     * @param userId the user's database ID
     * @return the alert count
     */
    long countByUserId(Long userId);
}
