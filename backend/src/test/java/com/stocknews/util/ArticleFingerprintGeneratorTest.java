package com.stocknews.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ArticleFingerprintGenerator.
 * Verifies deduplication fingerprinting behavior.
 */
class ArticleFingerprintGeneratorTest {

    @Test
    @DisplayName("Should generate consistent fingerprint for same inputs")
    void shouldGenerateConsistentFingerprint() {
        String fingerprint1 = ArticleFingerprintGenerator.generate("Apple stock rises", "https://example.com/article1");
        String fingerprint2 = ArticleFingerprintGenerator.generate("Apple stock rises", "https://example.com/article1");

        assertEquals(fingerprint1, fingerprint2);
    }

    @Test
    @DisplayName("Should generate different fingerprints for different titles")
    void shouldGenerateDifferentFingerprintsForDifferentTitles() {
        String fingerprint1 = ArticleFingerprintGenerator.generate("Apple stock rises", "https://example.com/article1");
        String fingerprint2 = ArticleFingerprintGenerator.generate("Apple stock falls", "https://example.com/article1");

        assertNotEquals(fingerprint1, fingerprint2);
    }

    @Test
    @DisplayName("Should generate different fingerprints for different URLs")
    void shouldGenerateDifferentFingerprintsForDifferentUrls() {
        String fingerprint1 = ArticleFingerprintGenerator.generate("Apple stock rises", "https://example.com/article1");
        String fingerprint2 = ArticleFingerprintGenerator.generate("Apple stock rises", "https://example.com/article2");

        assertNotEquals(fingerprint1, fingerprint2);
    }

    @Test
    @DisplayName("Should be case-insensitive for deduplication")
    void shouldBeCaseInsensitive() {
        String fingerprint1 = ArticleFingerprintGenerator.generate("Apple Stock RISES", "https://example.com/Article1");
        String fingerprint2 = ArticleFingerprintGenerator.generate("apple stock rises", "https://example.com/article1");

        assertEquals(fingerprint1, fingerprint2);
    }

    @Test
    @DisplayName("Should trim whitespace before hashing")
    void shouldTrimWhitespace() {
        String fingerprint1 = ArticleFingerprintGenerator.generate("  Apple stock rises  ", "  https://example.com/article1  ");
        String fingerprint2 = ArticleFingerprintGenerator.generate("Apple stock rises", "https://example.com/article1");

        assertEquals(fingerprint1, fingerprint2);
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValues() {
        assertDoesNotThrow(() -> ArticleFingerprintGenerator.generate(null, "https://example.com"));
        assertDoesNotThrow(() -> ArticleFingerprintGenerator.generate("Title", null));
        assertDoesNotThrow(() -> ArticleFingerprintGenerator.generate(null, null));
    }

    @Test
    @DisplayName("Should return 64-character hex string (SHA-256)")
    void shouldReturn64CharHexString() {
        String fingerprint = ArticleFingerprintGenerator.generate("Test title", "https://example.com");

        assertEquals(64, fingerprint.length());
        assertTrue(fingerprint.matches("[0-9a-f]{64}"));
    }
}
