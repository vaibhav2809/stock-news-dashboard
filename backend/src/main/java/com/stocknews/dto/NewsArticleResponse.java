package com.stocknews.dto;

import com.stocknews.model.entity.NewsArticle;
import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * API response DTO for a single news article.
 * Decouples the JPA entity from the REST response shape.
 * Includes no-arg constructor for Redis/Jackson deserialization compatibility.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleResponse {

    private Long id;
    private String title;
    private String summary;
    private String sourceUrl;
    private String imageUrl;
    private NewsSource source;
    private String symbol;
    private Sentiment sentiment;
    private Double sentimentScore;
    private OffsetDateTime publishedAt;

    /**
     * Converts a NewsArticle entity to its API response representation.
     * @param entity the JPA entity to convert
     * @return the response DTO
     */
    public static NewsArticleResponse fromEntity(NewsArticle entity) {
        return NewsArticleResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .sourceUrl(entity.getSourceUrl())
                .imageUrl(entity.getImageUrl())
                .source(entity.getSource())
                .symbol(entity.getSymbol())
                .sentiment(entity.getSentiment())
                .sentimentScore(entity.getSentimentScore())
                .publishedAt(entity.getPublishedAt())
                .build();
    }
}
