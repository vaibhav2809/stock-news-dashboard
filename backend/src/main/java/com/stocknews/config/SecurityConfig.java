package com.stocknews.config;

import com.stocknews.security.JwtAuthenticationFilter;
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

/**
 * Spring Security configuration with JWT-based stateless authentication.
 * Public endpoints: auth routes, news search, trending, sentiment, symbols, Swagger docs.
 * Protected endpoints: watchlist, alerts, spaces, user profile (require valid access token).
 * Returns 401 (not 403) for unauthenticated requests so the frontend can auto-refresh tokens.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the security filter chain with JWT authentication.
     * CORS is handled by a standalone CorsFilter bean at highest precedence (see CorsConfig).
     * Spring Security's .cors() is intentionally NOT used here to avoid conflicts
     * that block POST requests. CSRF disabled (stateless JWT), sessions are stateless,
     * OPTIONS preflight requests are always permitted,
     * JWT filter runs before UsernamePasswordAuthenticationFilter.
     * Unauthenticated requests return 401 (not 403) to trigger frontend token refresh.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth endpoints — always public
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()

                        // Symbol search (autocomplete) — public
                        .requestMatchers(HttpMethod.GET, "/api/v1/symbols/**").permitAll()

                        // News search and trending — public (read-only)
                        .requestMatchers(HttpMethod.GET, "/api/v1/news/**").permitAll()

                        // News fetch (POST) — public for triggering fetches
                        .requestMatchers(HttpMethod.POST, "/api/v1/news/fetch").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/news/backfill-sentiment").permitAll()

                        // Sentiment analysis — public (read-only)
                        .requestMatchers(HttpMethod.GET, "/api/v1/sentiment/**").permitAll()

                        // Swagger / OpenAPI docs — public
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
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
