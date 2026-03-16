package com.stocknews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for adding a symbol to a user's watchlist.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddWatchlistItemRequest {

    /** The stock ticker symbol to watch (e.g., "AAPL"). */
    @NotBlank(message = "Symbol is required")
    @Size(max = 20, message = "Symbol must be at most 20 characters")
    private String symbol;
}
