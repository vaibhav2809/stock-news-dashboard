package com.stocknews.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a single data point on the sentiment timeline chart.
 * Aggregates sentiment data for a specific date.
 * Includes no-arg constructor for Redis/Jackson deserialization compatibility.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentTimelineEntry {

    private LocalDate date;
    private long articleCount;
    private long positiveCount;
    private long negativeCount;
    private long neutralCount;
    private double averageScore;
}
