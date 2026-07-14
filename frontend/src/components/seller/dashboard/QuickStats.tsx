import { useEffect, useState } from 'react';
import { dashboardService } from '../../../service/seller/dashboard.service';
import type { DashboardStats } from '../../../service/seller/dashboard.service';

export function QuickStats() {
  const [stats, setStats] = useState<DashboardStats | null>(null);

  useEffect(() => {
    dashboardService.getStats()
      .then(setStats)
      .catch(console.error);
  }, []);

  return (
    <>
      {/* Total Products */}
      <div className="bg-white border border-[#e6e9eb] rounded-[10px] py-[18px] px-[20px] flex items-start gap-[12px] shadow-sm">
        <div className="w-[34px] h-[34px] shrink-0 rounded-[8px] bg-[#e6f6f4] text-[#0c7d73] flex items-center justify-center text-[16px]">
          📦
        </div>
        <div className="min-w-0">
          <p className="text-[12.5px] font-medium text-[#6b7876] m-0 mb-[6px] whitespace-nowrap">Total Products</p>
          <span className="text-[21px] font-bold text-[#14201e] leading-[1.1] tracking-[-0.01em] m-0">{stats ? stats.totalProducts : '...'}</span>
        </div>
      </div>

      {/* Total Orders */}
      <div className="bg-white border border-[#e6e9eb] rounded-[10px] py-[18px] px-[20px] flex items-start gap-[12px] shadow-sm">
        <div className="w-[34px] h-[34px] shrink-0 rounded-[8px] bg-[#e6f6f4] text-[#0c7d73] flex items-center justify-center text-[16px]">
          🛒
        </div>
        <div className="min-w-0">
          <p className="text-[12.5px] font-medium text-[#6b7876] m-0 mb-[6px] whitespace-nowrap">Total Orders</p>
          <span className="text-[21px] font-bold text-[#14201e] leading-[1.1] tracking-[-0.01em] m-0">{stats ? stats.totalOrders : '...'}</span>
        </div>
      </div>
    </>
  );
}
