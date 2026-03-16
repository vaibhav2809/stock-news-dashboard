package com.stocknews.dto;

import com.stocknews.model.entity.StockSymbol;
import lombok.Builder;
import lombok.Getter;

/**
 * API response DTO for a stock symbol.
 * Used in autocomplete search and watchlist display.
 */
@Getter
@Builder
public class StockSymbolResponse {

    private final Long id;
    private final String ticker;
    private final String companyName;
    private final String exchange;

    /**
     * Converts a StockSymbol entity to its API response representation.
     * @param entity the JPA entity to convert
     * @return the response DTO
     */
    public static StockSymbolResponse fromEntity(StockSymbol entity) {
        return StockSymbolResponse.builder()
                .id(entity.getId())
                .ticker(entity.getTicker())
                .companyName(entity.getCompanyName())
                .exchange(entity.getExchange())
                .build();
    }
}
