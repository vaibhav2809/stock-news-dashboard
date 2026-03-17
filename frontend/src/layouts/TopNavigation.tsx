import { useState, useEffect, useCallback } from 'react';
import { Menu, Search } from 'lucide-react';
import { ThemeToggle, CommandPalette } from '@/components/ui';
import { useSidebarStore } from '@/store';

/**
 * Top navigation bar with mobile hamburger menu, command palette trigger, and theme toggle.
 * Fixed at the top of the main content area (to the right of the sidebar).
 * The search bar opens a Command Palette (Ctrl+K) for quick stock search and page navigation.
 */
export function TopNavigation() {
  const { openMobileSidebar } = useSidebarStore();
  const [isCommandPaletteOpen, setIsCommandPaletteOpen] = useState(false);

  /** Opens the command palette. */
  const handleOpenPalette = useCallback(() => {
    setIsCommandPaletteOpen(true);
  }, []);

  /** Closes the command palette. */
  const handleClosePalette = useCallback(() => {
    setIsCommandPaletteOpen(false);
  }, []);

  /** Global keyboard shortcut: Ctrl+K / Cmd+K to open the command palette. */
  useEffect(() => {
    const handleGlobalKeyDown = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
        event.preventDefault();
        setIsCommandPaletteOpen((prev) => !prev);
      }
    };

    document.addEventListener('keydown', handleGlobalKeyDown);
    return () => document.removeEventListener('keydown', handleGlobalKeyDown);
  }, []);

  return (
    <>
      <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-gray-200 bg-white/80 px-4 backdrop-blur-md sm:px-6 dark:border-gray-800 dark:bg-gray-900/80">
        {/* Left side: hamburger (mobile) + search trigger */}
        <div className="flex flex-1 items-center gap-3">
          {/* Mobile hamburger menu button */}
          <button
            onClick={openMobileSidebar}
            className="rounded-lg p-2 text-gray-600 transition-colors hover:bg-gray-100 hover:text-gray-900 md:hidden dark:text-gray-400 dark:hover:bg-gray-800 dark:hover:text-gray-200"
            aria-label="Open navigation menu"
          >
            <Menu className="h-5 w-5" />
          </button>

          {/* Search Bar — clickable trigger for Command Palette */}
          <button
            onClick={handleOpenPalette}
            className="relative flex max-w-md flex-1 items-center gap-2 rounded-lg border border-gray-200 bg-gray-50 py-2 pl-3 pr-3 text-left text-sm text-gray-400 transition-colors hover:border-gray-300 hover:bg-gray-100 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-500 dark:hover:border-gray-600 dark:hover:bg-gray-750"
            aria-label="Open search (Ctrl+K)"
          >
            <Search className="h-4 w-4 flex-shrink-0" />
            <span className="flex-1">Search stocks...</span>
            <kbd className="hidden rounded border border-gray-300 bg-gray-100 px-1.5 py-0.5 text-xs text-gray-500 sm:inline-block dark:border-gray-600 dark:bg-gray-700 dark:text-gray-400">
              Ctrl+K
            </kbd>
          </button>
        </div>

        {/* Right side actions */}
        <div className="flex items-center gap-4">
          <ThemeToggle />
        </div>
      </header>

      {/* Command Palette Modal */}
      <CommandPalette isOpen={isCommandPaletteOpen} onClose={handleClosePalette} />
    </>
  );
}
