package com.stocknews.service;

import com.stocknews.model.enums.Sentiment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KeywordSentimentAnalyzer.
 * Verifies keyword-based sentiment scoring, classification thresholds,
 * title weighting, and edge cases (null/empty input).
 */
class KeywordSentimentAnalyzerTest {

    private final KeywordSentimentAnalyzer analyzer = new KeywordSentimentAnalyzer();

    @Nested
    @DisplayName("analyze")
    class Analyze {

        @Test
        @DisplayName("Should return positive sentiment for text with positive financial keywords")
        void shouldReturnPositiveSentimentForPositiveText() {
            KeywordSentimentAnalyzer.SentimentResult result = analyzer.analyze(
                    "Apple stock surges after record earnings beat expectations",
                    "The company reported strong growth and impressive revenue gains this quarter."
            );

            assertEquals(Sentiment.POSITIVE, result.sentiment());
            assertTrue(result.score() > 0.1, "Score should be above neutral threshold");
        }

        @Test
        @DisplayName("Should return negative sentiment for text with negative financial keywords")
        void shouldReturnNegativeSentimentForNegativeText() {
            KeywordSentimentAnalyzer.SentimentResult result = analyzer.analyze(
                    "Stock crashes after fraud investigation announced",
                    "The company faces bankruptcy risks amid declining revenue and massive losses."
            );

            assertEquals(Sentiment.NEGATIVE, result.sentiment());
            assertTrue(result.score() < -0.1, "Score should be below negative neutral threshold");
        }

        @Test
        @DisplayName("Should return neutral sentiment for text with no financial keywords")
        void shouldReturnNeutralSentimentForNeutralText() {
            KeywordSentimentAnalyzer.SentimentResult result = analyzer.analyze(
                    "Company announces new office location",
                    "The firm will move its headquarters to a different building next year."
            );

            assertEquals(Sentiment.NEUTRAL, result.sentiment());
            assertTrue(Math.abs(result.score()) <= 0.1, "Score should be within neutral threshold");
        }

        @Test
        @DisplayName("Should return neutral with 0.0 score when title and summary are both null")
        void shouldReturnNeutralWhenBothNull() {
            KeywordSentimentAnalyzer.SentimentResult result = analyzer.analyze(null, null);

            assertEquals(Sentiment.NEUTRAL, result.sentiment());
            assertEquals(0.0, result.score());
        }

        @Test
        @DisplayName("Should return neutral with 0.0 score when title and summary are both empty")
        void shouldReturnNeutralWhenBothEmpty() {
            KeywordSentimentAnalyzer.SentimentResult result = analyzer.analyze("", "");

            assertEquals(Sentiment.NEUTRAL, result.sentiment());
            assertEquals(0.0, result.score());
        }

        @Test
        @DisplayName("Should return neutral with 0.0 score when title and summary are blank")
        void shouldReturnNeutralWhenBothBlank() {
            KeywordSentimentAnalyzer.SentimentResult result = analyzer.analyze("   ", "   ");

            assertEquals(Sentiment.NEUTRAL, result.sentiment());
            assertEquals(0.0, result.score());
        }

        @Test
        @DisplayName("Should give title double weight compared to summary")
        void shouldGiveTitleDoubleWeight() {
            // Title has 1 positive keyword (2x weight = 2 positive points)
            // Summary has 1 negative keyword (1x weight = 1 negative point)
            // Net: 2 positive - 1 negative = 1, total = 3, score = 1/3 = ~0.33 (positive)
            KeywordSentimentAnalyzer.SentimentResult result = analyzer.analyze(
                    "Stock surges today",
                    "Some analysts warn about overvaluation concerns."
            );

            assertEquals(Sentiment.POSITIVE, result.sentiment());
            assertTrue(result.score() > 0.0, "Title weighting should make overall sentiment positive");
        }
    }

    @Nested
    @DisplayName("classifySentiment")
    class ClassifySentiment {

        @Test
        @DisplayName("Should return POSITIVE when score is above 0.1 threshold")
        void shouldReturnPositiveAboveThreshold() {
            assertEquals(Sentiment.POSITIVE, analyzer.classifySentiment(0.5));
        }

        @Test
        @DisplayName("Should return NEGATIVE when score is below -0.1 threshold")
        void shouldReturnNegativeBelowThreshold() {
            assertEquals(Sentiment.NEGATIVE, analyzer.classifySentiment(-0.5));
        }

        @Test
        @DisplayName("Should return NEUTRAL when score is exactly 0.0")
        void shouldReturnNeutralAtZero() {
            assertEquals(Sentiment.NEUTRAL, analyzer.classifySentiment(0.0));
        }

        @Test
        @DisplayName("Should return NEUTRAL when score is exactly at positive threshold boundary (0.1)")
        void shouldReturnNeutralAtPositiveThresholdBoundary() {
            assertEquals(Sentiment.NEUTRAL, analyzer.classifySentiment(0.1));
        }

        @Test
        @DisplayName("Should return NEUTRAL when score is exactly at negative threshold boundary (-0.1)")
        void shouldReturnNeutralAtNegativeThresholdBoundary() {
            assertEquals(Sentiment.NEUTRAL, analyzer.classifySentiment(-0.1));
        }

        @Test
        @DisplayName("Should return POSITIVE when score is just above threshold")
        void shouldReturnPositiveJustAboveThreshold() {
            assertEquals(Sentiment.POSITIVE, analyzer.classifySentiment(0.11));
        }

        @Test
        @DisplayName("Should return NEGATIVE when score is just below negative threshold")
        void shouldReturnNegativeJustBelowThreshold() {
            assertEquals(Sentiment.NEGATIVE, analyzer.classifySentiment(-0.11));
        }

        @Test
        @DisplayName("Should return POSITIVE for maximum score of 1.0")
        void shouldReturnPositiveForMaxScore() {
            assertEquals(Sentiment.POSITIVE, analyzer.classifySentiment(1.0));
        }

        @Test
        @DisplayName("Should return NEGATIVE for minimum score of -1.0")
        void shouldReturnNegativeForMinScore() {
            assertEquals(Sentiment.NEGATIVE, analyzer.classifySentiment(-1.0));
        }
    }

    @Nested
    @DisplayName("analyzeSentimentScore")
    class AnalyzeSentimentScore {

        @Test
        @DisplayName("Should return score between -1.0 and 1.0 for mixed text")
        void shouldReturnScoreInValidRange() {
            double score = analyzer.analyzeSentimentScore(
                    "Stock rally continues despite risks",
                    "Growth and gains offset by concerns about debt and inflation."
            );

            assertTrue(score >= -1.0 && score <= 1.0, "Score should be in [-1.0, 1.0] range");
        }

        @Test
        @DisplayName("Should return 0.0 when no keywords match")
        void shouldReturnZeroWhenNoKeywordsMatch() {
            double score = analyzer.analyzeSentimentScore(
                    "Company announces new office",
                    "The building is located downtown."
            );

            assertEquals(0.0, score);
        }

        @Test
        @DisplayName("Should return positive score for purely positive text")
        void shouldReturnPositiveScoreForPositiveText() {
            double score = analyzer.analyzeSentimentScore(
                    "Earnings beat expectations",
                    "Revenue growth and strong profits."
            );

            assertTrue(score > 0.0, "Score should be positive for positive-only text");
        }

        @Test
        @DisplayName("Should return negative score for purely negative text")
        void shouldReturnNegativeScoreForNegativeText() {
            double score = analyzer.analyzeSentimentScore(
                    "Stock crashes on fraud scandal",
                    "Losses mount amid bankruptcy fears."
            );

            assertTrue(score < 0.0, "Score should be negative for negative-only text");
        }

        @Test
        @DisplayName("Should handle null title with non-null summary")
        void shouldHandleNullTitleWithSummary() {
            double score = analyzer.analyzeSentimentScore(null, "Stock surges on strong earnings");

            assertTrue(score > 0.0, "Score should be positive from summary keywords");
        }

        @Test
        @DisplayName("Should handle non-null title with null summary")
        void shouldHandleNonNullTitleWithNullSummary() {
            double score = analyzer.analyzeSentimentScore("Massive losses reported", null);

            assertTrue(score < 0.0, "Score should be negative from title keywords");
        }
    }
}
