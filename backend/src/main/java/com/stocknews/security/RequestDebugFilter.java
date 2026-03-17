package com.stocknews.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Temporary diagnostic filter that logs request method, URI, and security context
 * to help debug why POST requests are being rejected by Spring Security.
 * This filter runs early in the chain and logs before and after the chain processes.
 */
@Slf4j
@Component
public class RequestDebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String method = request.getMethod();
        final String uri = request.getRequestURI();
        final String contextPath = request.getContextPath();
        final String servletPath = request.getServletPath();
        final String pathInfo = request.getPathInfo();
        final String authHeader = request.getHeader("Authorization");
        final boolean hasAuth = authHeader != null;

        log.info("DEBUG-FILTER [BEFORE] method={}, uri={}, contextPath={}, servletPath={}, pathInfo={}, hasAuth={}",
                method, uri, contextPath, servletPath, pathInfo, hasAuth);

        filterChain.doFilter(request, response);

        log.info("DEBUG-FILTER [AFTER] method={}, uri={}, status={}",
                method, uri, response.getStatus());
    }
}
