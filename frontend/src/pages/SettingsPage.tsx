import { Settings } from 'lucide-react';
import { ThemeToggle } from '@/components/ui';

/**
 * Settings page for user preferences (theme, notifications, etc.).
 */
export function SettingsPage() {
  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Settings</h1>
        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Manage your preferences and account settings
        </p>
      </div>

      <div className="max-w-2xl space-y-6">
        {/* Appearance */}
        <div className="rounded-xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-gray-900">
          <div className="flex items-center gap-3">
            <Settings className="h-5 w-5 text-gray-500 dark:text-gray-400" />
            <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Appearance</h2>
          </div>
          <div className="mt-4 flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-700 dark:text-gray-300">Theme</p>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Choose between light, dark, or system theme
              </p>
            </div>
            <ThemeToggle />
          </div>
        </div>
      </div>
    </div>
  );
}
