import { SellerLayout } from "../../../components/layout/seller/SellerLayout";
import { OrderTable } from "../../../components/seller/order/OrderTable";

export default function OrderFulfillment() {
  return (
    <SellerLayout>
      <div className="w-full h-full flex flex-col gap-stack-lg min-h-[calc(100vh-100px)]">
        <header className="flex justify-between items-center w-full mt-stack-md">
          <h2 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Order Fulfillment</h2>
        </header>

        <OrderTable />
      </div>
    </SellerLayout>
  );
}
