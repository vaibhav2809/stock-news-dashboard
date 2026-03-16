package com.stocknews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a Space (reading list).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceRequest {

    @NotBlank(message = "Space name is required")
    @Size(max = 100, message = "Space name must be 100 characters or less")
    private String name;

    @Size(max = 1000, message = "Description must be 1000 characters or less")
    private String description;
}
