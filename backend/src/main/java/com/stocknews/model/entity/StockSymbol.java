package com.stocknews.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Represents a trackable stock symbol (e.g., AAPL = Apple Inc.).
 * Used for autocomplete search and watchlist management.
 */
@Entity
@Table(name = "stock_symbols")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSymbol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stock ticker symbol (e.g., "AAPL", "TSLA"). */
    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    /** Full company name (e.g., "Apple Inc."). */
    @Column(name = "company_name", nullable = false)
    private String companyName;

    /** Stock exchange where this symbol is listed (e.g., "NASDAQ", "NYSE"). */
    @Column(length = 50)
    private String exchange;

    /** Whether this symbol is actively tracked for news. */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
