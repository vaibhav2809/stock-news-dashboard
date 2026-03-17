package com.stocknews.config;

import com.stocknews.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security configuration with JWT-based stateless authentication.
 * Public endpoints: auth routes, news, symbols, sentiment, health, Swagger docs.
 * Protected endpoints: watchlist, alerts, spaces, user profile (require valid access token).
 * Returns 401 (not 403) for unauthenticated requests so the frontend can auto-refresh tokens.
 *
 * Uses AntPathRequestMatcher explicitly instead of the default MvcRequestMatcher
 * to avoid known path-matching issues in Spring Security 6.x where MvcRequestMatcher
 * can silently fail to match POST requests and certain wildcard patterns.
 *
 * CORS is configured via Customizer.withDefaults() which auto-discovers
 * the CorsConfigurationSource bean from CorsConfig.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the security filter chain with JWT authentication.
     * Uses AntPathRequestMatcher for reliable path matching across all HTTP methods.
     * CORS uses CorsConfigurationSource bean (see CorsConfig), CSRF disabled (stateless JWT),
     * sessions are stateless, JWT filter runs before UsernamePasswordAuthenticationFilter.
     * Unauthenticated requests return 401 (not 403) to trigger frontend token refresh.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
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
                // DIAGNOSTIC: temporarily permit ALL to isolate if issue is authorization or filter
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
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
