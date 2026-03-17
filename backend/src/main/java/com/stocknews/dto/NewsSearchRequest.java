package com.stocknews.dto;

import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Query parameters for searching and filtering news articles.
 * All fields are optional — omitting a field means "no filter" for that dimension.
 */
@Getter
@Setter
@Builder
public class NewsSearchRequest {

    /** Stock ticker symbols to search for (e.g., ["AAPL", "TSLA"]). */
    private List<String> symbols;

    /** Filter by news source (FINNHUB, NEWSDATA_IO). */
    private NewsSource source;

    /** Filter by sentiment classification. */
    private Sentiment sentiment;

    /** Start date for the search range (inclusive). */
    private LocalDate fromDate;

    /** End date for the search range (inclusive). */
    private LocalDate toDate;

    /** Free-text keyword to search in article titles and summaries. */
    private String keyword;

    /** Page number (zero-based). */
    @Builder.Default
    private int page = 0;

    /** Number of results per page. */
    @Builder.Default
    private int size = 20;
}
