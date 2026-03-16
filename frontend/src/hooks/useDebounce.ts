import { useState, useEffect } from 'react';

/**
 * Debounces a value by the specified delay.
 * Returns the debounced value that only updates after the delay has passed
 * since the last change.
 *
 * @param value - The value to debounce
 * @param delayMs - Debounce delay in milliseconds (default: 300ms)
 * @returns The debounced value
 *
 * @example
 * const debouncedQuery = useDebounce(searchQuery, 300);
 */
export function useDebounce<T>(value: T, delayMs = 300): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delayMs);

    return () => {
      clearTimeout(timer);
    };
  }, [value, delayMs]);

  return debouncedValue;
}
