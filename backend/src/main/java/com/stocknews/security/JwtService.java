package com.stocknews.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service for JWT token generation and validation.
 * Uses HMAC-SHA256 for signing. Access tokens expire after the configured duration;
 * refresh tokens have a longer expiration for silent re-authentication.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    /**
     * Constructs the JWT service with configuration from application properties.
     *
     * @param secret              the HMAC secret (min 32 chars for HS256)
     * @param accessExpirationMs  access token TTL in milliseconds
     * @param refreshExpirationMs refresh token TTL in milliseconds
     */
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessExpirationMs;
        this.refreshTokenExpirationMs = refreshExpirationMs;
    }

    /**
     * Generates a short-lived access token for the given user.
     *
     * @param userId      the user's database ID
     * @param email       the user's email (stored as subject)
     * @param displayName the user's display name (stored as claim)
     * @return signed JWT access token
     */
    public String generateAccessToken(Long userId, String email, String displayName) {
        final Date now = new Date();
        final Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("displayName", displayName)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generates a long-lived refresh token for silent re-authentication.
     *
     * @param userId the user's database ID
     * @param email  the user's email (stored as subject)
     * @return signed JWT refresh token
     */
    public String generateRefreshToken(Long userId, String email) {
        final Date now = new Date();
        final Date expiry = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the user ID from a valid JWT token.
     *
     * @param token the JWT token string
     * @return the user ID from the "userId" claim
     */
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    /**
     * Extracts the email (subject) from a valid JWT token.
     *
     * @param token the JWT token string
     * @return the email address
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extracts the token type ("access" or "refresh") from a valid JWT token.
     *
     * @param token the JWT token string
     * @return the token type
     */
    public String extractTokenType(String token) {
        return extractClaims(token).get("type", String.class);
    }

    /**
     * Validates a JWT token by verifying its signature and checking expiration.
     *
     * @param token the JWT token string
     * @return true if the token is valid and not expired
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses and validates the JWT token, returning its claims.
     *
     * @param token the JWT token string
     * @return the token claims
     * @throws JwtException if the token is invalid or expired
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
