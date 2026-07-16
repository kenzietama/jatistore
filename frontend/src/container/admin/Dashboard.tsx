import { useEffect, useState } from 'react';
import { adminService } from '../../service/admin/admin.service';
import type { AdminDashboardResponse } from '../../service/admin/admin.service';
import { SellerManagement } from './SellerManagement';
import { CategoryManagement } from './CategoryManagement';

export function Dashboard() {
  const [data, setData] = useState<AdminDashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const response = await adminService.getDashboardSummary();
        setData(response);
      } catch (error) {
        console.error("Failed to fetch admin dashboard data", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center bg-surface">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  const stats = [
    { label: 'Total Users', value: data?.totalUsers || 0, icon: 'group' },
    { label: 'Total Sellers', value: data?.totalSellers || 0, icon: 'storefront' },
    { label: 'Active Products', value: data?.totalProducts || 0, icon: 'inventory_2' },
    { label: 'Successful Transactions', value: data?.totalTransactions || 0, icon: 'receipt_long' },
  ];

  return (
    <div className="space-y-10 h-full">
      <header className="flex justify-between items-center w-full mt-stack-md mb-stack-md">
        <h1 className="font-headline-lg text-[32px] font-bold text-on-surface m-0 mb-stack-md">Dashboard</h1>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter">
        {stats.map((stat, idx) => (
          <div key={idx} className="bg-surface-container-lowest border border-outline-variant rounded p-stack-md flex items-center gap-stack-md hover:bg-surface-container-low transition-colors">
            <div className="w-12 h-12 rounded-full bg-primary-container text-on-primary-container flex items-center justify-center shrink-0">
              <span className="material-symbols-outlined">{stat.icon}</span>
            </div>
            <div>
              <p className="font-label-md text-label-md text-on-surface-variant">{stat.label}</p>
              <p className="font-headline-md text-headline-md text-on-surface font-mono-data tracking-tight">
                {stat.value.toLocaleString()}
              </p>
            </div>
          </div>
        ))}
      </div>
      
      {/* Sub-panels rendered as sections */}
      <SellerManagement />
      <CategoryManagement />
    </div>
  );
}
