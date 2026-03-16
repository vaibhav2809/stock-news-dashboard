import axios from 'axios';
import type { ApiErrorResponse } from '@/types';
import type { AuthResponse } from '@/types/auth';

/**
 * Base URL for all API requests.
 * In development, Vite's proxy forwards /api to localhost:8080.
 * In production, this should be set via VITE_API_BASE_URL env var.
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

/**
 * Pre-configured Axios instance for all backend API calls.
 * Features:
 * - Base URL automatically prepended
 * - JSON content type by default
 * - Request interceptor attaches JWT access token
 * - Response interceptor handles 401 with automatic token refresh
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
  timeout: 15000,
});

/** localStorage key matching the Zustand persist config in authStore. */
const AUTH_STORAGE_KEY = 'stocknews-auth';

/**
 * Reads the auth store from localStorage without importing the Zustand store directly.
 * This avoids circular dependency issues (apiClient <-> authStore).
 */
function getAuthFromStorage(): { accessToken: string | null; refreshToken: string | null } {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) return { accessToken: null, refreshToken: null };
    const parsed = JSON.parse(raw);
    return {
      accessToken: parsed?.state?.accessToken ?? null,
      refreshToken: parsed?.state?.refreshToken ?? null,
    };
  } catch {
    return { accessToken: null, refreshToken: null };
  }
}

/** Updates tokens in localStorage after a successful refresh. */
function updateAuthInStorage(accessToken: string, refreshToken: string): void {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) return;
    const parsed = JSON.parse(raw);
    parsed.state.accessToken = accessToken;
    parsed.state.refreshToken = refreshToken;
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(parsed));
  } catch {
    // Storage update failed — user will need to re-login
  }
}

/** Clears auth state from localStorage (forces re-login). */
function clearAuthFromStorage(): void {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) return;
    const parsed = JSON.parse(raw);
    parsed.state = {
      accessToken: null,
      refreshToken: null,
      userId: null,
      email: null,
      displayName: null,
      isAuthenticated: false,
    };
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(parsed));
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY);
  }
}

/** Tracks whether a token refresh is already in progress. */
let isRefreshing = false;

/** Queue of requests waiting for the token refresh to complete. */
let refreshSubscribers: Array<(token: string) => void> = [];

/** Notifies all queued requests with the new access token. */
function onRefreshComplete(newToken: string): void {
  refreshSubscribers.forEach((callback) => callback(newToken));
  refreshSubscribers = [];
}

/**
 * Request interceptor: attaches the JWT access token to every outgoing request.
 * Skips auth endpoints (login, register, refresh) to avoid circular token attachment.
 */
apiClient.interceptors.request.use((config) => {
  const { accessToken } = getAuthFromStorage();
  if (accessToken && config.url && !config.url.includes('/auth/')) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

/**
 * Response interceptor that:
 * 1. Automatically refreshes the access token on 401 responses
 * 2. Retries the original request with the new token
 * 3. Redirects to login if refresh also fails
 * 4. Extracts meaningful error messages for non-401 errors
 */
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 with automatic token refresh (skip for auth endpoints)
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._isRetry &&
      !originalRequest.url?.includes('/auth/')
    ) {
      originalRequest._isRetry = true;

      if (isRefreshing) {
        // Another request is already refreshing — queue this one
        return new Promise((resolve) => {
          refreshSubscribers.push((newToken: string) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            resolve(apiClient(originalRequest));
          });
        });
      }

      isRefreshing = true;
      const { refreshToken } = getAuthFromStorage();

      if (refreshToken) {
        try {
          const { data } = await axios.post<AuthResponse>(
            `${API_BASE_URL}/auth/refresh`,
            { refreshToken },
            { headers: { 'Content-Type': 'application/json' } },
          );

          updateAuthInStorage(data.accessToken, data.refreshToken);
          onRefreshComplete(data.accessToken);
          isRefreshing = false;

          originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
          return apiClient(originalRequest);
        } catch {
          clearAuthFromStorage();
          isRefreshing = false;
          refreshSubscribers = [];
          window.location.href = '/login';
          return Promise.reject(error);
        }
      } else {
        clearAuthFromStorage();
        isRefreshing = false;
        window.location.href = '/login';
        return Promise.reject(error);
      }
    }

    // Standard error handling for non-401 errors
    if (axios.isAxiosError(error) && error.response) {
      const apiError = error.response.data as ApiErrorResponse;
      const enhancedError = new Error(apiError.message || 'An unexpected error occurred');
      (enhancedError as Error & { status: number }).status = error.response.status;
      (enhancedError as Error & { apiError: ApiErrorResponse }).apiError = apiError;
      return Promise.reject(enhancedError);
    }
    return Promise.reject(error);
  },
);

export default apiClient;
