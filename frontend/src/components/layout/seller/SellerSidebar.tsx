import { NavLink } from 'react-router-dom';

interface SellerSidebarProps {
  isCollapsed: boolean;
  setIsCollapsed: (v: boolean) => void;
  isMobileOpen: boolean;
  setIsMobileOpen: (v: boolean) => void;
}

export function SellerSidebar({ isCollapsed, setIsCollapsed, isMobileOpen, setIsMobileOpen }: SellerSidebarProps) {
  const widthClass = isCollapsed ? 'md:w-[88px]' : 'md:w-[240px]';
  const transformClass = isMobileOpen ? 'translate-x-0' : '-translate-x-full';

  return (
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
          <a 
            className={`flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-all ${isCollapsed ? 'justify-center' : ''}`} 
            href="#"
            title={isCollapsed ? "Orders" : undefined}
          >
            <span className="material-symbols-outlined shrink-0" data-icon="shopping_bag">shopping_bag</span>
            <span className={`font-label-md text-label-md whitespace-nowrap ${isCollapsed ? 'hidden' : 'block'}`}>Orders</span>
          </a>
        </li>
        <li>
          <a 
            className={`flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-all ${isCollapsed ? 'justify-center' : ''}`} 
            href="#"
            title={isCollapsed ? "Flash Sale" : undefined}
          >
            <span className="material-symbols-outlined shrink-0" data-icon="bolt">bolt</span>
            <span className={`font-label-md text-label-md whitespace-nowrap ${isCollapsed ? 'hidden' : 'block'}`}>Flash Sale</span>
          </a>
        </li>
        <li>
          <a 
            className={`flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-all ${isCollapsed ? 'justify-center' : ''}`} 
            href="#"
            title={isCollapsed ? "Financials" : undefined}
          >
            <span className="material-symbols-outlined shrink-0" data-icon="payments">payments</span>
            <span className={`font-label-md text-label-md whitespace-nowrap ${isCollapsed ? 'hidden' : 'block'}`}>Financials</span>
          </a>
        </li>
      </ul>

      {/* Bottom Profile */}
      <div className={`mt-auto pt-stack-md border-t border-outline-variant ${isCollapsed ? 'flex justify-center' : ''}`}>
        <div className={`flex items-center gap-stack-sm ${isCollapsed ? 'justify-center' : ''}`}>
          <img alt="Seller Profile" className="w-10 h-10 rounded-full object-cover border border-outline-variant shrink-0" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCkIs4AXIbS0ZETMjiEGEQ9nUPCyWmF3KB0zxowwVVxiVOyxu85qfo7ws7eo9Hsy8OL5FqO1z2AgGHrYnpiZdHE8kEpZUd33g4PKkvSE-5NhMSHnGPeyrGSDU7-SmWtdiItMyQlEAicDATJWbIel1PjpsMJzYLrG0daUsFgO5Rn4zq2ihS2SsYmDoHPiZl3gAIKdpHM8QTRE74f-C9-2zkL3mWb6QLfchLiqhpa8PS7-Bmv0YKhKArhlLyqSyvc7rBjfE83xhQaHfxf" />
          <div className={`flex flex-col overflow-hidden ${isCollapsed ? 'hidden' : 'flex'}`}>
            <span className="font-label-md text-label-md text-on-surface whitespace-nowrap truncate">Alex Mercer</span>
            <span className="font-label-sm text-label-sm text-on-surface-variant cursor-pointer hover:underline whitespace-nowrap">View Profile</span>
          </div>
        </div>
      </div>
    </nav>
  );
}
