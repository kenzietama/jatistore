import { useEffect, useState } from 'react';
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
    <section className="mb-stack-md">
      <h1 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Welcome back, {stats ? stats.sellerName : 'Store Owner'}.</h1>
    </section>
  );
}
