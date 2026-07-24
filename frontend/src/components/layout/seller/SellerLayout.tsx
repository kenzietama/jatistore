import { useState, useEffect } from 'react';
import { SellerSidebar } from './SellerSidebar';
import { SellerHeader } from './SellerHeader';
import api from '../../../lib/api';

interface SellerLayoutProps {
  children: React.ReactNode;
}

export function SellerLayout({ children }: SellerLayoutProps) {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(() => {
    return localStorage.getItem('sellerSidebarCollapsed') === 'true';
  });
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);

  useEffect(() => {
    // Check seller active status immediately on mount, then poll every 5 seconds
    const checkStatus = async () => {
      try {
        await api.get("/api/v1/seller/dashboard/profile");
      } catch (error) {
        // Interceptor in api.ts will handle 401/403 automatically
      }
    };
    checkStatus();

    const interval = setInterval(checkStatus, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleSetCollapsed = (val: boolean) => {
    setIsSidebarCollapsed(val);
    localStorage.setItem('sellerSidebarCollapsed', String(val));
  };

  return (
    <div className="bg-background text-on-background font-body-md min-h-screen flex">
      <SellerSidebar 
        isCollapsed={isSidebarCollapsed} 
        setIsCollapsed={handleSetCollapsed}
        isMobileOpen={isMobileSidebarOpen}
        setIsMobileOpen={setIsMobileSidebarOpen}
      />
      <main className={`flex-1 transition-all duration-300 ml-0 ${isSidebarCollapsed ? 'md:ml-[88px]' : 'md:ml-[240px]'} p-gutter`}>
        <SellerHeader onMenuClick={() => setIsMobileSidebarOpen(true)} />
        {children}
      </main>

      {/* Mobile Backdrop */}
      {isMobileSidebarOpen && (
        <div 
          className="fixed inset-0 bg-on-background/20 backdrop-blur-sm z-30 md:hidden"
          onClick={() => setIsMobileSidebarOpen(false)}
        />
      )}
    </div>
  );
}
