import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../../store/auth/useAuthStore';
import { authService } from '../../../service/auth/authService';

interface AdminSidebarProps {
  isCollapsed: boolean;
  setIsCollapsed: (collapsed: boolean) => void;
  isMobileOpen: boolean;
  setIsMobileOpen: (open: boolean) => void;
}

export function AdminSidebar({ isCollapsed, setIsCollapsed, isMobileOpen, setIsMobileOpen }: AdminSidebarProps) {
  const widthClass = isCollapsed ? 'md:w-[88px]' : 'md:w-[240px]';
  const transformClass = isMobileOpen ? 'translate-x-0' : '-translate-x-full';

  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);
  const navigate = useNavigate();
  const logoutStore = useAuthStore((state) => state.logout);

  const handleLogout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error("Logout failed at server", error);
    } finally {
      logoutStore();
      setIsLogoutModalOpen(false);
      navigate('/auth/login');
    }
  };

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: 'admin_panel_settings', path: '/admin/dashboard', disabled: false },
    { id: 'flash_sale', label: 'Flash Sale Manager', icon: 'campaign', path: '/admin/flash-sales', disabled: false },
    { id: 'audit_trails', label: 'Audit Trails', icon: 'history_edu', path: '/admin/audit-trails', disabled: false }
  ];

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
              <span className="material-symbols-outlined" data-icon="admin_panel_settings" data-weight="fill">admin_panel_settings</span>
            </div>
            <h1 className="text-headline-sm font-bold text-primary dark:text-inverse-primary font-headline-sm whitespace-nowrap overflow-hidden">JatiStore Admin</h1>
          </div>
          <div>
            <span className="text-label-sm font-label-sm font-mono-data text-on-surface-variant px-2 py-1 rounded bg-surface-variant/50 inline-block">ADMIN</span>
          </div>
        </div>

        {/* Collapsed Logo (Only visible when collapsed on desktop) */}
        {isCollapsed && (
          <div 
            className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-on-primary mx-auto mb-unit shrink-0 hidden md:flex cursor-pointer hover:scale-110 transition-transform"
            onClick={() => setIsCollapsed(!isCollapsed)}
            title="Expand Sidebar"
          >
            <span className="material-symbols-outlined text-[20px]">admin_panel_settings</span>
          </div>
        )}

        {/* Mobile Close Button */}
        <button 
          onClick={() => setIsMobileOpen(false)}
          className="md:hidden p-unit text-on-surface-variant hover:bg-surface-variant rounded-full transition-colors"
        >
          <span className="material-symbols-outlined text-[20px]">close</span>
        </button>
      </div>

      {/* Main Navigation */}
      <div className="flex-1 overflow-y-auto">
        <div className="space-y-stack-sm pt-stack-md">

          {navItems.map((item) => (
            <NavLink
              key={item.id}
              to={item.path}
              onClick={(e) => {
                if (item.disabled) {
                  e.preventDefault();
                  return;
                }
                if (window.innerWidth < 768) setIsMobileOpen(false);
              }}
              title={isCollapsed ? item.label : undefined}
              className={({ isActive }) => 
                `w-full flex items-center gap-stack-sm p-stack-sm rounded transition-colors group relative ${
                  item.disabled ? 'opacity-50 cursor-not-allowed' : ''
                } ${
                  isActive && !item.disabled
                    ? 'bg-primary-container text-on-primary-container' 
                    : 'text-on-surface hover:bg-surface-variant hover:text-on-surface-variant'
                } ${isCollapsed ? 'justify-center' : ''}`
              }
            >
              {({ isActive }) => (
                <>
                  {isActive && !item.disabled && (
                    <div className="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-1/2 bg-primary rounded-r-full"></div>
                  )}
                  
                  <span className={`material-symbols-outlined shrink-0 ${isActive && !item.disabled ? 'text-primary' : ''}`} data-icon={item.icon}>
                    {item.icon}
                  </span>
                  {!isCollapsed && (
                    <span className="font-label-md text-label-md truncate">{item.label}</span>
                  )}
                </>
              )}
            </NavLink>
          ))}
        </div>
      </div>

      {/* User Area */}
      <div className="mt-auto pt-stack-md border-t border-outline-variant">
        {!isCollapsed ? (
          <div className="flex items-center justify-between p-stack-sm bg-surface-variant rounded">
            <div className="flex items-center gap-stack-sm overflow-hidden">
              <div className="w-8 h-8 rounded-full bg-secondary-container text-on-secondary-container flex items-center justify-center font-title-sm shrink-0">
                AD
              </div>
              <div className="flex flex-col overflow-hidden">
                <span className="font-label-md text-label-md text-on-surface truncate">Admin</span>
                <span className="text-[10px] text-on-surface-variant truncate">admin@jatistore.com</span>
              </div>
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
            <div className="w-10 h-10 mx-auto rounded-full bg-secondary-container text-on-secondary-container flex items-center justify-center font-title-md shadow-sm" title="Admin">
              AD
            </div>
          </div>
        )}
      </div>
    </nav>

    {/* Logout Confirmation Modal */}
    {isLogoutModalOpen && (
      <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
        <div className="bg-surface-container-lowest rounded-xl shadow-lg border border-outline-variant overflow-hidden w-full max-w-sm flex flex-col transform transition-all">
          <div className="p-stack-md border-b border-outline-variant flex items-center gap-2">
            <span className="material-symbols-outlined text-error">logout</span>
            <h3 className="font-title-md text-title-md text-on-surface m-0">Confirm Logout</h3>
          </div>
          
          <div className="p-stack-md">
            <p className="text-body-md text-on-surface-variant m-0">
              Are you sure you want to logout from the Admin Dashboard?
            </p>
          </div>
          
          <div className="p-stack-md border-t border-outline-variant bg-surface flex justify-end gap-stack-sm rounded-b-xl">
            <button 
              onClick={() => setIsLogoutModalOpen(false)}
              className="px-4 py-2 rounded font-label-md text-label-md text-on-surface-variant hover:bg-surface-variant transition-colors"
            >
              Cancel
            </button>
            <button 
              onClick={handleLogout}
              className="px-4 py-2 rounded bg-error text-on-error font-label-md text-label-md hover:opacity-90 transition-opacity"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    )}
    </>
  );
}
