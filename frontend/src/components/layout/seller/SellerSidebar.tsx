import { NavLink, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { dashboardService } from '../../../service/seller/dashboard.service';
import type { SellerProfile } from '../../../service/seller/dashboard.service';
import { useAuthStore } from '../../../store/auth/useAuthStore';
import { authService } from '../../../service/auth/authService';

interface SellerSidebarProps {
  isCollapsed: boolean;
  setIsCollapsed: (v: boolean) => void;
  isMobileOpen: boolean;
  setIsMobileOpen: (v: boolean) => void;
}

export function SellerSidebar({ isCollapsed, setIsCollapsed, isMobileOpen, setIsMobileOpen }: SellerSidebarProps) {
  const widthClass = isCollapsed ? 'md:w-[88px]' : 'md:w-[240px]';
  const transformClass = isMobileOpen ? 'translate-x-0' : '-translate-x-full';

  const [profile, setProfile] = useState<SellerProfile | null>(null);
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);
  const [hasNewFlashSale, setHasNewFlashSale] = useState(false);
  const navigate = useNavigate();
  const logoutStore = useAuthStore((state) => state.logout);

  useEffect(() => {
    dashboardService.getProfile().then(setProfile).catch(console.error);
    
    const checkFlashSales = async () => {
      try {
        const { sellerFlashSaleService } = await import('../../../service/seller/flash-sale.service');
        const available = await sellerFlashSaleService.getAvailableFlashSales();
        const upcomingIds = available.filter(f => f.status === 'upcoming').map(f => f.id);
        const user = useAuthStore.getState().user;
        if (upcomingIds.length > 0 && user?.userId) {
          const storageKey = `seenFlashSaleIds_${user.userId}`;
          let seenIds: string[] = [];
          try {
            const parsed = JSON.parse(localStorage.getItem(storageKey) || '[]');
            seenIds = Array.isArray(parsed) ? parsed : [];
          } catch {
            seenIds = [];
          }
          const hasNew = upcomingIds.some(id => !seenIds.includes(id));
          setHasNewFlashSale(hasNew);
        }
      } catch (err) {
        console.error('Failed to check flash sales', err);
      }
    };
    checkFlashSales();
  }, []);

  const handleLogout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error("Logout failed at server", error);
    } finally {
      logoutStore();
      localStorage.removeItem('sellerProducts');
      setIsLogoutModalOpen(false);
      navigate('/auth/login');
    }
  };

  return (
    <>
    <nav className={`fixed left-0 top-0 flex-col p-stack-md z-40 h-full bg-surface-container dark:bg-inverse-surface border-r border-outline-variant dark:border-outline flex w-[240px] ${widthClass} transform ${transformClass} md:translate-x-0 transition-all duration-300`}>
      
      {/* Header */}
      <div className="mb-stack-lg flex items-start justify-between">
        {/* Logo and Title (Hidden when collapsed on desktop) */}
        <div className={`flex-col ${isCollapsed ? 'hidden' : 'flex'}`}>
          <div 
            className="flex items-center gap-unit mb-unit cursor-pointer hover:opacity-80 transition-opacity"
            onClick={() => setIsCollapsed(!isCollapsed)}
            title="Collapse Sidebar"
          >
            <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-on-primary shrink-0">
              <span className="material-symbols-outlined" data-icon="storefront" data-weight="fill">storefront</span>
            </div>
            <h1 className="text-headline-sm font-bold text-primary dark:text-inverse-primary font-headline-sm whitespace-nowrap overflow-hidden">Store Backoffice</h1>
          </div>
          <div>
            <span className="text-label-sm font-label-sm font-mono-data text-on-surface-variant px-2 py-1 rounded bg-surface-variant/50 inline-block">SELLER</span>
          </div>
        </div>

        {/* Collapsed Logo (Only visible when collapsed on desktop) */}
        {isCollapsed && (
          <div 
            className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-on-primary mx-auto mb-unit shrink-0 hidden md:flex cursor-pointer hover:scale-110 transition-transform"
            onClick={() => setIsCollapsed(!isCollapsed)}
            title="Expand Sidebar"
          >
            <span className="material-symbols-outlined" data-icon="storefront" data-weight="fill">storefront</span>
          </div>
        )}



        {/* Close Button (Mobile) */}
        <button 
          className="md:hidden text-on-surface-variant p-1 absolute right-4 top-4" 
          onClick={() => setIsMobileOpen(false)}
        >
          <span className="material-symbols-outlined">close</span>
        </button>
      </div>

      {/* Navigation Links */}
      <ul className="space-y-unit flex-1 overflow-y-auto overflow-x-hidden mt-4">
        <li>
          <NavLink
            to="/seller/dashboard"
            end
            onClick={() => setIsMobileOpen(false)}
            className={({ isActive }) =>
              isActive
                ? `flex items-center gap-stack-sm p-2 bg-secondary-container dark:bg-secondary text-on-secondary-container dark:text-on-secondary rounded-lg scale-[0.98] transition-all ${isCollapsed ? 'justify-center' : ''}`
                : `flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-all ${isCollapsed ? 'justify-center' : ''}`
            }
            title={isCollapsed ? "Dashboard" : undefined}
          >
            <span className="material-symbols-outlined shrink-0" data-icon="dashboard">dashboard</span>
            <span className={`font-label-md text-label-md whitespace-nowrap ${isCollapsed ? 'hidden' : 'block'}`}>Dashboard</span>
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/seller/products"
            onClick={() => setIsMobileOpen(false)}
            className={({ isActive }) =>
              isActive
                ? `flex items-center gap-stack-sm p-2 bg-secondary-container dark:bg-secondary text-on-secondary-container dark:text-on-secondary rounded-lg scale-[0.98] transition-all ${isCollapsed ? 'justify-center' : ''}`
                : `flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-all ${isCollapsed ? 'justify-center' : ''}`
            }
            title={isCollapsed ? "Products" : undefined}
          >
            <span className="material-symbols-outlined shrink-0" data-icon="inventory_2">inventory_2</span>
            <span className={`font-label-md text-label-md whitespace-nowrap ${isCollapsed ? 'hidden' : 'block'}`}>Products</span>
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/seller/orders"
            onClick={() => setIsMobileOpen(false)}
            className={({ isActive }) =>
              isActive
                ? `flex items-center gap-stack-sm p-2 bg-secondary-container dark:bg-secondary text-on-secondary-container dark:text-on-secondary rounded-lg scale-[0.98] transition-all ${isCollapsed ? 'justify-center' : ''}`
                : `flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-all ${isCollapsed ? 'justify-center' : ''}`
            }
            title={isCollapsed ? "Orders" : undefined}
          >
            <span className="material-symbols-outlined shrink-0" data-icon="shopping_bag">shopping_bag</span>
            <span className={`font-label-md text-label-md whitespace-nowrap ${isCollapsed ? 'hidden' : 'block'}`}>Orders</span>
          </NavLink>
        </li>
        <li>
          <NavLink 
            to="/seller/flash-sales"
            onClick={() => {
              setIsMobileOpen(false);
              setHasNewFlashSale(false);
            }}
            className={({ isActive }) =>
              `relative ${isActive
                ? `flex items-center gap-stack-sm p-2 bg-secondary-container dark:bg-secondary text-on-secondary-container dark:text-on-secondary rounded-lg scale-[0.98] transition-all ${isCollapsed ? 'justify-center' : ''}`
                : `flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-all ${isCollapsed ? 'justify-center' : ''}`
              }`
            }
            title={isCollapsed ? "Flash Sale" : undefined}
          >
            <span className="material-symbols-outlined shrink-0" data-icon="bolt">bolt</span>
            <span className={`font-label-md text-label-md whitespace-nowrap flex-1 flex items-center justify-between ${isCollapsed ? 'hidden' : 'flex'}`}>
              Flash Sale
              {hasNewFlashSale && (
                <span className="text-[10px] font-bold bg-error text-on-error px-1.5 py-0.5 rounded uppercase">New</span>
              )}
            </span>
            {isCollapsed && hasNewFlashSale && (
              <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-error"></span>
            )}
          </NavLink>
        </li>
        <li>
          <NavLink 
            to="/seller/financials"
            onClick={() => setIsMobileOpen(false)}
            className={({ isActive }) =>
              isActive
                ? `flex items-center gap-stack-sm p-2 bg-secondary-container dark:bg-secondary text-on-secondary-container dark:text-on-secondary rounded-lg scale-[0.98] transition-all ${isCollapsed ? 'justify-center' : ''}`
                : `flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-all ${isCollapsed ? 'justify-center' : ''}`
            }
            title={isCollapsed ? "Financials" : undefined}
          >
            <span className="material-symbols-outlined shrink-0" data-icon="payments">payments</span>
            <span className={`font-label-md text-label-md whitespace-nowrap ${isCollapsed ? 'hidden' : 'block'}`}>Financials</span>
          </NavLink>
        </li>
      </ul>

      {/* Bottom Profile */}
      <div className="mt-auto pt-stack-md border-t border-outline-variant">
        {!isCollapsed ? (
          <div className="flex items-center justify-between p-stack-sm bg-surface-variant rounded">
            <div className="flex items-center gap-stack-sm w-full overflow-hidden">
              {profile ? (
                <>
                  <img alt="Seller Profile" className="w-8 h-8 rounded-full object-cover border border-outline-variant shrink-0 bg-surface-variant" src={profile.storeImage || 'https://ui-avatars.com/api/?name=Store'} />
                  <div className="flex flex-col overflow-hidden w-full">
                    <span className="font-label-md text-label-md text-on-surface whitespace-nowrap truncate">{profile.storeName}</span>
                    <span className="text-[10px] text-on-surface-variant truncate whitespace-nowrap">{profile.email}</span>
                  </div>
                </>
              ) : (
                <>
                  <div className="w-8 h-8 rounded-full bg-surface-variant border border-outline-variant shrink-0 animate-pulse"></div>
                  <div className="flex flex-col overflow-hidden w-full gap-1">
                    <div className="h-3 bg-surface-variant rounded w-20 animate-pulse"></div>
                    <div className="h-2 bg-surface-variant rounded w-24 animate-pulse"></div>
                  </div>
                </>
              )}
            </div>
            <button 
              onClick={() => setIsLogoutModalOpen(true)}
              className="p-1.5 text-error hover:bg-error/10 rounded-full transition-colors flex shrink-0 ml-1"
              title="Logout"
            >
              <span className="material-symbols-outlined text-[18px]">logout</span>
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-2">
            {profile ? (
              <img alt="Seller Profile" className="w-10 h-10 mx-auto rounded-full object-cover border border-outline-variant shrink-0 bg-surface-variant shadow-sm" src={profile.storeImage || 'https://ui-avatars.com/api/?name=Store'} title={profile.storeName} />
            ) : (
              <div className="w-10 h-10 mx-auto rounded-full bg-surface-variant border border-outline-variant shrink-0 animate-pulse shadow-sm"></div>
            )}
          </div>
        )}
      </div>
    </nav>

    {/* Logout Confirmation Modal - Placed outside <nav> to escape stacking context */}
    {isLogoutModalOpen && (
      <div className="fixed inset-0 bg-on-background/30 backdrop-blur-sm z-[999] flex items-center justify-center p-4">
        <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-6 max-w-sm w-full shadow-lg">
          <h3 className="text-headline-md font-headline-md text-on-surface mb-2">Confirm Logout</h3>
          <p className="text-body-md font-body-md text-on-surface-variant mb-6">Are you sure you want to log out? You will need to sign in again to access the dashboard.</p>
          <div className="flex justify-end gap-3">
            <button 
              onClick={() => setIsLogoutModalOpen(false)}
              className="px-4 py-2 rounded text-on-surface-variant hover:bg-surface-container-high transition-colors font-label-md text-label-md"
            >
              Cancel
            </button>
            <button 
              onClick={handleLogout}
              className="px-4 py-2 rounded bg-error text-on-error hover:opacity-90 transition-opacity font-label-md text-label-md flex items-center gap-2"
            >
              <span className="material-symbols-outlined text-[18px]">logout</span>
              Logout
            </button>
          </div>
        </div>
      </div>
    )}
    </>
  );
}
