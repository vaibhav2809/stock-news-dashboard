package com.stocknews.repository;

import com.stocknews.model.entity.NewsArticle;
import com.stocknews.model.enums.NewsSource;
import com.stocknews.model.enums.Sentiment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repository for news article CRUD and search operations.
 * Uses Spring Data JPA for automatic query generation.
 */
@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    /**
     * Checks if an article with this fingerprint already exists (deduplication).
     * @param fingerprint SHA-256 hash of title + source URL
     * @return true if a duplicate exists
     */
    boolean existsByFingerprint(String fingerprint);

    /**
     * Finds articles by stock symbol, ordered by publication date descending.
     * @param symbol the stock ticker (e.g., "AAPL")
     * @param pageable pagination parameters
     * @return paginated list of articles
     */
    Page<NewsArticle> findBySymbolOrderByPublishedAtDesc(String symbol, Pageable pageable);

    /**
     * Finds articles matching multiple optional filters including keyword search.
     * Uses JPQL with conditional logic so any filter can be null (meaning "no filter").
     * When a keyword is provided, it searches in both title and summary (case-insensitive).
     *
     * @param symbols list of ticker symbols (null = all symbols)
     * @param hasSymbols whether the symbols list is non-empty
     * @param source news source filter (null = all sources)
     * @param sentiment sentiment filter (null = all sentiments)
     * @param fromDate start date (null = no start bound)
     * @param toDate end date (null = no end bound)
     * @param keyword free-text keyword to match in title or summary (null = no keyword filter)
     * @param hasKeyword whether the keyword is non-empty
     * @param pageable pagination parameters
     * @return paginated, filtered list of articles
     */
    @Query("""
            SELECT a FROM NewsArticle a
            WHERE (:hasSymbols = false OR a.symbol IN :symbols)
              AND (:#{#source == null} = true OR a.source = :source)
              AND (:#{#sentiment == null} = true OR a.sentiment = :sentiment)
              AND (:#{#fromDate == null} = true OR a.publishedAt >= :fromDate)
              AND (:#{#toDate == null} = true OR a.publishedAt <= :toDate)
              AND (:hasKeyword = false OR (LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))))
            ORDER BY a.publishedAt DESC
            """)
    Page<NewsArticle> searchArticles(
            @Param("symbols") List<String> symbols,
            @Param("hasSymbols") boolean hasSymbols,
            @Param("source") NewsSource source,
            @Param("sentiment") Sentiment sentiment,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate,
            @Param("keyword") String keyword,
            @Param("hasKeyword") boolean hasKeyword,
            Pageable pageable
    );

    /**
     * Searches articles by keyword in title or summary, ordered by publication date descending.
     * Used for the dedicated keyword search endpoint.
     *
     * @param keyword the search keyword (case-insensitive partial match)
     * @param pageable pagination parameters
     * @return paginated list of matching articles
     */
    @Query("""
            SELECT n FROM NewsArticle n
            WHERE (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(n.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY n.publishedAt DESC
            """)
    Page<NewsArticle> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Finds the most recent articles across all symbols for the trending page.
     * @param since only include articles published after this time
     * @param pageable pagination parameters
     * @return paginated list of trending articles
     */
    Page<NewsArticle> findByPublishedAtAfterOrderByPublishedAtDesc(OffsetDateTime since, Pageable pageable);

    /**
     * Counts the total number of articles for a given stock symbol.
     * Used by the watchlist feature to show article counts per watched symbol.
     * @param symbol the stock ticker
     * @return article count
     */
    long countBySymbol(String symbol);

    /**
     * Counts articles by symbol and sentiment, used for sentiment distribution.
     * @param symbol the stock ticker
     * @param sentiment the sentiment classification
     * @return count of articles matching both criteria
     */
    long countBySymbolAndSentiment(String symbol, Sentiment sentiment);

    /**
     * Finds articles for a specific symbol within a date range, ordered by published date.
     * Used for sentiment history timeline.
     * @param symbol the stock ticker
     * @param fromDate start of the range
     * @param toDate end of the range
     * @param pageable pagination
     * @return paginated articles
     */
    Page<NewsArticle> findBySymbolAndPublishedAtBetweenOrderByPublishedAtDesc(
            String symbol, OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable);
}
