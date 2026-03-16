package com.stocknews.model.entity;

import com.stocknews.model.enums.AlertFrequency;
import com.stocknews.model.enums.AlertType;
import com.stocknews.model.enums.Sentiment;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Represents a user-configured alert for a stock symbol.
 * Alerts are evaluated periodically by the scheduler and trigger when their condition is met.
 * Each alert has a type (NEWS_VOLUME, SENTIMENT_CHANGE, BREAKING_NEWS), a frequency-based
 * cooldown, and tracks when it was last triggered.
 */
@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who owns this alert. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** The stock ticker symbol being monitored (e.g., "AAPL"). */
    @Column(nullable = false, length = 20)
    private String symbol;

    /** The type of condition that triggers this alert. */
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 30)
    private AlertType alertType;

    /** Numeric threshold for NEWS_VOLUME alerts (e.g., article count). Nullable for BREAKING_NEWS. */
    @Column(name = "threshold_value")
    private Double thresholdValue;

    /** Target sentiment for SENTIMENT_CHANGE alerts (e.g., NEGATIVE). Nullable for other types. */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_sentiment", length = 20)
    private Sentiment targetSentiment;

    /** How often this alert can re-trigger (cooldown period). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AlertFrequency frequency = AlertFrequency.DAILY;

    /** Whether this alert is currently active and should be evaluated by the scheduler. */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Timestamp of the last time this alert was triggered. Null if never triggered. */
    @Column(name = "last_triggered_at")
    private OffsetDateTime lastTriggeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        final OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
