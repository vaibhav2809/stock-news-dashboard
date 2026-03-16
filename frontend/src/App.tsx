import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { router } from './router';

/**
 * TanStack Query client with sensible defaults for a financial dashboard.
 * - staleTime: 2 minutes (news data is relatively fresh)
 * - retry: 2 attempts on failure
 * - refetchOnWindowFocus: true (re-fetch when user returns to tab)
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 2 * 60 * 1000,
      retry: 2,
      refetchOnWindowFocus: true,
    },
  },
});

/**
 * Root application component.
 * Wraps the app with TanStack Query provider and React Router.
 */
export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}
