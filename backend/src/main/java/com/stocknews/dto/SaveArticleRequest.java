package com.stocknews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Request DTO for saving an article to a Space.
 * Contains a denormalized snapshot of the article data.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveArticleRequest {

    private String externalId;

    @NotBlank(message = "Source is required")
    private String source;

    private String symbol;

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be 500 characters or less")
    private String title;

    private String summary;

    @NotBlank(message = "Source URL is required")
    private String sourceUrl;

    private String imageUrl;

    private String sentiment;

    private Double sentimentScore;

    private OffsetDateTime publishedAt;
}
