import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { AdminSidebar } from './AdminSidebar';

export function AdminLayout() {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [isMobileOpen, setIsMobileOpen] = useState(false);

  return (
    <div className="flex min-h-screen bg-background text-on-background font-body-md">
      <AdminSidebar 
        isCollapsed={isSidebarCollapsed}
        setIsCollapsed={setIsSidebarCollapsed}
        isMobileOpen={isMobileOpen}
        setIsMobileOpen={setIsMobileOpen}
      />
      
      {/* Mobile Overlay */}
      {isMobileOpen && (
        <div 
          className="fixed inset-0 bg-black/50 z-30 md:hidden"
          onClick={() => setIsMobileOpen(false)}
        />
      )}

      {/* Main Content Area */}
      <div className={`flex-1 flex flex-col min-h-screen transition-all duration-300 ${isSidebarCollapsed ? 'md:ml-[88px]' : 'md:ml-[240px]'}`}>
        
        {/* Mobile Header */}
        <header className="md:hidden flex items-center justify-between p-stack-sm bg-surface-container-lowest border-b border-outline-variant z-20 sticky top-0">
          <div className="flex items-center gap-unit">
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center">
              <span className="material-symbols-outlined text-on-primary text-[20px]">admin_panel_settings</span>
            </div>
            <span className="font-title-md text-primary">Admin</span>
          </div>
          <button 
            onClick={() => setIsMobileOpen(true)}
            className="p-unit text-on-surface-variant hover:bg-surface-variant rounded transition-colors"
          >
            <span className="material-symbols-outlined">menu</span>
          </button>
        </header>

        <main className="flex-1 p-gutter">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
