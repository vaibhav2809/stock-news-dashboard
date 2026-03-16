package com.stocknews.model.entity;

import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Represents a news article fetched from an external API (Finnhub or NewsData.io).
 * Articles are deduplicated using a fingerprint hash of title + source URL.
 */
@Entity
@Table(name = "news_articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** External ID from the source API (e.g., Finnhub article ID). */
    @Column(name = "external_id")
    private String externalId;

    /** SHA-256 hash of (title + source_url) for deduplication. */
    @Column(nullable = false, unique = true, length = 64)
    private String fingerprint;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "source_url", nullable = false, length = 2000)
    private String sourceUrl;

    @Column(name = "image_url", length = 2000)
    private String imageUrl;

    /** Which external API this article came from. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NewsSource source;

    /** Stock ticker symbol this article relates to (e.g., "AAPL"). */
    @Column(length = 20)
    private String symbol;

    /** Sentiment classification: POSITIVE, NEGATIVE, or NEUTRAL. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Sentiment sentiment = Sentiment.NEUTRAL;

    /** Numeric sentiment score from -1.0 (very negative) to 1.0 (very positive). */
    @Column(name = "sentiment_score")
    @Builder.Default
    private Double sentimentScore = 0.0;

    /** When the article was originally published by the source. */
    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    /** When we fetched this article from the external API. */
    @Column(name = "fetched_at", nullable = false)
    private OffsetDateTime fetchedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (fetchedAt == null) {
            fetchedAt = OffsetDateTime.now();
        }
    }
}
