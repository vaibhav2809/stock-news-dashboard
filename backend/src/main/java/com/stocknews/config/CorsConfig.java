package com.stocknews.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configures Cross-Origin Resource Sharing (CORS) for the API.
 * Creates a standalone CorsFilter bean that is injected into SecurityConfig
 * and added to the Spring Security filter chain via addFilterBefore.
 *
 * Spring Security's .cors() integration is intentionally NOT used because
 * it has a confirmed interaction issue where it blocks POST requests to
 * endpoints configured as permitAll in the authorization rules.
 *
 * Allowed origins are read from the CORS_ALLOWED_ORIGINS environment variable,
 * defaulting to localhost:5173 for local development.
 * When set to "*", uses allowedOriginPatterns to support credentials.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    /**
     * Creates a CorsFilter bean applied to all paths (/**).
     * This filter is injected into SecurityConfig and runs within the
     * Spring Security filter chain before the JWT authentication filter.
     * Uses allowedOriginPatterns when wildcard "*" is specified,
     * since Spring does not allow allowCredentials with allowedOrigins("*").
     *
     * @return configured CorsFilter bean
     */
    @Bean
    public CorsFilter corsFilter() {
        final CorsConfiguration config = new CorsConfiguration();

        if ("*".equals(allowedOrigins.trim())) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
