import { Outlet, Navigate } from 'react-router-dom';
import { TrendingUp } from 'lucide-react';
import { useAuthStore } from '@/store/authStore';

/**
 * Layout for authentication pages (login, register).
 * Centered card with branding on a dark background.
 * Redirects to dashboard if the user is already authenticated.
 */
export function AuthLayout() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-950 px-4">
      <div className="w-full max-w-md">
        {/* Branding */}
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-xl bg-primary-600">
            <TrendingUp className="h-8 w-8 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-white">StockNews</h1>
          <p className="mt-1 text-sm text-gray-400">
            Track stock news, sentiment, and trends
          </p>
        </div>

        {/* Auth Form (Login or Register) */}
        <div className="rounded-xl border border-gray-800 bg-gray-900 p-6 shadow-xl">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
