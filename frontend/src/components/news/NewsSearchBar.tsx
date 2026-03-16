import { useState, useRef, useEffect, useCallback } from 'react';
import { Search, X } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { searchSymbols } from '@/api/symbolsApi';
import type { StockSymbol } from '@/api/symbolsApi';
import { useDebounce } from '@/hooks/useDebounce';
import { cn } from '@/utils';

interface NewsSearchBarProps {
  /** Currently selected symbol tickers */
  selectedSymbols: string[];
  /** Callback when symbols selection changes */
  onSymbolsChange: (symbols: string[]) => void;
  /** Additional CSS classes */
  className?: string;
}

/**
 * Search bar with stock symbol autocomplete.
 * Users can type a ticker or company name and select from matching results.
 * Selected symbols appear as removable chips.
 *
 * @param selectedSymbols - Array of currently selected ticker symbols
 * @param onSymbolsChange - Callback invoked when the selection changes
 * @param className - Additional CSS classes
 */
export function NewsSearchBar({ selectedSymbols, onSymbolsChange, className }: NewsSearchBarProps) {
  const [inputValue, setInputValue] = useState('');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const inputRef = useRef<HTMLInputElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const debouncedQuery = useDebounce(inputValue.trim(), 300);

  /** Fetch matching symbols from the backend autocomplete endpoint. */
  const { data: searchResults = [], isFetching: isSearching } = useQuery({
    queryKey: ['symbol-search', debouncedQuery],
    queryFn: () => searchSymbols(debouncedQuery),
    enabled: debouncedQuery.length >= 1,
    staleTime: 30 * 1000, // 30 seconds
  });

  /** Filter out already-selected symbols from the dropdown. */
  const filteredResults = searchResults.filter(
    (symbol) => !selectedSymbols.includes(symbol.ticker),
  );

  /** Adds a symbol to the selection. */
  const handleSelectSymbol = useCallback(
    (symbol: StockSymbol) => {
      onSymbolsChange([...selectedSymbols, symbol.ticker]);
      setInputValue('');
      setIsDropdownOpen(false);
      setHighlightedIndex(-1);
      inputRef.current?.focus();
    },
    [selectedSymbols, onSymbolsChange],
  );

  /** Removes a symbol from the selection. */
  const handleRemoveSymbol = useCallback(
    (ticker: string) => {
      onSymbolsChange(selectedSymbols.filter((s) => s !== ticker));
    },
    [selectedSymbols, onSymbolsChange],
  );

  /** Keyboard navigation within the dropdown. */
  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent<HTMLInputElement>) => {
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        setHighlightedIndex((prev) =>
          prev < filteredResults.length - 1 ? prev + 1 : prev,
        );
      } else if (event.key === 'ArrowUp') {
        event.preventDefault();
        setHighlightedIndex((prev) => (prev > 0 ? prev - 1 : -1));
      } else if (event.key === 'Enter' && highlightedIndex >= 0) {
        event.preventDefault();
        const selected = filteredResults[highlightedIndex];
        if (selected) {
          handleSelectSymbol(selected);
        }
      } else if (event.key === 'Escape') {
        setIsDropdownOpen(false);
        setHighlightedIndex(-1);
      } else if (event.key === 'Backspace' && inputValue === '' && selectedSymbols.length > 0) {
        handleRemoveSymbol(selectedSymbols[selectedSymbols.length - 1]);
      }
    },
    [filteredResults, highlightedIndex, handleSelectSymbol, inputValue, selectedSymbols, handleRemoveSymbol],
  );

  /** Close dropdown when clicking outside. */
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node) &&
        inputRef.current &&
        !inputRef.current.contains(event.target as Node)
      ) {
        setIsDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  /** Open dropdown when input has content and results exist. */
  useEffect(() => {
    if (debouncedQuery.length >= 1 && filteredResults.length > 0) {
      setIsDropdownOpen(true);
    }
  }, [debouncedQuery, filteredResults.length]);

  return (
    <div className={cn('relative', className)}>
      {/* Input area with selected chips */}
      <div className="flex flex-wrap items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-2 focus-within:border-primary-500 focus-within:ring-1 focus-within:ring-primary-500 dark:border-gray-700 dark:bg-gray-800">
        <Search className="h-4 w-4 flex-shrink-0 text-gray-400" />

        {/* Selected symbol chips */}
        {selectedSymbols.map((ticker) => (
          <span
            key={ticker}
            className="inline-flex items-center gap-1 rounded-md bg-primary-100 px-2 py-0.5 text-xs font-semibold text-primary-800 dark:bg-primary-900/30 dark:text-primary-400"
          >
            {ticker}
            <button
              onClick={() => handleRemoveSymbol(ticker)}
              className="rounded-sm hover:bg-primary-200 dark:hover:bg-primary-800/50"
              aria-label={`Remove ${ticker}`}
            >
              <X className="h-3 w-3" />
            </button>
          </span>
        ))}

        {/* Search input */}
        <input
          ref={inputRef}
          type="text"
          value={inputValue}
          onChange={(e) => {
            setInputValue(e.target.value);
            setHighlightedIndex(-1);
          }}
          onFocus={() => {
            if (filteredResults.length > 0 && debouncedQuery.length >= 1) {
              setIsDropdownOpen(true);
            }
          }}
          onKeyDown={handleKeyDown}
          placeholder={selectedSymbols.length === 0 ? 'Search by ticker or company name...' : 'Add more...'}
          className="min-w-[120px] flex-1 bg-transparent text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none dark:text-gray-100 dark:placeholder:text-gray-500"
        />

        {/* Loading indicator */}
        {isSearching && (
          <div className="h-4 w-4 animate-spin rounded-full border-2 border-gray-300 border-t-primary-500" />
        )}
      </div>

      {/* Autocomplete dropdown */}
      {isDropdownOpen && filteredResults.length > 0 && (
        <div
          ref={dropdownRef}
          className="absolute left-0 right-0 top-full z-50 mt-1 max-h-60 overflow-y-auto rounded-lg border border-gray-200 bg-white shadow-lg dark:border-gray-700 dark:bg-gray-800"
        >
          {filteredResults.map((symbol, index) => (
            <button
              key={symbol.id}
              onClick={() => handleSelectSymbol(symbol)}
              onMouseEnter={() => setHighlightedIndex(index)}
              className={cn(
                'flex w-full items-center justify-between px-3 py-2 text-left text-sm transition-colors',
                index === highlightedIndex
                  ? 'bg-primary-50 dark:bg-primary-900/20'
                  : 'hover:bg-gray-50 dark:hover:bg-gray-700/50',
              )}
            >
              <div>
                <span className="font-semibold text-gray-900 dark:text-gray-100">
                  {symbol.ticker}
                </span>
                <span className="ml-2 text-gray-500 dark:text-gray-400">
                  {symbol.companyName}
                </span>
              </div>
              <span className="text-xs text-gray-400 dark:text-gray-500">{symbol.exchange}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
