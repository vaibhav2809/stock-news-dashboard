package com.stocknews.service;

import com.stocknews.model.enums.Sentiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Performs basic sentiment analysis on news article text using a financial keyword lexicon.
 * This is a rule-based approach using curated word lists for positive and negative financial sentiment.
 *
 * <p>The analyzer scores text by counting positive and negative keyword matches,
 * then normalizes the score to a range of -1.0 (strongly negative) to +1.0 (strongly positive).</p>
 *
 * <p>This is intentionally simple and suitable for a portfolio project. A production system
 * would use NLP models (e.g., FinBERT) or a third-party sentiment API.</p>
 */
@Slf4j
@Service
public class KeywordSentimentAnalyzer {

    /** Financial terms indicating positive sentiment. */
    private static final Set<String> POSITIVE_KEYWORDS = Set.of(
            "surge", "surges", "surging", "soar", "soars", "soaring",
            "rally", "rallies", "rallying", "gain", "gains", "gained",
            "rise", "rises", "rising", "climb", "climbs", "climbing",
            "jump", "jumps", "jumped", "boost", "boosts", "boosted",
            "beat", "beats", "beating", "exceed", "exceeds", "exceeded",
            "outperform", "outperforms", "upgrade", "upgraded", "upgrades",
            "bullish", "optimistic", "positive", "strong", "stronger",
            "growth", "growing", "profit", "profitable", "profitability",
            "record", "breakthrough", "innovation", "innovative",
            "revenue", "earnings", "dividend", "expansion", "expanding",
            "recovery", "recovering", "momentum", "upside", "opportunity",
            "buy", "overweight", "outpace", "accelerate", "accelerating",
            "success", "successful", "promising", "impressive", "robust"
    );

    /** Financial terms indicating negative sentiment. */
    private static final Set<String> NEGATIVE_KEYWORDS = Set.of(
            "crash", "crashes", "crashing", "plunge", "plunges", "plunging",
            "decline", "declines", "declining", "drop", "drops", "dropped",
            "fall", "falls", "falling", "sink", "sinks", "sinking",
            "tumble", "tumbles", "tumbling", "slump", "slumps", "slumping",
            "loss", "losses", "losing", "miss", "misses", "missed",
            "fail", "fails", "failed", "failure", "warning", "warnings",
            "downgrade", "downgraded", "downgrades", "bearish", "pessimistic",
            "weak", "weaker", "weakness", "risk", "risks", "risky",
            "debt", "deficit", "layoff", "layoffs", "restructuring",
            "recession", "recessionary", "inflation", "inflationary",
            "lawsuit", "investigation", "fraud", "scandal", "violation",
            "sell", "underweight", "underperform", "overvalued",
            "concern", "concerns", "worried", "uncertainty", "volatile",
            "bankruptcy", "default", "crisis", "downturn", "negative"
    );

    /** Minimum absolute score to classify as non-neutral. */
    private static final double NEUTRAL_THRESHOLD = 0.1;

    /**
     * Analyzes the combined text of a news article and returns a sentiment score.
     * The score ranges from -1.0 (very negative) to +1.0 (very positive).
     *
     * @param title the article title (weighted 2x because titles are more indicative)
     * @param summary the article summary/body text
     * @return sentiment score between -1.0 and 1.0
     */
    public double analyzeSentimentScore(String title, String summary) {
        if ((title == null || title.isBlank()) && (summary == null || summary.isBlank())) {
            return 0.0;
        }

        // Title gets double weight because it's more indicative of the article's sentiment
        final String titleText = title != null ? title.toLowerCase() : "";
        final String summaryText = summary != null ? summary.toLowerCase() : "";

        int positiveCount = 0;
        int negativeCount = 0;

        // Score the title (2x weight)
        for (final String word : extractWords(titleText)) {
            if (POSITIVE_KEYWORDS.contains(word)) {
                positiveCount += 2;
            } else if (NEGATIVE_KEYWORDS.contains(word)) {
                negativeCount += 2;
            }
        }

        // Score the summary (1x weight)
        for (final String word : extractWords(summaryText)) {
            if (POSITIVE_KEYWORDS.contains(word)) {
                positiveCount++;
            } else if (NEGATIVE_KEYWORDS.contains(word)) {
                negativeCount++;
            }
        }

        final int totalMatches = positiveCount + negativeCount;
        if (totalMatches == 0) {
            return 0.0;
        }

        // Normalize to [-1.0, 1.0] range
        final double rawScore = (double) (positiveCount - negativeCount) / totalMatches;

        // Clamp to [-1.0, 1.0]
        return Math.max(-1.0, Math.min(1.0, rawScore));
    }

    /**
     * Converts a numeric sentiment score to a categorical Sentiment enum.
     *
     * @param score the sentiment score (-1.0 to 1.0)
     * @return POSITIVE if score > threshold, NEGATIVE if score < -threshold, else NEUTRAL
     */
    public Sentiment classifySentiment(double score) {
        if (score > NEUTRAL_THRESHOLD) {
            return Sentiment.POSITIVE;
        } else if (score < -NEUTRAL_THRESHOLD) {
            return Sentiment.NEGATIVE;
        }
        return Sentiment.NEUTRAL;
    }

    /**
     * Convenience method that analyzes text and returns both score and classification.
     *
     * @param title the article title
     * @param summary the article summary
     * @return a SentimentResult containing the score and classification
     */
    public SentimentResult analyze(String title, String summary) {
        final double score = analyzeSentimentScore(title, summary);
        final Sentiment sentiment = classifySentiment(score);
        return new SentimentResult(sentiment, score);
    }

    /**
     * Splits text into lowercase words, stripping punctuation.
     * @param text the input text
     * @return array of cleaned words
     */
    private String[] extractWords(String text) {
        return text.replaceAll("[^a-zA-Z\\s]", " ")
                .trim()
                .split("\\s+");
    }

    /**
     * Holds the result of sentiment analysis: both the classification and numeric score.
     *
     * @param sentiment the categorical classification (POSITIVE, NEGATIVE, NEUTRAL)
     * @param score the numeric score from -1.0 to 1.0
     */
    public record SentimentResult(Sentiment sentiment, double score) {}
}
