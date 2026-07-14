import { useState } from 'react';
import { SellerSidebar } from './SellerSidebar';
import { SellerHeader } from './SellerHeader';

interface SellerLayoutProps {
  children: React.ReactNode;
}

export function SellerLayout({ children }: SellerLayoutProps) {
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(() => {
    return localStorage.getItem('sellerSidebarCollapsed') === 'true';
  });
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);

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
