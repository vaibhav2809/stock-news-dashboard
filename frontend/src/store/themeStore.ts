import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type Theme = 'light' | 'dark' | 'system';

interface ThemeState {
  theme: Theme;
  setTheme: (theme: Theme) => void;
}

/**
 * Returns the effective theme (light or dark) based on the current setting.
 * If set to 'system', checks the user's OS preference.
 * @param theme - The stored theme preference
 * @returns 'light' or 'dark'
 */
function resolveEffectiveTheme(theme: Theme): 'light' | 'dark' {
  if (theme === 'system') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  return theme;
}

/**
 * Applies the theme to the document root element by toggling the 'dark' class.
 * @param theme - The theme preference to apply
 */
function applyThemeToDocument(theme: Theme): void {
  const effectiveTheme = resolveEffectiveTheme(theme);
  document.documentElement.classList.toggle('dark', effectiveTheme === 'dark');
}

/**
 * Global theme store with localStorage persistence.
 * Manages light/dark/system theme preference.
 */
export const useThemeStore = create<ThemeState>()(
  persist(
    (set) => ({
      theme: 'system',
      setTheme: (theme: Theme) => {
        applyThemeToDocument(theme);
        set({ theme });
      },
    }),
    {
      name: 'stocknews-theme',
      onRehydrateStorage: () => {
        return (state) => {
          if (state) {
            applyThemeToDocument(state.theme);
          }
        };
      },
    },
  ),
);

/**
 * Apply the saved theme immediately on module load, before React renders.
 * This prevents a flash of unstyled content (FOUC) when the user has
 * previously selected a non-default theme.
 */
function applyPersistedThemeOnLoad(): void {
  try {
    const stored = localStorage.getItem('stocknews-theme');
    if (stored) {
      const parsed = JSON.parse(stored) as { state?: { theme?: Theme } };
      const savedTheme = parsed?.state?.theme ?? 'system';
      applyThemeToDocument(savedTheme);
    } else {
      applyThemeToDocument('system');
    }
  } catch {
    applyThemeToDocument('system');
  }
}

applyPersistedThemeOnLoad();
