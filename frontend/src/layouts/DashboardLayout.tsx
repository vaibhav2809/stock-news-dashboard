import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { TopNavigation } from './TopNavigation';
import { useSidebarStore } from '@/store';
import { cn } from '@/utils';

/**
 * Main dashboard layout with sidebar navigation, top bar, and content area.
 * On desktop (md+), the content area adjusts its left margin based on sidebar collapse state.
 * On mobile (<md), the sidebar renders as a slide-over overlay with a backdrop.
 */
export function DashboardLayout() {
  const { isCollapsed, isMobileOpen, closeMobileSidebar } = useSidebarStore();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950">
      {/* Mobile backdrop overlay */}
      {isMobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 md:hidden"
          onClick={closeMobileSidebar}
          aria-hidden="true"
        />
      )}

      <Sidebar />

      <div
        className={cn(
          'transition-all duration-300',
          'md:ml-16',
          !isCollapsed && 'md:ml-[260px]',
        )}
      >
        <TopNavigation />
        <main className="p-4 sm:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
