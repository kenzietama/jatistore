import React, { useEffect, useState } from 'react';
import { dashboardService } from '../../../service/seller/dashboard.service';
import type { DashboardStats } from '../../../service/seller/dashboard.service';

export function WelcomeSection() {
  const [stats, setStats] = useState<DashboardStats | null>(null);

  useEffect(() => {
    dashboardService.getStats()
      .then(setStats)
      .catch(console.error);
  }, []);

  return (
    <section className="mb-[24px]">
      <div className="mb-[28px]">
        <h1 className="text-[26px] font-bold text-[#14201e] tracking-[-0.02em] m-0 mb-[4px]">Welcome back, {stats ? stats.sellerName : 'Store Owner'}.</h1>
        <p className="text-[14px] text-[#6b7876] m-0">Here's a quick overview of your store's performance today.</p>
      </div>
    </section>
  );
}
