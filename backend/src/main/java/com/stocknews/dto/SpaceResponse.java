package com.stocknews.dto;

import com.stocknews.model.entity.Space;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for a Space (reading list).
 * Includes the article count but not the articles themselves (use SpaceDetailResponse for that).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceResponse {

    private Long id;
    private String name;
    private String description;
    private int articleCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * Creates a SpaceResponse from a Space entity.
     * @param space the JPA entity
     * @return mapped response DTO
     */
    public static SpaceResponse fromEntity(Space space) {
        return SpaceResponse.builder()
                .id(space.getId())
                .name(space.getName())
                .description(space.getDescription())
                .articleCount(space.getSavedArticles().size())
                .createdAt(space.getCreatedAt())
                .updatedAt(space.getUpdatedAt())
                .build();
    }
}
