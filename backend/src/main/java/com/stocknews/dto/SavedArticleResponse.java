package com.stocknews.dto;

import com.stocknews.model.entity.SavedArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for a saved article within a Space.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedArticleResponse {

    private Long id;
    private String externalId;
    private String source;
    private String symbol;
    private String title;
    private String summary;
    private String sourceUrl;
    private String imageUrl;
    private String sentiment;
    private Double sentimentScore;
    private OffsetDateTime publishedAt;
    private OffsetDateTime savedAt;

    /**
     * Creates a SavedArticleResponse from a SavedArticle entity.
     * @param article the JPA entity
     * @return mapped response DTO
     */
    public static SavedArticleResponse fromEntity(SavedArticle article) {
        return SavedArticleResponse.builder()
                .id(article.getId())
                .externalId(article.getExternalId())
                .source(article.getSource().name())
                .symbol(article.getSymbol())
                .title(article.getTitle())
                .summary(article.getSummary())
                .sourceUrl(article.getSourceUrl())
                .imageUrl(article.getImageUrl())
                .sentiment(article.getSentiment() != null ? article.getSentiment().name() : null)
                .sentimentScore(article.getSentimentScore())
                .publishedAt(article.getPublishedAt())
                .savedAt(article.getSavedAt())
                .build();
    }
}
