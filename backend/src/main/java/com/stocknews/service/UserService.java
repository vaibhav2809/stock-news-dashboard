package com.stocknews.service;

import com.stocknews.dto.*;
import com.stocknews.exception.DuplicateResourceException;
import com.stocknews.model.entity.User;
import com.stocknews.repository.UserRepository;
import com.stocknews.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user authentication and registration.
 * Handles password hashing, credential validation, and JWT token generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new user account.
     * Hashes the password with BCrypt and generates JWT tokens.
     *
     * @param request the registration details (email, password, displayName)
     * @return authentication response with tokens and user info
     * @throws DuplicateResourceException if the email is already registered
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        final String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: email already exists email={}", email);
            throw new DuplicateResourceException("An account with this email already exists");
        }

        final User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName().trim())
                .build();

        final User savedUser = userRepository.save(user);
        log.info("User registered successfully userId={}, email={}", savedUser.getId(), email);

        return buildAuthResponse(savedUser);
    }

    /**
     * Authenticates a user with email and password.
     * Validates credentials and generates JWT tokens on success.
     *
     * @param request the login credentials (email, password)
     * @return authentication response with tokens and user info
     * @throws BadCredentialsException if the email or password is incorrect
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        final String email = request.getEmail().toLowerCase().trim();

        final User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: email not found email={}", email);
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: incorrect password email={}", email);
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("User logged in successfully userId={}, email={}", user.getId(), email);
        return buildAuthResponse(user);
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     * Validates the refresh token and issues new access + refresh tokens.
     *
     * @param request the refresh token
     * @return new authentication response with fresh tokens
     * @throws BadCredentialsException if the refresh token is invalid or expired
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        final String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BadCredentialsException("Invalid token type — expected refresh token");
        }

        final Long userId = jwtService.extractUserId(refreshToken);
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User no longer exists"));

        log.info("Token refreshed for userId={}", userId);
        return buildAuthResponse(user);
    }

    /**
     * Retrieves the current user's profile by ID.
     *
     * @param userId the authenticated user's ID
     * @return the user profile response
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return UserResponse.fromEntity(user);
    }

    /**
     * Builds the full authentication response with access and refresh tokens.
     *
     * @param user the authenticated user entity
     * @return the auth response DTO
     */
    private AuthResponse buildAuthResponse(User user) {
        final String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getDisplayName());
        final String refreshToken = jwtService.generateRefreshToken(
                user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }
}
