package com.stocknews.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Generates a SHA-256 fingerprint for news articles to prevent duplicates.
 * Two articles with the same title and source URL are considered duplicates.
 */
public final class ArticleFingerprintGenerator {

    private ArticleFingerprintGenerator() {
        // Utility class — no instantiation
    }

    /**
     * Generates a SHA-256 hash from the article title and source URL.
     * @param title the article title
     * @param sourceUrl the article source URL
     * @return 64-character hex string representing the fingerprint
     * @throws IllegalStateException if SHA-256 is not available (should never happen)
     */
    public static String generate(String title, String sourceUrl) {
        final String input = normalizeForHashing(title) + "|" + normalizeForHashing(sourceUrl);
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Normalizes a string for consistent hashing: trims whitespace and lowercases.
     * @param value the input string
     * @return normalized string, or empty string if null
     */
    private static String normalizeForHashing(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }
}
