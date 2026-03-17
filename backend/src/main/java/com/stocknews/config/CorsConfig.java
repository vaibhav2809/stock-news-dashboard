package com.stocknews.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configures Cross-Origin Resource Sharing (CORS) for the API.
 * Registers a CorsFilter at highest precedence so it runs BEFORE Spring Security.
 * This ensures preflight OPTIONS requests are handled correctly and POST requests
 * with Content-Type: application/json are not blocked.
 * Allowed origins are read from the CORS_ALLOWED_ORIGINS environment variable,
 * defaulting to localhost:5173 for local development.
 * When set to "*", uses allowedOriginPatterns to support credentials.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    /**
     * Creates a CORS filter that runs at highest precedence (before Spring Security).
     * Applies to /api/** and /ws/** paths.
     * Uses allowedOriginPatterns when wildcard "*" is specified,
     * since Spring does not allow allowCredentials with allowedOrigins("*").
     *
     * @return configured CorsFilter bean with highest order priority
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        final CorsConfiguration config = new CorsConfiguration();

        if ("*".equals(allowedOrigins.trim())) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/ws/**", config);
        return new CorsFilter(source);
    }
}
