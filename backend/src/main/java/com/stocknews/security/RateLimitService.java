package com.stocknews.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Redis-based rate limiting service that tracks API usage per user per day.
 * Protects free-tier external API quotas (Finnhub: 60 calls/min, NewsData.io: limited daily).
 * Each user gets a configurable daily request limit for news search operations.
 * Anonymous (unauthenticated) users share a single "anonymous" bucket with a lower limit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")
public class RateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    /** Maximum news search requests per authenticated user per day. */
    @Value("${app.rate-limit.daily-per-user:100}")
    private int dailyLimitPerUser;

    /** Maximum news search requests for anonymous users per day (shared bucket). */
    @Value("${app.rate-limit.daily-anonymous:30}")
    private int dailyLimitAnonymous;

    /** Redis key prefix for rate limit counters. */
    private static final String KEY_PREFIX = "ratelimit:";

    /**
     * Checks if a user has remaining API quota for today and increments the counter.
     * Returns true if the request is allowed, false if the limit is exceeded.
     *
     * @param userId the authenticated user's ID, or null for anonymous users
     * @return true if the request is within the daily limit
     */
    public boolean tryConsume(Long userId) {
        final String key = buildKey(userId);
        final int limit = userId != null ? dailyLimitPerUser : dailyLimitAnonymous;

        final Long currentCount = stringRedisTemplate.opsForValue().increment(key);

        if (currentCount != null && currentCount == 1L) {
            // First request today — set expiry to end of day (or 24h for simplicity)
            stringRedisTemplate.expire(key, Duration.ofHours(24));
        }

        if (currentCount != null && currentCount > limit) {
            log.warn("Rate limit exceeded: userId={}, count={}, limit={}", userId, currentCount, limit);
            return false;
        }

        return true;
    }

    /**
     * Returns the number of requests remaining for today.
     *
     * @param userId the authenticated user's ID, or null for anonymous users
     * @return remaining requests
     */
    public int getRemainingRequests(Long userId) {
        final String key = buildKey(userId);
        final int limit = userId != null ? dailyLimitPerUser : dailyLimitAnonymous;
        final String value = stringRedisTemplate.opsForValue().get(key);

        if (value == null) {
            return limit;
        }

        final int used = Integer.parseInt(value);
        return Math.max(0, limit - used);
    }

    /**
     * Builds the Redis key for a user's daily rate limit counter.
     * Format: ratelimit:{userId}:{date} or ratelimit:anon:{date}
     *
     * @param userId the user ID or null for anonymous
     * @return the Redis key
     */
    private String buildKey(Long userId) {
        final String dateStr = LocalDate.now().toString();
        final String userPart = userId != null ? userId.toString() : "anon";
        return KEY_PREFIX + userPart + ":" + dateStr;
    }
}
