import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface SidebarState {
  isCollapsed: boolean;
  isMobileOpen: boolean;
  toggleSidebar: () => void;
  setSidebarCollapsed: (isCollapsed: boolean) => void;
  openMobileSidebar: () => void;
  closeMobileSidebar: () => void;
}

/**
 * Global sidebar state store with localStorage persistence.
 * Controls whether the sidebar navigation is collapsed or expanded,
 * and whether the mobile sidebar overlay is open.
 */
export const useSidebarStore = create<SidebarState>()(
  persist(
    (set) => ({
      isCollapsed: false,
      isMobileOpen: false,
      toggleSidebar: () => set((state) => ({ isCollapsed: !state.isCollapsed })),
      setSidebarCollapsed: (isCollapsed: boolean) => set({ isCollapsed }),
      openMobileSidebar: () => set({ isMobileOpen: true }),
      closeMobileSidebar: () => set({ isMobileOpen: false }),
    }),
    {
      name: 'stocknews-sidebar',
      partialize: (state) => ({ isCollapsed: state.isCollapsed }),
    },
  ),
);
