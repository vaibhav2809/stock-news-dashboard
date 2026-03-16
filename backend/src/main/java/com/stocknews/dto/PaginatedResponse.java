package com.stocknews.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper for list endpoints.
 * Provides consistent pagination metadata across all list APIs.
 * Requires @NoArgsConstructor for Jackson deserialization from Redis cache.
 *
 * @param <T> the type of items in the response
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    /** The list of items for the current page. */
    private List<T> data;

    /** Current page number (zero-based). */
    private int page;

    /** Total number of pages available. */
    private int totalPages;

    /** Total number of items across all pages. */
    private long totalElements;

    /**
     * Creates a PaginatedResponse from a Spring Data Page object.
     * @param data the items for this page
     * @param page the current page number
     * @param totalPages total pages available
     * @param totalElements total items across all pages
     * @param <T> the item type
     * @return the paginated response
     */
    public static <T> PaginatedResponse<T> of(List<T> data, int page, int totalPages, long totalElements) {
        return PaginatedResponse.<T>builder()
                .data(data)
                .page(page)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }
}
