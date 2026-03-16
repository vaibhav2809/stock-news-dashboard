package com.stocknews.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for extracting the authenticated user's ID from the SecurityContext.
 * Used by controllers to replace the hardcoded DEFAULT_USER_ID.
 */
public final class AuthenticatedUser {

    private AuthenticatedUser() {
        // Utility class — prevent instantiation
    }

    /**
     * Extracts the current authenticated user's ID from the SecurityContext.
     * The userId is stored as the principal by JwtAuthenticationFilter.
     *
     * @return the authenticated user's database ID
     * @throws IllegalStateException if no authentication is present
     */
    public static Long getUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        return (Long) authentication.getPrincipal();
    }
}
