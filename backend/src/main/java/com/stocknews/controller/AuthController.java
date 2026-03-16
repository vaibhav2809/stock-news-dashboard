package com.stocknews.controller;

import com.stocknews.dto.*;
import com.stocknews.security.AuthenticatedUser;
import com.stocknews.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Handles user registration, login, token refresh, and profile retrieval.
 * Registration and login are public; profile retrieval requires authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and token management")
public class AuthController {

    private final UserService userService;

    /**
     * Registers a new user account.
     *
     * @param request the registration details (email, password, displayName)
     * @return JWT tokens and user info
     */
    @PostMapping("/register")
    @Operation(summary = "Register", description = "Create a new user account and receive JWT tokens")
    @ApiResponse(responseCode = "201", description = "Account created successfully")
    @ApiResponse(responseCode = "409", description = "Email already registered")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register — email={}", request.getEmail());
        final AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param request the login credentials
     * @return JWT tokens and user info
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email and password to receive JWT tokens")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login — email={}", request.getEmail());
        final AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * @param request the refresh token
     * @return new JWT tokens and user info
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Exchange a refresh token for new access and refresh tokens")
    @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/v1/auth/refresh");
        final AuthResponse response = userService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the current authenticated user's profile.
     * Requires a valid access token in the Authorization header.
     *
     * @return the user profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve the authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<UserResponse> getCurrentUser() {
        final Long userId = AuthenticatedUser.getUserId();
        log.info("GET /api/v1/auth/me — userId={}", userId);
        final UserResponse response = userService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }
}
