package com.stocknews.dto;

import com.stocknews.model.entity.WatchlistItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * API response DTO for a watchlist item.
 * Includes the symbol, company name (resolved from stock_symbols), and article count.
 * Includes no-arg constructor for Redis/Jackson deserialization compatibility.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItemResponse {

    private Long id;
    private String symbol;
    private String companyName;
    private long articleCount;
    private OffsetDateTime addedAt;

    /**
     * Converts a WatchlistItem entity to its response DTO.
     * Company name and article count are resolved separately by the service layer.
     * @param entity the watchlist item entity
     * @param companyName the resolved company name
     * @param articleCount number of articles for this symbol
     * @return the response DTO
     */
    public static WatchlistItemResponse fromEntity(WatchlistItem entity, String companyName, long articleCount) {
        return WatchlistItemResponse.builder()
                .id(entity.getId())
                .symbol(entity.getSymbol())
                .companyName(companyName)
                .articleCount(articleCount)
                .addedAt(entity.getAddedAt())
                .build();
    }
}
