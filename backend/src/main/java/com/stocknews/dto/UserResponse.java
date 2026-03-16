package com.stocknews.dto;

import com.stocknews.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for user profile information.
 * Never exposes password hash or other sensitive internal fields.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String displayName;
    private OffsetDateTime createdAt;

    /**
     * Converts a User entity to its safe response representation.
     *
     * @param entity the JPA User entity
     * @return the response DTO without sensitive fields
     */
    public static UserResponse fromEntity(User entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .displayName(entity.getDisplayName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
