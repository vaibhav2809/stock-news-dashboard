package com.stocknews.service;

import com.stocknews.dto.*;
import com.stocknews.exception.DuplicateResourceException;
import com.stocknews.model.entity.User;
import com.stocknews.repository.UserRepository;
import com.stocknews.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserService}.
 * Mocks repository, password encoder, and JWT service to test business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "securePassword123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedpassword";
    private static final String DISPLAY_NAME = "Test User";
    private static final String ACCESS_TOKEN = "access-token-value";
    private static final String REFRESH_TOKEN = "refresh-token-value";

    @Test
    @DisplayName("register should return AuthResponse with tokens on success")
    void register_success() {
        RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, DISPLAY_NAME);
        User savedUser = buildUser();

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(USER_ID, EMAIL, DISPLAY_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(USER_ID, EMAIL)).thenReturn(REFRESH_TOKEN);

        AuthResponse response = userService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getDisplayName()).isEqualTo(DISPLAY_NAME);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register should throw DuplicateResourceException when email already exists")
    void register_duplicateEmail() {
        RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, DISPLAY_NAME);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login should return AuthResponse with tokens on success")
    void login_success() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        User user = buildUser();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(jwtService.generateAccessToken(USER_ID, EMAIL, DISPLAY_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(USER_ID, EMAIL)).thenReturn(REFRESH_TOKEN);

        AuthResponse response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("login should throw BadCredentialsException when email is not found")
    void login_wrongEmail() {
        LoginRequest request = new LoginRequest("unknown@example.com", PASSWORD);

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("login should throw BadCredentialsException when password is incorrect")
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest(EMAIL, "wrongPassword");
        User user = buildUser();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", HASHED_PASSWORD)).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("refreshToken should return new AuthResponse with valid refresh token")
    void refreshToken_success() {
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        User user = buildUser();

        when(jwtService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);
        when(jwtService.extractTokenType(REFRESH_TOKEN)).thenReturn("refresh");
        when(jwtService.extractUserId(REFRESH_TOKEN)).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(USER_ID, EMAIL, DISPLAY_NAME)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(USER_ID, EMAIL)).thenReturn("new-refresh-token");

        AuthResponse response = userService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("refreshToken should throw BadCredentialsException when token is invalid")
    void refreshToken_invalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> userService.refreshToken(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    @DisplayName("refreshToken should throw BadCredentialsException when access token is used as refresh")
    void refreshToken_accessTokenUsedAsRefresh() {
        RefreshTokenRequest request = new RefreshTokenRequest(ACCESS_TOKEN);

        when(jwtService.isTokenValid(ACCESS_TOKEN)).thenReturn(true);
        when(jwtService.extractTokenType(ACCESS_TOKEN)).thenReturn("access");

        assertThatThrownBy(() -> userService.refreshToken(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("expected refresh token");
    }

    @Test
    @DisplayName("getCurrentUser should return UserResponse for valid userId")
    void getCurrentUser_success() {
        User user = buildUser();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUser(USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(USER_ID);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getDisplayName()).isEqualTo(DISPLAY_NAME);
    }

    /**
     * Builds a User entity with standard test values.
     *
     * @return a populated User entity
     */
    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .passwordHash(HASHED_PASSWORD)
                .displayName(DISPLAY_NAME)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
