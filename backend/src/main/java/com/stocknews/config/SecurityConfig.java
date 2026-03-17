package com.stocknews.config;

import com.stocknews.security.JwtAuthenticationFilter;
import com.stocknews.security.RequestDebugFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CorsFilter;

/**
 * Spring Security configuration with JWT-based stateless authentication.
 * Public endpoints: auth routes, news, symbols, sentiment, health, Swagger docs.
 * Protected endpoints: watchlist, alerts, spaces, user profile (require valid access token).
 * Returns 401 (not 403) for unauthenticated requests so the frontend can auto-refresh tokens.
 *
 * CORS is NOT handled by Spring Security (.cors() is intentionally omitted).
 * Instead, a standalone CorsFilter bean from CorsConfig runs BEFORE the security
 * filter chain via addFilterBefore. This avoids a known interaction where Spring
 * Security's CORS integration interferes with POST request authorization.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsFilter corsFilter;
    private final RequestDebugFilter requestDebugFilter;

    /**
     * Configures the security filter chain with JWT authentication.
     * CorsFilter is added before the JWT filter so CORS preflight (OPTIONS)
     * is handled before any security checks. Spring Security's .cors() is
     * deliberately NOT used due to a confirmed interaction issue where it
     * blocks POST requests to permitAll endpoints.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}}"
                            );
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Preflight OPTIONS requests — always permit
                        .requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.name())).permitAll()

                        // Auth endpoints — always public (register, login, refresh)
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/**")).permitAll()

                        // Symbol search (autocomplete) — public
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/symbols/**", HttpMethod.GET.name())).permitAll()

                        // News endpoints — all public (GET search + POST fetch)
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/news/**")).permitAll()

                        // Sentiment analysis — public (read-only)
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/sentiment/**", HttpMethod.GET.name())).permitAll()

                        // Health/version check — public
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/health/**")).permitAll()

                        // Admin endpoints — public for now (import triggers)
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/admin/**")).permitAll()

                        // Swagger / OpenAPI docs — public
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api-docs/**")).permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                // Debug filter runs first, then CorsFilter, then JWT filter
                .addFilterBefore(requestDebugFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder for hashing user passwords.
     *
     * @return the password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
