import React from 'react';
import { SellerLayout } from '../../../components/layout/seller/SellerLayout';
import { WelcomeSection } from '../../../components/seller/dashboard/WelcomeSection';
import { FinancialOverview } from '../../../components/seller/dashboard/FinancialOverview';
import { QuickStats } from '../../../components/seller/dashboard/QuickStats';
import { RecentOrders } from '../../../components/seller/dashboard/RecentOrders';

export default function Dashboard() {
  return (
    <SellerLayout>
      <WelcomeSection />
      
      {/* Bento Grid Layout */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-gutter mb-stack-md">
        <FinancialOverview />
        <QuickStats />
      </div>

      <RecentOrders />
    </SellerLayout>
  );
}
