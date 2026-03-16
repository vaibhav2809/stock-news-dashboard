package com.stocknews.model.entity;

import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Represents a news article saved to a Space (reading list).
 * Stores a denormalized snapshot of the article data because external API articles
 * are ephemeral and may not be available later.
 */
@Entity
@Table(name = "saved_articles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"space_id", "source_url"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(name = "external_id")
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NewsSource source;

    @Column(length = 20)
    private String symbol;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "source_url", nullable = false, columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sentiment sentiment;

    @Column(name = "sentiment_score")
    @Builder.Default
    private Double sentimentScore = 0.0;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private OffsetDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        this.savedAt = OffsetDateTime.now();
    }
}
