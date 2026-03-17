import { useState, useEffect, useRef, useCallback, Fragment } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Search,
  TrendingUp,
  Bookmark,
  BarChart3,
  Bell,
  Newspaper,
  Settings,
  ArrowRight,
} from 'lucide-react';
import { searchSymbols } from '@/api/symbolsApi';
import type { StockSymbol } from '@/api/symbolsApi';
import { useDebounce } from '@/hooks/useDebounce';
import { cn } from '@/utils';

/** Navigation shortcut shown when the search input is empty. */
interface QuickNavigationItem {
  label: string;
  path: string;
  icon: React.ReactNode;
  description: string;
}

/** Quick-access page links shown when no search query is entered. */
const QUICK_NAVIGATION_ITEMS: QuickNavigationItem[] = [
  { label: 'News Feed', path: '/', icon: <Newspaper className="h-4 w-4" />, description: 'Search and browse stock news' },
  { label: 'Trending', path: '/trending', icon: <TrendingUp className="h-4 w-4" />, description: 'Most discussed stocks today' },
  { label: 'Watchlist', path: '/watchlist', icon: <BarChart3 className="h-4 w-4" />, description: 'Your tracked stocks' },
  { label: 'Spaces', path: '/spaces', icon: <Bookmark className="h-4 w-4" />, description: 'Saved article collections' },
  { label: 'Alerts', path: '/alerts', icon: <Bell className="h-4 w-4" />, description: 'Manage news alerts' },
  { label: 'Settings', path: '/settings', icon: <Settings className="h-4 w-4" />, description: 'App preferences' },
];

interface CommandPaletteProps {
  /** Whether the palette is open. */
  isOpen: boolean;
  /** Callback to close the palette. */
  onClose: () => void;
}

/**
 * Command palette modal (Ctrl+K) for quick stock search and page navigation.
 * When the search input is empty, shows quick navigation links to all pages.
 * When a query is typed, shows matching stock symbols with autocomplete.
 * Selecting a stock navigates to the News Feed page with that symbol pre-selected.
 *
 * @param isOpen - Whether the modal is currently visible
 * @param onClose - Callback invoked when the user closes the modal
 */
export function CommandPalette({ isOpen, onClose }: CommandPaletteProps) {
  const navigate = useNavigate();
  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);

  const [query, setQuery] = useState('');
  const [highlightedIndex, setHighlightedIndex] = useState(0);

  const debouncedQuery = useDebounce(query.trim(), 200);

  /** Fetch matching stock symbols from the backend. */
  const { data: symbolResults = [], isFetching: isSearching } = useQuery({
    queryKey: ['command-palette-search', debouncedQuery],
    queryFn: () => searchSymbols(debouncedQuery),
    enabled: debouncedQuery.length >= 1,
    staleTime: 30 * 1000,
  });

  const isShowingSymbols = debouncedQuery.length >= 1;
  const totalItems = isShowingSymbols ? symbolResults.length : QUICK_NAVIGATION_ITEMS.length;

  /** Navigate to the News Feed with the selected stock symbol. */
  const handleSelectSymbol = useCallback(
    (symbol: StockSymbol) => {
      onClose();
      navigate(`/?symbol=${encodeURIComponent(symbol.ticker)}`);
    },
    [navigate, onClose],
  );

  /** Navigate to a page from the quick navigation list. */
  const handleNavigate = useCallback(
    (path: string) => {
      onClose();
      navigate(path);
    },
    [navigate, onClose],
  );

  /** Select the currently highlighted item. */
  const handleSelectHighlighted = useCallback(() => {
    if (isShowingSymbols) {
      const symbol = symbolResults[highlightedIndex];
      if (symbol) handleSelectSymbol(symbol);
    } else {
      const navItem = QUICK_NAVIGATION_ITEMS[highlightedIndex];
      if (navItem) handleNavigate(navItem.path);
    }
  }, [isShowingSymbols, symbolResults, highlightedIndex, handleSelectSymbol, handleNavigate]);

  /** Keyboard navigation: arrows, enter, escape. */
  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent<HTMLInputElement>) => {
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        setHighlightedIndex((prev) => (prev < totalItems - 1 ? prev + 1 : 0));
      } else if (event.key === 'ArrowUp') {
        event.preventDefault();
        setHighlightedIndex((prev) => (prev > 0 ? prev - 1 : totalItems - 1));
      } else if (event.key === 'Enter') {
        event.preventDefault();
        handleSelectHighlighted();
      } else if (event.key === 'Escape') {
        onClose();
      }
    },
    [totalItems, handleSelectHighlighted, onClose],
  );

  /** Reset state when opening; focus input. */
  useEffect(() => {
    if (isOpen) {
      setQuery('');
      setHighlightedIndex(0);
      // Small delay to ensure modal is rendered before focusing
      requestAnimationFrame(() => inputRef.current?.focus());
    }
  }, [isOpen]);

  /** Reset highlighted index when results change. */
  useEffect(() => {
    setHighlightedIndex(0);
  }, [debouncedQuery]);

  /** Scroll highlighted item into view. */
  useEffect(() => {
    if (!listRef.current) return;
    const highlighted = listRef.current.querySelector('[data-highlighted="true"]');
    if (highlighted) {
      highlighted.scrollIntoView({ block: 'nearest' });
    }
  }, [highlightedIndex]);

  if (!isOpen) return null;

  return (
    <Fragment>
      {/* Backdrop */}
      <div
        className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Modal */}
      <div className="fixed inset-x-0 top-[15%] z-50 mx-auto w-full max-w-lg px-4">
        <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-2xl dark:border-gray-700 dark:bg-gray-900">
          {/* Search input */}
          <div className="flex items-center gap-3 border-b border-gray-200 px-4 py-3 dark:border-gray-700">
            <Search className="h-5 w-5 flex-shrink-0 text-gray-400" />
            <input
              ref={inputRef}
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Search stocks or jump to a page..."
              className="flex-1 bg-transparent text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none dark:text-gray-100 dark:placeholder:text-gray-500"
            />
            {isSearching && (
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-gray-300 border-t-primary-500" />
            )}
            <kbd className="hidden rounded border border-gray-300 bg-gray-100 px-1.5 py-0.5 text-xs text-gray-500 sm:inline-block dark:border-gray-600 dark:bg-gray-700 dark:text-gray-400">
              ESC
            </kbd>
          </div>

          {/* Results list */}
          <div ref={listRef} className="max-h-72 overflow-y-auto py-2">
            {isShowingSymbols ? (
              // Stock symbol results
              symbolResults.length > 0 ? (
                <div>
                  <div className="px-4 py-1.5 text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500">
                    Stocks
                  </div>
                  {symbolResults.slice(0, 20).map((symbol, index) => (
                    <button
                      key={symbol.id}
                      data-highlighted={index === highlightedIndex}
                      onClick={() => handleSelectSymbol(symbol)}
                      onMouseEnter={() => setHighlightedIndex(index)}
                      className={cn(
                        'flex w-full items-center justify-between px-4 py-2.5 text-left text-sm transition-colors',
                        index === highlightedIndex
                          ? 'bg-primary-50 text-primary-900 dark:bg-primary-900/20 dark:text-primary-100'
                          : 'text-gray-700 hover:bg-gray-50 dark:text-gray-300 dark:hover:bg-gray-800',
                      )}
                    >
                      <div className="flex items-center gap-3">
                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gray-100 text-xs font-bold text-gray-600 dark:bg-gray-800 dark:text-gray-400">
                          {symbol.ticker.slice(0, 2)}
                        </div>
                        <div>
                          <div className="font-semibold">{symbol.ticker}</div>
                          <div className="text-xs text-gray-500 dark:text-gray-400">
                            {symbol.companyName}
                          </div>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-gray-400 dark:text-gray-500">
                          {symbol.exchange}
                        </span>
                        <ArrowRight className="h-3.5 w-3.5 text-gray-400" />
                      </div>
                    </button>
                  ))}
                </div>
              ) : !isSearching ? (
                <div className="px-4 py-8 text-center text-sm text-gray-500 dark:text-gray-400">
                  No stocks found for &ldquo;{query}&rdquo;
                </div>
              ) : null
            ) : (
              // Quick navigation
              <div>
                <div className="px-4 py-1.5 text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500">
                  Quick Navigation
                </div>
                {QUICK_NAVIGATION_ITEMS.map((item, index) => (
                  <button
                    key={item.path}
                    data-highlighted={index === highlightedIndex}
                    onClick={() => handleNavigate(item.path)}
                    onMouseEnter={() => setHighlightedIndex(index)}
                    className={cn(
                      'flex w-full items-center justify-between px-4 py-2.5 text-left text-sm transition-colors',
                      index === highlightedIndex
                        ? 'bg-primary-50 text-primary-900 dark:bg-primary-900/20 dark:text-primary-100'
                        : 'text-gray-700 hover:bg-gray-50 dark:text-gray-300 dark:hover:bg-gray-800',
                    )}
                  >
                    <div className="flex items-center gap-3">
                      <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400">
                        {item.icon}
                      </div>
                      <div>
                        <div className="font-medium">{item.label}</div>
                        <div className="text-xs text-gray-500 dark:text-gray-400">
                          {item.description}
                        </div>
                      </div>
                    </div>
                    <ArrowRight className="h-3.5 w-3.5 text-gray-400" />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Footer hint */}
          <div className="flex items-center justify-between border-t border-gray-200 px-4 py-2 dark:border-gray-700">
            <div className="flex items-center gap-3 text-xs text-gray-400 dark:text-gray-500">
              <span className="flex items-center gap-1">
                <kbd className="rounded border border-gray-300 bg-gray-100 px-1 py-0.5 text-[10px] dark:border-gray-600 dark:bg-gray-700">↑↓</kbd>
                navigate
              </span>
              <span className="flex items-center gap-1">
                <kbd className="rounded border border-gray-300 bg-gray-100 px-1 py-0.5 text-[10px] dark:border-gray-600 dark:bg-gray-700">↵</kbd>
                select
              </span>
              <span className="flex items-center gap-1">
                <kbd className="rounded border border-gray-300 bg-gray-100 px-1 py-0.5 text-[10px] dark:border-gray-600 dark:bg-gray-700">esc</kbd>
                close
              </span>
            </div>
          </div>
        </div>
      </div>
    </Fragment>
  );
}
