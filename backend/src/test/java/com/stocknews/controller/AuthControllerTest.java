package com.stocknews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocknews.dto.*;
import com.stocknews.exception.DuplicateResourceException;
import com.stocknews.security.JwtAuthenticationFilter;
import com.stocknews.security.JwtService;
import com.stocknews.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for {@link AuthController}.
 * Uses MockMvc to test HTTP routing, status codes, validation, and response serialization.
 * Security filters are disabled for most tests; the /me endpoint tests manually configure
 * the SecurityContext to simulate authentication.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ObjectMapper objectMapper;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "securePassword123";
    private static final String DISPLAY_NAME = "Test User";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 on success")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, DISPLAY_NAME);
        AuthResponse response = buildAuthResponse();

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.email").value(EMAIL));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 for invalid email")
    void register_invalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("not-an-email", PASSWORD, DISPLAY_NAME);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 for short password")
    void register_shortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest(EMAIL, "short", DISPLAY_NAME);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 409 for duplicate email")
    void register_duplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, DISPLAY_NAME);

        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("An account with this email already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 on success")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        AuthResponse response = buildAuthResponse();

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.userId").value(USER_ID));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 401 for invalid credentials")
    void login_invalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(EMAIL, "wrongPassword");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh should return 200 on success")
    void refreshToken_success() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        AuthResponse response = buildAuthResponse();

        when(userService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(userService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return 200 when authenticated")
    void me_authenticated() throws Exception {
        // Manually set the SecurityContext with userId as principal (same as JwtAuthenticationFilter)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(USER_ID, EMAIL, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserResponse userResponse = UserResponse.builder()
                .id(USER_ID)
                .email(EMAIL)
                .displayName(DISPLAY_NAME)
                .createdAt(OffsetDateTime.now())
                .build();

        when(userService.getCurrentUser(USER_ID)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.displayName").value(DISPLAY_NAME));

        verify(userService).getCurrentUser(USER_ID);
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return 401 when not authenticated")
    void me_unauthenticated() throws Exception {
        // No SecurityContext set — AuthenticatedUser.getUserId() throws IllegalStateException,
        // which GlobalExceptionHandler maps to 401 Unauthorized
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("No authenticated user found in SecurityContext"));
    }

    /**
     * Builds a standard AuthResponse for test assertions.
     *
     * @return a populated AuthResponse
     */
    private AuthResponse buildAuthResponse() {
        return AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .userId(USER_ID)
                .email(EMAIL)
                .displayName(DISPLAY_NAME)
                .build();
    }
}
