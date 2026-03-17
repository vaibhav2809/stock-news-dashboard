package com.stocknews.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * HTTP filter that enforces per-user daily rate limits on API endpoints.
 * Runs after the JWT authentication filter so the user ID is available.
 * Applies to news search, trending, and fetch endpoints to protect external API quotas.
 * Returns HTTP 429 (Too Many Requests) with remaining quota info when the limit is exceeded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
@Profile({"dev"})
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!shouldRateLimit(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final Long userId = extractUserId();

        if (!rateLimitService.tryConsume(userId)) {
            final int remaining = rateLimitService.getRemainingRequests(userId);
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

            final Map<String, Object> errorBody = Map.of(
                    "timestamp", OffsetDateTime.now().toString(),
                    "status", 429,
                    "error", "Too Many Requests",
                    "message", "Daily API limit exceeded. Please try again tomorrow.",
                    "remaining", remaining
            );
            objectMapper.writeValue(response.getOutputStream(), errorBody);
            return;
        }

        // Add remaining count to response headers for client awareness
        final int remaining = rateLimitService.getRemainingRequests(userId);
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        filterChain.doFilter(request, response);
    }

    /**
     * Determines whether the request should be rate-limited.
     * Only applies to endpoints that trigger external API calls.
     *
     * @param request the HTTP request
     * @return true if rate limiting should be applied
     */
    private boolean shouldRateLimit(HttpServletRequest request) {
        final String path = request.getRequestURI();
        final String method = request.getMethod();

        // Rate-limit news search, trending, and manual fetch
        if (path.startsWith("/api/v1/news") && "GET".equals(method)) {
            return true;
        }
        if (path.equals("/api/v1/news/fetch") && "POST".equals(method)) {
            return true;
        }
        // Rate-limit sentiment endpoints (they query the DB which was populated by API calls)
        if (path.startsWith("/api/v1/sentiment") && "GET".equals(method)) {
            return true;
        }

        return false;
    }

    /**
     * Extracts the authenticated user's ID from the SecurityContext.
     * Returns null for unauthenticated (anonymous) users.
     *
     * @return the user ID or null
     */
    private Long extractUserId() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
