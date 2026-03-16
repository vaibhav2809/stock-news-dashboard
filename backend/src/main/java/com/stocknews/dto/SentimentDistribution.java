package com.stocknews.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the sentiment distribution for a specific stock symbol.
 * Shows counts and percentages for each sentiment category.
 * Includes no-arg constructor for Redis/Jackson deserialization compatibility.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentDistribution {

    private String symbol;
    private long totalArticles;
    private long positiveCount;
    private long negativeCount;
    private long neutralCount;
    private double positivePercent;
    private double negativePercent;
    private double neutralPercent;
    private double averageScore;

    /**
     * Creates a SentimentDistribution from raw counts and an average score.
     *
     * @param symbol the stock ticker
     * @param positiveCount number of positive articles
     * @param negativeCount number of negative articles
     * @param neutralCount number of neutral articles
     * @param averageScore the average sentiment score across all articles
     * @return the distribution DTO
     */
    public static SentimentDistribution of(String symbol, long positiveCount, long negativeCount,
                                            long neutralCount, double averageScore) {
        final long total = positiveCount + negativeCount + neutralCount;
        return SentimentDistribution.builder()
                .symbol(symbol)
                .totalArticles(total)
                .positiveCount(positiveCount)
                .negativeCount(negativeCount)
                .neutralCount(neutralCount)
                .positivePercent(total > 0 ? Math.round(positiveCount * 1000.0 / total) / 10.0 : 0.0)
                .negativePercent(total > 0 ? Math.round(negativeCount * 1000.0 / total) / 10.0 : 0.0)
                .neutralPercent(total > 0 ? Math.round(neutralCount * 1000.0 / total) / 10.0 : 0.0)
                .averageScore(Math.round(averageScore * 1000.0) / 1000.0)
                .build();
    }
}
