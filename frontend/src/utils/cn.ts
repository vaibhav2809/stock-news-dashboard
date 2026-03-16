import { clsx, type ClassValue } from 'clsx';

/**
 * Merges class names conditionally using clsx.
 * This is the standard utility for combining Tailwind classes with conditional logic.
 *
 * @param inputs - Class values to merge (strings, objects, arrays)
 * @returns A single merged class name string
 *
 * @example
 * cn('p-4', isActive && 'bg-blue-500', { 'text-white': isActive })
 */
export function cn(...inputs: ClassValue[]): string {
  return clsx(inputs);
}
