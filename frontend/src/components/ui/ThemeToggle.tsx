import { Sun, Moon, Monitor } from 'lucide-react';
import { useThemeStore } from '@/store';
import { cn } from '@/utils';

type Theme = 'light' | 'dark' | 'system';

/** Configuration for each theme option button. */
const THEME_OPTIONS: { value: Theme; icon: typeof Sun; label: string }[] = [
  { value: 'light', icon: Sun, label: 'Light mode' },
  { value: 'dark', icon: Moon, label: 'Dark mode' },
  { value: 'system', icon: Monitor, label: 'System theme' },
];

/**
 * Theme toggle component that switches between light, dark, and system themes.
 * Displays as a segmented control with icon buttons.
 */
export function ThemeToggle() {
  const { theme, setTheme } = useThemeStore();

  return (
    <div className="flex items-center gap-1 rounded-lg bg-gray-100 p-1 dark:bg-gray-800">
      {THEME_OPTIONS.map(({ value, icon: Icon, label }) => (
        <button
          key={value}
          onClick={() => setTheme(value)}
          className={cn(
            'rounded-md p-1.5 transition-colors',
            theme === value
              ? 'bg-white text-gray-900 shadow-sm dark:bg-gray-700 dark:text-gray-100'
              : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200',
          )}
          aria-label={label}
          title={label}
        >
          <Icon className="h-4 w-4" />
        </button>
      ))}
    </div>
  );
}
