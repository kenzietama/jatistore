import { useEffect, useState } from "react";
import { Outlet, NavLink } from "react-router-dom";
import api from "../../../lib/api";

export function UserLayout() {
  const [userProfile, setUserProfile] = useState<any>(null);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get("/api/v1/user/profile");
      if (response.data && (response.data.restApiResponseHttpCode === 200 || response.data.code === 200)) {
        setUserProfile(response.data.restApiResponseData || response.data.data);
      }
    } catch (error) {
      console.error("Failed to fetch user profile", error);
    }
  };

  const getNavLinkClass = ({ isActive }: { isActive: boolean }) => {
    const baseClass = "flex items-center gap-3 px-4 py-3 rounded-xl transition-all ";
    if (isActive) {
      return baseClass + "bg-primary text-on-primary shadow-md";
    }
    return baseClass + "text-on-surface-variant hover:bg-surface-container group";
  };

  const getIconClass = ({ isActive }: { isActive: boolean }) => {
    if (isActive) {
      return "material-symbols-outlined active-fill";
    }
    return "material-symbols-outlined group-hover:text-primary transition-colors";
  };

  return (
    <div className="flex flex-col min-h-screen bg-background text-on-background">
      <main className="flex-grow w-full max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop py-stack-lg">
        <div className="flex flex-col md:flex-row gap-gutter">
          
          {/* Sidebar Navigation */}
          <aside className="w-full md:w-[260px] flex-shrink-0">
            <div className="md:sticky top-24 space-y-6">
              <div className="px-4">
                {userProfile ? (
                  <h2 className="text-2xl font-bold text-on-surface mb-1 truncate" title={`Hi, ${userProfile.fullName}`}>Hi, {userProfile.fullName}</h2>
                ) : (
                  <h2 className="text-2xl font-bold text-on-surface mb-1">Hi</h2>
                )}
              </div>
              
              <nav className="flex flex-col gap-1 px-2">
                <NavLink to="/user/profile" className={getNavLinkClass}>
                  {({ isActive }) => (
                    <>
                      <span className={getIconClass({ isActive })}>person</span>
                      <span className="font-label-md">Profile</span>
                    </>
                  )}
                </NavLink>

                <NavLink to="/user/financials" className={getNavLinkClass}>
                  {({ isActive }) => (
                    <>
                      <span className={getIconClass({ isActive })}>account_balance_wallet</span>
                      <span className="font-label-md">User Financials</span>
                    </>
                  )}
                </NavLink>

                <NavLink to="/user/orders" className={getNavLinkClass}>
                  {({ isActive }) => (
                    <>
                      <span className={getIconClass({ isActive })}>history</span>
                      <span className="font-label-md">Order History</span>
                    </>
                  )}
                </NavLink>
              </nav>
            </div>
          </aside>

          {/* Main Content Area */}
          <div className="flex-grow w-full min-w-0">
            <Outlet context={{ refreshGlobalProfile: fetchProfile }} />
          </div>
          
        </div>
      </main>

      <footer className="w-full bg-surface border-t border-outline-variant py-stack-lg mt-auto">
        <div className="max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop text-center">
          <p className="font-label-sm text-label-sm text-on-surface-variant">
            © 2026 JatiStore. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}
