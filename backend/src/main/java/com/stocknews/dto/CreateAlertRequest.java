package com.stocknews.dto;

import com.stocknews.model.enums.AlertFrequency;
import com.stocknews.model.enums.AlertType;
import com.stocknews.model.enums.Sentiment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new stock news alert.
 * Validated at the controller boundary before reaching the service layer.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlertRequest {

    /** The stock ticker symbol to monitor (e.g., "AAPL"). */
    @NotBlank(message = "Symbol is required")
    private String symbol;

    /** The type of alert condition to evaluate. */
    @NotNull(message = "Alert type is required")
    private AlertType alertType;

    /** Numeric threshold for NEWS_VOLUME alerts. Nullable for BREAKING_NEWS and SENTIMENT_CHANGE. */
    private Double thresholdValue;

    /** Target sentiment for SENTIMENT_CHANGE alerts. Nullable for other alert types. */
    private Sentiment targetSentiment;

    /** How often this alert can re-trigger (cooldown period). */
    @NotNull(message = "Frequency is required")
    private AlertFrequency frequency;
}
