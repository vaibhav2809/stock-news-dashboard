package com.stocknews.dto;

import com.stocknews.model.entity.Alert;
import com.stocknews.model.enums.AlertFrequency;
import com.stocknews.model.enums.AlertType;
import com.stocknews.model.enums.Sentiment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * API response DTO for an alert.
 * Includes all alert configuration fields and metadata.
 * Includes no-arg constructor for Redis/Jackson deserialization compatibility.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;
    private String symbol;
    private AlertType alertType;
    private Double thresholdValue;
    private Sentiment targetSentiment;
    private AlertFrequency frequency;
    private Boolean isActive;
    private OffsetDateTime lastTriggeredAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * Converts an Alert entity to its response DTO.
     *
     * @param entity the alert entity
     * @return the response DTO with all fields mapped
     */
    public static AlertResponse fromEntity(Alert entity) {
        return AlertResponse.builder()
                .id(entity.getId())
                .symbol(entity.getSymbol())
                .alertType(entity.getAlertType())
                .thresholdValue(entity.getThresholdValue())
                .targetSentiment(entity.getTargetSentiment())
                .frequency(entity.getFrequency())
                .isActive(entity.getIsActive())
                .lastTriggeredAt(entity.getLastTriggeredAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
