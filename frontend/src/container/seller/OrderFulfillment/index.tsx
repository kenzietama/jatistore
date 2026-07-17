import { SellerLayout } from "../../../components/layout/seller/SellerLayout";
import { OrderTable } from "../../../components/seller/order/OrderTable";

export default function OrderFulfillment() {
  return (
    <SellerLayout>
      <header className="flex justify-between items-center w-full mt-stack-md mb-stack-md">
        <h2 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Order Fulfillment</h2>
      </header>

      <OrderTable />
    </SellerLayout>
  );
}
