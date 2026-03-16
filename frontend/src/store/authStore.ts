import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthResponse } from '@/types/auth';

/** Key used for localStorage persistence. */
const AUTH_STORAGE_KEY = 'stocknews-auth';

/** Shape of the auth state managed by Zustand. */
interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  userId: number | null;
  email: string | null;
  displayName: string | null;
  isAuthenticated: boolean;
}

/** Actions available on the auth store. */
interface AuthActions {
  /** Sets auth state from a successful login/register response. */
  setAuth: (response: AuthResponse) => void;
  /** Clears all auth state (logout). */
  clearAuth: () => void;
  /** Updates the access token after a refresh. */
  updateTokens: (accessToken: string, refreshToken: string) => void;
}

/** Initial unauthenticated state. */
const INITIAL_STATE: AuthState = {
  accessToken: null,
  refreshToken: null,
  userId: null,
  email: null,
  displayName: null,
  isAuthenticated: false,
};

/**
 * Zustand store for authentication state.
 * Persisted to localStorage so the user stays logged in across page refreshes.
 * The access token is included in API requests via the Axios interceptor.
 */
export const useAuthStore = create<AuthState & AuthActions>()(
  persist(
    (set) => ({
      ...INITIAL_STATE,

      setAuth: (response: AuthResponse) =>
        set({
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          userId: response.userId,
          email: response.email,
          displayName: response.displayName,
          isAuthenticated: true,
        }),

      clearAuth: () => set(INITIAL_STATE),

      updateTokens: (accessToken: string, refreshToken: string) =>
        set({ accessToken, refreshToken }),
    }),
    {
      name: AUTH_STORAGE_KEY,
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        userId: state.userId,
        email: state.email,
        displayName: state.displayName,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);
