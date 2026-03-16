import { Menu, Search } from 'lucide-react';
import { ThemeToggle } from '@/components/ui';
import { useSidebarStore } from '@/store';

/**
 * Top navigation bar with mobile hamburger menu, search input, and theme toggle.
 * Fixed at the top of the main content area (to the right of the sidebar).
 */
export function TopNavigation() {
  const { openMobileSidebar } = useSidebarStore();

  return (
    <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-gray-200 bg-white/80 px-4 backdrop-blur-md sm:px-6 dark:border-gray-800 dark:bg-gray-900/80">
      {/* Left side: hamburger (mobile) + search */}
      <div className="flex flex-1 items-center gap-3">
        {/* Mobile hamburger menu button */}
        <button
          onClick={openMobileSidebar}
          className="rounded-lg p-2 text-gray-600 transition-colors hover:bg-gray-100 hover:text-gray-900 md:hidden dark:text-gray-400 dark:hover:bg-gray-800 dark:hover:text-gray-200"
          aria-label="Open navigation menu"
        >
          <Menu className="h-5 w-5" />
        </button>

        {/* Search Bar */}
        <div className="relative max-w-md flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search stocks... (Ctrl+K)"
            className="w-full rounded-lg border border-gray-200 bg-gray-50 py-2 pl-10 pr-4 text-sm text-gray-900 placeholder:text-gray-400 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100 dark:placeholder:text-gray-500"
          />
          <kbd className="absolute right-3 top-1/2 hidden -translate-y-1/2 rounded border border-gray-300 bg-gray-100 px-1.5 py-0.5 text-xs text-gray-500 sm:inline-block dark:border-gray-600 dark:bg-gray-700 dark:text-gray-400">
            Ctrl+K
          </kbd>
        </div>
      </div>

      {/* Right side actions */}
      <div className="flex items-center gap-4">
        <ThemeToggle />
      </div>
    </header>
  );
}
