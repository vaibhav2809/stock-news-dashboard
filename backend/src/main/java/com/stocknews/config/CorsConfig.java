package com.stocknews.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configures Cross-Origin Resource Sharing (CORS) for the API.
 * Exposes a CorsConfigurationSource bean that Spring Security integrates with
 * via SecurityConfig's .cors() directive.
 * Allowed origins are read from the CORS_ALLOWED_ORIGINS environment variable,
 * defaulting to localhost:5173 for local development.
 * When set to "*", uses allowedOriginPatterns to support credentials.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    /**
     * Creates a CORS configuration source that applies to /api/** and /ws/** paths.
     * Spring Security's .cors() integration automatically discovers this bean
     * and applies it to the security filter chain.
     * Uses allowedOriginPatterns when wildcard "*" is specified,
     * since Spring does not allow allowCredentials with allowedOrigins("*").
     *
     * @return configured CorsConfigurationSource bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
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
        return source;
    }
}
