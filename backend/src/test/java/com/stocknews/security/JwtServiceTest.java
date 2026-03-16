package com.stocknews.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtService}.
 * Tests token generation, claim extraction, and validation logic
 * using a directly instantiated service (no Spring context needed).
 */
class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-chars-long";
    private static final long ACCESS_EXPIRATION_MS = 3_600_000L;
    private static final long REFRESH_EXPIRATION_MS = 604_800_000L;

    private static final Long USER_ID = 42L;
    private static final String EMAIL = "test@example.com";
    private static final String DISPLAY_NAME = "Test User";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateAccessToken should return a non-null token")
    void generateAccessToken_shouldReturnNonNullToken() {
        String token = jwtService.generateAccessToken(USER_ID, EMAIL, DISPLAY_NAME);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generateRefreshToken should return a non-null token")
    void generateRefreshToken_shouldReturnNonNullToken() {
        String token = jwtService.generateRefreshToken(USER_ID, EMAIL);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("extractUserId should return the correct userId from a token")
    void extractUserId_shouldReturnCorrectUserId() {
        String token = jwtService.generateAccessToken(USER_ID, EMAIL, DISPLAY_NAME);

        Long extractedUserId = jwtService.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("extractEmail should return the correct email from a token")
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateAccessToken(USER_ID, EMAIL, DISPLAY_NAME);

        String extractedEmail = jwtService.extractEmail(token);

        assertThat(extractedEmail).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("extractTokenType should return 'access' for access tokens")
    void extractTokenType_shouldReturnAccessForAccessTokens() {
        String token = jwtService.generateAccessToken(USER_ID, EMAIL, DISPLAY_NAME);

        String tokenType = jwtService.extractTokenType(token);

        assertThat(tokenType).isEqualTo("access");
    }

    @Test
    @DisplayName("extractTokenType should return 'refresh' for refresh tokens")
    void extractTokenType_shouldReturnRefreshForRefreshTokens() {
        String token = jwtService.generateRefreshToken(USER_ID, EMAIL);

        String tokenType = jwtService.extractTokenType(token);

        assertThat(tokenType).isEqualTo("refresh");
    }

    @Test
    @DisplayName("isTokenValid should return true for a valid token")
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateAccessToken(USER_ID, EMAIL, DISPLAY_NAME);

        boolean isValid = jwtService.isTokenValid(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid should return false for an expired token")
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        // Build a token that expired 10 seconds ago using the same signing key
        SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date pastExpiry = new Date(now.getTime() - 10_000L);

        String expiredToken = Jwts.builder()
                .subject(EMAIL)
                .claim("userId", USER_ID)
                .claim("type", "access")
                .issuedAt(new Date(now.getTime() - 20_000L))
                .expiration(pastExpiry)
                .signWith(signingKey)
                .compact();

        boolean isValid = jwtService.isTokenValid(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isTokenValid should return false for a tampered token")
    void isTokenValid_shouldReturnFalseForTamperedToken() {
        String validToken = jwtService.generateAccessToken(USER_ID, EMAIL, DISPLAY_NAME);

        // Tamper with the token by altering the last character of the signature
        String tamperedToken = validToken.substring(0, validToken.length() - 1)
                + (validToken.endsWith("A") ? "B" : "A");

        boolean isValid = jwtService.isTokenValid(tamperedToken);

        assertThat(isValid).isFalse();
    }
}
