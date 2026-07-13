import { SellerLayout } from "../../../components/layout/seller/SellerLayout";
import { OrderTable } from "../../../components/seller/order/OrderTable";

export default function OrderFulfillment() {
  return (
    <SellerLayout>
      <header className="mb-gutter flex justify-between items-end border-b border-outline-variant pb-stack-sm">
        <div>
          <h1 className="font-headline-lg text-headline-lg text-on-surface">Order Fulfillment</h1>
        </div>
        {/* Tabs - can be added later if needed */}
        <div className="flex gap-stack-md"></div>
      </header>

      <OrderTable />
    </SellerLayout>
  );
}
