import { NavLink, Link, useNavigate } from 'react-router-dom';
import {
  Newspaper,
  TrendingUp,
  FolderOpen,
  Bookmark,
  Bell,
  BarChart3,
  Settings,
  ChevronLeft,
  ChevronRight,
  LogOut,
  Loader2,
  X,
} from 'lucide-react';
import { useSidebarStore } from '@/store';
import { useAuthStore } from '@/store/authStore';
import { useWatchlist, useRemoveFromWatchlist } from '@/hooks/useWatchlist';
import { cn } from '@/utils';

/** Navigation items displayed in the sidebar. */
const NAVIGATION_ITEMS = [
  { to: '/', icon: Newspaper, label: 'News Feed' },
  { to: '/trending', icon: TrendingUp, label: 'Trending' },
  { to: '/spaces', icon: FolderOpen, label: 'Spaces' },
  { to: '/watchlist', icon: Bookmark, label: 'Watchlist' },
  { to: '/sentiment', icon: BarChart3, label: 'Sentiment' },
  { to: '/alerts', icon: Bell, label: 'Alerts' },
] as const;

const BOTTOM_NAVIGATION_ITEMS = [
  { to: '/settings', icon: Settings, label: 'Settings' },
] as const;

/**
 * Sidebar navigation component with collapsible state.
 * On desktop (md+), it is always visible and can be collapsed.
 * On mobile (<md), it slides in as an overlay when isMobileOpen is true.
 * Contains main navigation links, a quick watchlist section, and user info with logout.
 */
export function Sidebar() {
  const { isCollapsed, isMobileOpen, toggleSidebar, closeMobileSidebar } = useSidebarStore();
  const { displayName, clearAuth } = useAuthStore();
  const navigate = useNavigate();
  const { data: watchlistItems, isLoading: isWatchlistLoading } = useWatchlist();
  const removeFromWatchlist = useRemoveFromWatchlist();

  /** Handles user logout -- clears auth state and redirects to login. */
  const handleLogout = () => {
    clearAuth();
    navigate('/login', { replace: true });
  };

  /** Closes the mobile sidebar overlay when a nav link is clicked. */
  const handleMobileNavClick = () => {
    closeMobileSidebar();
  };

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-50 flex h-screen flex-col border-r border-gray-200 bg-white transition-all duration-300 dark:border-gray-800 dark:bg-gray-900',
        // Desktop: always visible, width depends on collapsed state
        'max-md:w-[260px]',
        'md:z-30',
        isCollapsed ? 'md:w-16' : 'md:w-[260px]',
        // Mobile: translate off-screen when closed
        isMobileOpen ? 'max-md:translate-x-0' : 'max-md:-translate-x-full',
      )}
    >
      {/* Logo / Brand -- navigates to home (News Feed) on click */}
      <Link
        to="/"
        onClick={handleMobileNavClick}
        className="flex h-16 items-center gap-3 border-b border-gray-200 px-4 transition-colors hover:bg-gray-50 dark:border-gray-800 dark:hover:bg-gray-800/50"
      >
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary-600 text-white">
          <TrendingUp className="h-5 w-5" />
        </div>
        {/* Show label on mobile always, on desktop only when not collapsed */}
        <span
          className={cn(
            'text-lg font-semibold text-gray-900 dark:text-gray-100',
            isCollapsed ? 'md:hidden' : '',
          )}
        >
          StockNews
        </span>
      </Link>

      {/* Main Navigation */}
      <nav className="space-y-1 px-2 py-4">
        {NAVIGATION_ITEMS.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            onClick={handleMobileNavClick}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-primary-50 text-primary-700 dark:bg-primary-900/20 dark:text-primary-400'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-400 dark:hover:bg-gray-800 dark:hover:text-gray-200',
                isCollapsed && 'md:justify-center md:px-2',
              )
            }
            title={isCollapsed ? label : undefined}
          >
            <Icon className="h-5 w-5 shrink-0" />
            {/* Show label on mobile always, on desktop only when not collapsed */}
            <span className={cn(isCollapsed && 'md:hidden')}>{label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Quick Watchlist Section -- visible when sidebar is expanded (desktop) or on mobile */}
      <div
        className={cn(
          'flex-1 overflow-y-auto border-t border-gray-200 px-2 py-3 dark:border-gray-800',
          isCollapsed && 'md:hidden',
        )}
      >
        <div className="mb-2 flex items-center justify-between px-3">
          <h3 className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500">
            Watchlist
          </h3>
          <NavLink
            to="/watchlist"
            onClick={handleMobileNavClick}
            className="text-xs text-primary-600 hover:text-primary-700 dark:text-primary-400 dark:hover:text-primary-300"
          >
            Manage
          </NavLink>
        </div>

        {isWatchlistLoading ? (
          <div className="flex items-center justify-center py-4">
            <Loader2 className="h-4 w-4 animate-spin text-gray-400" />
          </div>
        ) : watchlistItems && watchlistItems.length > 0 ? (
          <div className="space-y-0.5">
            {watchlistItems.map((item) => (
              <div
                key={item.id}
                className="group flex items-center justify-between rounded-lg px-3 py-2 transition-colors hover:bg-gray-50 dark:hover:bg-gray-800/50"
              >
                <Link
                  to={`/sentiment`}
                  onClick={handleMobileNavClick}
                  className="flex flex-1 items-center gap-2 text-sm"
                >
                  <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded bg-primary-100 text-[10px] font-bold text-primary-700 dark:bg-primary-900/30 dark:text-primary-400">
                    {item.symbol.slice(0, 2)}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-gray-800 dark:text-gray-200">
                      {item.symbol}
                    </p>
                    <p className="truncate text-[10px] text-gray-400 dark:text-gray-500">
                      {item.articleCount} articles
                    </p>
                  </div>
                </Link>
                <button
                  onClick={() => removeFromWatchlist.mutate(item.symbol)}
                  className="shrink-0 rounded p-1 text-gray-400 opacity-0 transition-opacity hover:bg-gray-200 hover:text-red-500 group-hover:opacity-100 dark:hover:bg-gray-700 dark:hover:text-red-400"
                  title={`Remove ${item.symbol} from watchlist`}
                >
                  <X className="h-3 w-3" />
                </button>
              </div>
            ))}
          </div>
        ) : (
          <div className="px-3 py-4 text-center">
            <Bookmark className="mx-auto mb-1.5 h-5 w-5 text-gray-300 dark:text-gray-600" />
            <p className="text-xs text-gray-400 dark:text-gray-500">
              No symbols watched
            </p>
            <NavLink
              to="/watchlist"
              onClick={handleMobileNavClick}
              className="mt-1 inline-block text-xs text-primary-600 hover:text-primary-700 dark:text-primary-400"
            >
              Add symbols
            </NavLink>
          </div>
        )}
      </div>

      {/* Collapsed state: spacer to push bottom nav down (desktop only) */}
      {isCollapsed && <div className="hidden flex-1 md:block" />}

      {/* Bottom Navigation */}
      <div className="space-y-1 border-t border-gray-200 px-2 py-4 dark:border-gray-800">
        {BOTTOM_NAVIGATION_ITEMS.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            onClick={handleMobileNavClick}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-primary-50 text-primary-700 dark:bg-primary-900/20 dark:text-primary-400'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-400 dark:hover:bg-gray-800 dark:hover:text-gray-200',
                isCollapsed && 'md:justify-center md:px-2',
              )
            }
            title={isCollapsed ? label : undefined}
          >
            <Icon className="h-5 w-5 shrink-0" />
            <span className={cn(isCollapsed && 'md:hidden')}>{label}</span>
          </NavLink>
        ))}

        {/* User Info & Logout */}
        {displayName && (
          <div
            className={cn(
              'flex items-center gap-3 rounded-lg px-3 py-2.5',
              isCollapsed && 'md:justify-center md:px-2',
            )}
          >
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary-600 text-xs font-bold text-white">
              {displayName.charAt(0).toUpperCase()}
            </div>
            <span
              className={cn(
                'flex-1 truncate text-sm font-medium text-gray-700 dark:text-gray-300',
                isCollapsed && 'md:hidden',
              )}
            >
              {displayName}
            </span>
          </div>
        )}

        <button
          onClick={handleLogout}
          className={cn(
            'flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-gray-600 transition-colors hover:bg-red-50 hover:text-red-600 dark:text-gray-400 dark:hover:bg-red-900/20 dark:hover:text-red-400',
            isCollapsed && 'md:justify-center md:px-2',
          )}
          title={isCollapsed ? 'Logout' : undefined}
        >
          <LogOut className="h-5 w-5 shrink-0" />
          <span className={cn(isCollapsed && 'md:hidden')}>Logout</span>
        </button>

        {/* Collapse Toggle -- desktop only */}
        <button
          onClick={toggleSidebar}
          className={cn(
            'hidden w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-gray-600 transition-colors hover:bg-gray-100 hover:text-gray-900 md:flex dark:text-gray-400 dark:hover:bg-gray-800 dark:hover:text-gray-200',
            isCollapsed && 'md:justify-center md:px-2',
          )}
          aria-label={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {isCollapsed ? (
            <ChevronRight className="h-5 w-5 shrink-0" />
          ) : (
            <>
              <ChevronLeft className="h-5 w-5 shrink-0" />
              <span>Collapse</span>
            </>
          )}
        </button>
      </div>
    </aside>
  );
}
