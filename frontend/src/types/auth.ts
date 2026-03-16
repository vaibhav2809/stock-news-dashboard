/** Request body for user registration. */
export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

/** Request body for user login. */
export interface LoginRequest {
  email: string;
  password: string;
}

/** Request body for token refresh. */
export interface RefreshTokenRequest {
  refreshToken: string;
}

/** Response from login, register, and refresh endpoints. */
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  email: string;
  displayName: string;
}

/** Authenticated user profile from GET /auth/me. */
export interface UserProfile {
  id: number;
  email: string;
  displayName: string;
  createdAt: string;
}
