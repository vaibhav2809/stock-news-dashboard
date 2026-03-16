package com.stocknews.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Represents a stock symbol that a user is actively watching.
 * Each user has one implicit watchlist — no separate "watchlist" entity needed.
 * The unique constraint on (user_id, symbol) prevents duplicate entries.
 */
@Entity
@Table(name = "watchlist_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who is watching this symbol. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** The stock ticker being watched (e.g., "AAPL"). */
    @Column(nullable = false, length = 20)
    private String symbol;

    /** When the user added this symbol to their watchlist. */
    @Column(name = "added_at", nullable = false, updatable = false)
    private OffsetDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = OffsetDateTime.now();
        }
    }
}
