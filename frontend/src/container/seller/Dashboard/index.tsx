
import { SellerLayout } from '../../../components/layout/seller/SellerLayout';
import { WelcomeSection } from '../../../components/seller/dashboard/WelcomeSection';
import { FinancialOverview } from '../../../components/seller/dashboard/FinancialOverview';
import { QuickStats } from '../../../components/seller/dashboard/QuickStats';
import { RecentOrders } from '../../../components/seller/dashboard/RecentOrders';

export default function Dashboard() {
  return (
    <SellerLayout>
      <div className="w-full h-full flex flex-col gap-stack-lg min-h-[calc(100vh-100px)] py-stack-md">
        <WelcomeSection />
        
        {/* Summary Cards 1x4 Grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-gutter mb-stack-md shrink-0">
          <FinancialOverview />
          <QuickStats />
        </div>

        <RecentOrders />
      </div>
    </SellerLayout>
  );
}
