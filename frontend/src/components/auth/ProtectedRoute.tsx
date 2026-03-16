import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

/**
 * Route guard that redirects unauthenticated users to the login page.
 * Preserves the original URL so the user can be redirected back after login.
 */
export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  return <>{children}</>;
}
