import { createBrowserRouter } from 'react-router-dom';
import { DashboardLayout, AuthLayout } from '@/layouts';
import { ProtectedRoute } from '@/components/auth/ProtectedRoute';
import {
  NewsFeedPage,
  TrendingPage,
  WatchlistPage,
  SentimentPage,
  AlertsPage,
  SpacesPage,
  SpaceDetailPage,
  SettingsPage,
  LoginPage,
  RegisterPage,
  NotFoundPage,
} from '@/pages';

/**
 * Application router configuration.
 * Auth routes use AuthLayout (centered card, public).
 * Dashboard routes use DashboardLayout (sidebar + main) and require authentication.
 */
export const router = createBrowserRouter([
  // Auth routes — public, centered layout
  {
    element: <AuthLayout />,
    children: [
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
    ],
  },

  // Dashboard routes — protected, sidebar layout
  {
    element: (
      <ProtectedRoute>
        <DashboardLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <NewsFeedPage /> },
      { path: 'trending', element: <TrendingPage /> },
      { path: 'spaces', element: <SpacesPage /> },
      { path: 'spaces/:id', element: <SpaceDetailPage /> },
      { path: 'watchlist', element: <WatchlistPage /> },
      { path: 'sentiment', element: <SentimentPage /> },
      { path: 'alerts', element: <AlertsPage /> },
      { path: 'settings', element: <SettingsPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
