import apiClient from './apiClient';
import type {
  RegisterRequest,
  LoginRequest,
  RefreshTokenRequest,
  AuthResponse,
  UserProfile,
} from '@/types/auth';

/**
 * Registers a new user account.
 * @param request - registration details (email, password, displayName)
 * @returns auth response with JWT tokens and user info
 */
export async function register(request: RegisterRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/register', request);
  return data;
}

/**
 * Authenticates a user with email and password.
 * @param request - login credentials
 * @returns auth response with JWT tokens and user info
 */
export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/login', request);
  return data;
}

/**
 * Refreshes an expired access token using a valid refresh token.
 * @param request - the refresh token
 * @returns new auth response with fresh tokens
 */
export async function refreshToken(request: RefreshTokenRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/refresh', request);
  return data;
}

/**
 * Retrieves the current authenticated user's profile.
 * Requires a valid access token in the Authorization header.
 * @returns the user profile
 */
export async function getCurrentUser(): Promise<UserProfile> {
  const { data } = await apiClient.get<UserProfile>('/auth/me');
  return data;
}
