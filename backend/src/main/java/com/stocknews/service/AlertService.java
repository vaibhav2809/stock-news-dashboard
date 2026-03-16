package com.stocknews.service;

import com.stocknews.dto.AlertResponse;
import com.stocknews.dto.CreateAlertRequest;
import com.stocknews.exception.RateLimitExceededException;
import com.stocknews.exception.ResourceNotFoundException;
import com.stocknews.model.entity.Alert;
import com.stocknews.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing user-configured stock news alerts.
 * Handles CRUD operations and enforces the per-user alert limit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private static final int MAX_ALERTS_PER_USER = 20;

    private final AlertRepository alertRepository;

    /**
     * Creates a new alert for the given user.
     * Normalizes the symbol to uppercase and enforces a maximum of {@value MAX_ALERTS_PER_USER} alerts per user.
     *
     * @param userId  the authenticated user's ID
     * @param request the alert creation request with symbol, type, threshold, sentiment, and frequency
     * @return the created alert as a response DTO
     * @throws RateLimitExceededException if the user has reached the maximum alert limit
     */
    @Transactional
    public AlertResponse createAlert(Long userId, CreateAlertRequest request) {
        final String normalizedSymbol = request.getSymbol().trim().toUpperCase();
        log.info("Creating alert for userId={}, symbol={}, alertType={}", userId, normalizedSymbol, request.getAlertType());

        final long currentAlertCount = alertRepository.countByUserId(userId);
        if (currentAlertCount >= MAX_ALERTS_PER_USER) {
            throw new RateLimitExceededException(
                    "Maximum alert limit reached (" + MAX_ALERTS_PER_USER + "). Delete an existing alert before creating a new one.");
        }

        final Alert alert = Alert.builder()
                .userId(userId)
                .symbol(normalizedSymbol)
                .alertType(request.getAlertType())
                .thresholdValue(request.getThresholdValue())
                .targetSentiment(request.getTargetSentiment())
                .frequency(request.getFrequency())
                .build();

        final Alert savedAlert = alertRepository.save(alert);
        log.info("Created alert id={} for userId={}, symbol={}, alertType={}",
                savedAlert.getId(), userId, normalizedSymbol, request.getAlertType());

        return AlertResponse.fromEntity(savedAlert);
    }

    /**
     * Retrieves all alerts for the given user, ordered by creation date descending.
     *
     * @param userId the authenticated user's ID
     * @return list of alert response DTOs
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> getAlerts(Long userId) {
        log.debug("Fetching alerts for userId={}", userId);
        final List<Alert> alerts = alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return alerts.stream()
                .map(AlertResponse::fromEntity)
                .toList();
    }

    /**
     * Toggles the active state of an alert (active becomes inactive and vice versa).
     *
     * @param userId  the authenticated user's ID
     * @param alertId the ID of the alert to toggle
     * @return the updated alert as a response DTO
     * @throws ResourceNotFoundException if the alert does not exist or does not belong to the user
     */
    @Transactional
    public AlertResponse toggleAlert(Long userId, Long alertId) {
        log.info("Toggling alert id={} for userId={}", alertId, userId);

        final Alert alert = alertRepository.findByUserIdAndId(userId, alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id=" + alertId + " not found for your account"));

        alert.setIsActive(!alert.getIsActive());
        final Alert updatedAlert = alertRepository.save(alert);

        log.info("Toggled alert id={} for userId={}, isActive={}", alertId, userId, updatedAlert.getIsActive());
        return AlertResponse.fromEntity(updatedAlert);
    }

    /**
     * Deletes an alert belonging to the given user.
     *
     * @param userId  the authenticated user's ID
     * @param alertId the ID of the alert to delete
     * @throws ResourceNotFoundException if the alert does not exist or does not belong to the user
     */
    @Transactional
    public void deleteAlert(Long userId, Long alertId) {
        log.info("Deleting alert id={} for userId={}", alertId, userId);

        final Alert alert = alertRepository.findByUserIdAndId(userId, alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert with id=" + alertId + " not found for your account"));

        alertRepository.delete(alert);
        log.info("Deleted alert id={} for userId={}", alertId, userId);
    }
}
