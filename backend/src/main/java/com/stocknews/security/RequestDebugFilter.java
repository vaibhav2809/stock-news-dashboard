package com.stocknews.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Temporary diagnostic filter that adds debug headers to every response.
 * Headers include the request method, URI, servlet path, and whether the
 * AntPathRequestMatcher for /api/v1/auth/** matches the request.
 * Check X-Debug-* response headers in curl -v output.
 */
@Slf4j
@Component
public class RequestDebugFilter extends OncePerRequestFilter {

    private static final AntPathRequestMatcher AUTH_MATCHER =
            new AntPathRequestMatcher("/api/v1/auth/**");

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String method = request.getMethod();
        final String uri = request.getRequestURI();
        final String servletPath = request.getServletPath();
        final boolean matchesAuth = AUTH_MATCHER.matches(request);

        // Add debug headers to the response so we can see them in curl -v
        response.setHeader("X-Debug-Method", method);
        response.setHeader("X-Debug-URI", uri);
        response.setHeader("X-Debug-ServletPath", servletPath);
        response.setHeader("X-Debug-AuthMatch", String.valueOf(matchesAuth));
        response.setHeader("X-Debug-ContextPath", request.getContextPath());

        log.info("DEBUG method={}, uri={}, servletPath={}, authMatch={}", method, uri, servletPath, matchesAuth);

        filterChain.doFilter(request, response);
    }
}
