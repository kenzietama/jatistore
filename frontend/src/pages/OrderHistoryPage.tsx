import React, { useState } from "react";

interface OrderItem {
  id: string;
  orderNumber: string;
  datePlaced: string;
  totalAmount: number;
  status: "Shipped" | "Received";
  productName: string;
  variant: string;
  quantity: number;
  image: string;
}

interface OrderHistoryPageProps {
  onNavigateHome: () => void;
  onNavigateCart: () => void;
  cartCount: number;
}

const OrderHistoryPage: React.FC<OrderHistoryPageProps> = ({
  onNavigateHome,
  onNavigateCart,
  cartCount,
}) => {
  const [orders, setOrders] = useState<OrderItem[]>(() => {
  const savedOrders = localStorage.getItem("jatistore_orders");
  if (savedOrders) {
    return JSON.parse(savedOrders);
  }
  return [
    {
      id: "1",
      orderNumber: "#JS-9921",
      datePlaced: "Oct 24, 2023",
      totalAmount: 89.0,
      status: "Shipped",
      productName: "Artisan Ceramic Mug",
      variant: "Matte Teal",
      quantity: 2,
      image: "https://lh3.googleusercontent.com/aida-public/AB6AXuBUsnQhrXHjvu1hHH7p9iNaeWc72KlCPoUKnQV_abA1Q8EkpIW6RJDP6nYlnmEAdt56iZjOvvGgqCYZEe72gua020qr0zxUSnxX1RpCviPjX9xOouqA9ICrbNuyrSHAhZLhE6P6BVeqbmKYKdOyFEhz5ULev16uGhyQ8W3F_jmuWyxHFN4ykqkMIs7xosWEu_23vRfxM5dylVsgcn2KJ3NP8_h8vt1dXvIoWBrrOOYaYw2QAoq1dgx2tFynJV5eEUwATazxOLAHXV-u",
    },
    {
      id: "2",
      orderNumber: "#JS-8821",
      datePlaced: "Oct 12, 2023",
      totalAmount: 145.5,
      status: "Received",
      productName: "Woven Cotton Throw",
      variant: "Oat",
      quantity: 1,
      image: "https://lh3.googleusercontent.com/aida-public/AB6AXuDcNUi9GXk7sjcu9IbCfmXmBzyVXwYMm07lcrjAswQanSZY8g6AezFPGWKhxhGOjnls3XxD7lThH0Iz9OYaIPX8ZDcnP4_5EyZ3094s-mF3RG9INW0x056S0cZD2xnKJ4kpqu32YFGEeJAatIVqcLWAkL2zOwszhoGM8eM3tVKzLIAW7x1w42bu0Kw3qBExHHrQ_y-0x5GqawwTvqRRtRWJP8108VUKWRO4ittpAv9PuUGLz1LN_UWWw0y9jNYhSKxitix2HspA3x-F",
    }
  ];
});

  const [filterShipped, setFilterShipped] = useState(true);
  const [filterReceived, setFilterReceived] = useState(true);
  const [timeframe, setTimeframe] = useState("Last 30 Days");
  const [searchQuery, setSearchQuery] = useState("");
  
  const [processingOrderId, setProcessingOrderId] = useState<string | null>(null);
  const [showToast, setShowToast] = useState(false);
  const [toastAnimationClass, setToastAnimationClass] = useState("toast-enter");

//   const handleReceiveOrder = (orderId: string) => {
//     setProcessingOrderId(orderId);

//     setTimeout(() => {
//       setOrders((prevOrders) =>
//         prevOrders.map((order) =>
//           order.id === orderId ? { ...order, status: "Received" } : order
//         )
//       );
//       setProcessingOrderId(null);
//       triggerToast();
//     }, 1500);
//   };
    const handleReceiveOrder = (orderId: string) => {
    setProcessingOrderId(orderId);

    setTimeout(() => {
        setOrders((prevOrders) => {
        const updated = prevOrders.map((order) =>
            order.id === orderId ? { ...order, status: "Received" as const } : order
        );
        localStorage.setItem("jatistore_orders", JSON.stringify(updated));
        return updated;
        });
        
        setProcessingOrderId(null);
        triggerToast();
    }, 1500);
    };

  const triggerToast = () => {
    setShowToast(true);
    setToastAnimationClass("toast-enter");

    const timer = setTimeout(() => {
      handleCloseToast();
    }, 5000);

    return () => clearTimeout(timer);
  };

  const handleCloseToast = () => {
    setToastAnimationClass("toast-exit");
    setTimeout(() => {
      setShowToast(false);
    }, 300);
  };

  // Filter memproses data mock di atas secara real-time
  const filteredOrders = orders.filter((order) => {
    const matchesStatus =
      (order.status === "Shipped" && filterShipped) ||
      (order.status === "Received" && filterReceived);

    const matchesSearch =
      order.productName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      order.orderNumber.toLowerCase().includes(searchQuery.toLowerCase());

    return matchesStatus && matchesSearch;
  });

  return (
    <div className="bg-background text-on-background font-body-md min-h-screen flex flex-col relative overflow-x-hidden">
      
      {/* TopNavBar */}
      <header className="bg-surface border-b border-outline-variant shadow-sm w-full sticky top-0 z-50 transition-all duration-300">
        <div className="flex justify-between items-center w-full px-margin-desktop max-w-container-max mx-auto h-16">
          <button 
            onClick={onNavigateHome}
            className="text-headline-md font-headline-lg font-bold text-primary flex items-center gap-2 hover:opacity-90"
          >
            <span className="material-symbols-outlined text-[32px]">storefront</span>
            JatiStore
          </button>

          {/* Search Bar terikat ke state searchQuery */}
          <div className="hidden md:flex items-center flex-1 max-w-md mx-8 relative">
            <span className="material-symbols-outlined absolute left-3 text-outline">search</span>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full h-10 pl-10 pr-4 rounded-full border border-outline-variant bg-surface-container-lowest focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 text-body-sm font-body-sm transition-all text-on-surface"
              placeholder="Search products or order #..."
            />
          </div>

          <nav className="flex items-center gap-6">
            <div className="hidden md:flex items-center gap-6 font-label-md text-label-md">
              <button onClick={onNavigateHome} className="text-on-surface-variant hover:text-primary transition-colors">Home</button>
              <button className="text-on-surface-variant hover:text-primary transition-colors">Categories</button>
              <span className="text-primary border-b-2 border-primary pb-1 font-bold opacity-80 cursor-default">Orders</span>
            </div>

            <div className="flex items-center gap-4 text-on-surface-variant">
              <button 
                onClick={onNavigateCart}
                aria-label="Shopping Cart" 
                className="hover:text-primary transition-colors relative"
              >
                <span className="material-symbols-outlined">shopping_cart</span>
                {cartCount > 0 && (
                  <span className="absolute -top-1 -right-1 bg-error text-on-error text-[10px] font-bold px-1.5 py-0.5 rounded-full">
                    {cartCount}
                  </span>
                )}
              </button>
              <button aria-label="Account Profile" className="hover:text-primary transition-colors">
                <span className="material-symbols-outlined">account_circle</span>
              </button>
              <button aria-label="Menu" className="md:hidden hover:text-primary transition-colors">
                <span className="material-symbols-outlined">menu</span>
              </button>
            </div>
          </nav>
        </div>
      </header>

      {/* Main Content Canvas */}
      <main className="flex-1 w-full max-w-container-max mx-auto px-margin-mobile md:px-margin-desktop py-stack-lg">
        <div className="mb-stack-lg">
          <h1 className="text-display-lg font-display-lg text-on-background mb-unit">Order History</h1>
          <p className="text-body-lg font-body-lg text-on-surface-variant">Track, manage, and review your recent purchases.</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
          
          {/* Filter Sidebar */}
          <aside className="lg:col-span-3">
            <div className="bg-surface-container-lowest border border-outline-variant rounded-xl p-stack-md sticky top-24">
              <h3 className="text-headline-md font-headline-md text-on-surface mb-stack-sm border-b border-outline-variant pb-2">Filters</h3>
              <div className="space-y-4">
                
                <div>
                  <label className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wider block mb-2">Status</label>
                  <div className="flex flex-col gap-2">
                    <label className="flex items-center gap-2 cursor-pointer group">
                      <input
                        type="checkbox"
                        checked={filterShipped}
                        onChange={(e) => setFilterShipped(e.target.checked)}
                        className="rounded border-outline-variant text-primary focus:ring-primary w-4 h-4"
                      />
                      <span className="text-body-sm font-body-sm text-on-surface group-hover:text-primary transition-colors">Shipped</span>
                    </label>
                    <label className="flex items-center gap-2 cursor-pointer group">
                      <input
                        type="checkbox"
                        checked={filterReceived}
                        onChange={(e) => setFilterReceived(e.target.checked)}
                        className="rounded border-outline-variant text-primary focus:ring-primary w-4 h-4"
                      />
                      <span className="text-body-sm font-body-sm text-on-surface group-hover:text-primary transition-colors">Received</span>
                    </label>
                  </div>
                </div>

                <div>
                  <label className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wider block mb-2">Timeframe</label>
                  <select 
                    value={timeframe}
                    onChange={(e) => setTimeframe(e.target.value)}
                    className="w-full border border-outline-variant rounded-lg h-10 px-3 text-body-sm font-body-sm bg-surface-container-lowest text-on-surface focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
                  >
                    <option>Last 30 Days</option>
                    <option>Last 3 Months</option>
                    <option>2023</option>
                  </select>
                </div>

              </div>
            </div>
          </aside>

          {/* Order Cards List */}
          <div className="lg:col-span-9 space-y-stack-md">
            {filteredOrders.length > 0 ? (
              filteredOrders.map((order) => (
                <div 
                  key={order.id} 
                  className={`bg-surface-container-lowest border border-outline-variant rounded-xl overflow-hidden hover:shadow-md transition-all duration-300 ${
                    order.status === "Received" ? "opacity-95" : ""
                  }`}
                >
                  {/* Order Header */}
                  <div className="bg-surface-container-low px-stack-md py-stack-sm border-b border-outline-variant flex flex-wrap justify-between items-center gap-4">
                    <div className="flex flex-wrap items-center gap-6">
                      <div>
                        <p className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wider">Order Number</p>
                        <p className="text-body-md font-mono-data font-medium text-on-surface">{order.orderNumber}</p>
                      </div>
                      <div>
                        <p className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wider">Date Placed</p>
                        <p className="text-body-md font-body-md text-on-surface">{order.datePlaced}</p>
                      </div>
                      <div>
                        <p className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wider">Total Amount</p>
                        <p className="text-body-md font-body-md font-bold text-on-surface">
                          ${order.totalAmount.toFixed(2)}
                        </p>
                      </div>
                    </div>

                    {order.status === "Shipped" ? (
                      <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-secondary-container/20 border border-secondary text-on-secondary-container">
                        <span className="material-symbols-outlined text-[16px]">local_shipping</span>
                        <span className="text-label-sm font-label-sm font-bold uppercase tracking-wider">Shipped</span>
                      </div>
                    ) : (
                      <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-primary-container/10 border border-primary text-primary">
                        <span className="material-symbols-outlined text-[16px]">task_alt</span>
                        <span className="text-label-sm font-label-sm font-bold uppercase tracking-wider">Received</span>
                      </div>
                    )}
                  </div>

                  {/* Order Card Body */}
                  <div className="p-stack-md flex flex-col sm:flex-row gap-stack-md items-start sm:items-center">
                    <div className="flex-shrink-0 w-24 h-24 rounded-lg overflow-hidden bg-surface-container border border-outline-variant">
                      <img className="w-full h-full object-cover" src={order.image} alt={order.productName} />
                    </div>
                    
                    <div className="flex-1">
                      <h4 className="text-body-lg font-headline-md text-on-surface mb-1">{order.productName}</h4>
                      <p className="text-body-sm font-body-sm text-on-surface-variant mb-2">
                        Variant: {order.variant} | Qty: {order.quantity}
                      </p>
                      <button className="text-primary font-label-md text-label-md hover:underline inline-flex items-center gap-1">
                        View Product <span className="material-symbols-outlined text-[14px]">arrow_forward</span>
                      </button>
                    </div>

                    {/* Tombol Aksi */}
                    <div className="w-full sm:w-auto mt-4 sm:mt-0 flex flex-col gap-2">
                      {order.status === "Shipped" ? (
                        <>
                          <button
                            onClick={() => handleReceiveOrder(order.id)}
                            disabled={processingOrderId !== null}
                            className={`w-full sm:w-auto bg-primary hover:bg-primary-container text-on-primary font-label-md text-label-md py-2.5 px-6 rounded-lg transition-colors flex items-center justify-center gap-2 shadow-sm ${
                              processingOrderId === order.id ? "opacity-70 cursor-not-allowed" : ""
                            }`}
                          >
                            <span className="material-symbols-outlined text-[18px]">
                              {processingOrderId === order.id ? "hourglass_empty" : "check_circle"}
                            </span>
                            {processingOrderId === order.id ? "Processing..." : "Pesanan Diterima"}
                          </button>
                          <button className="w-full sm:w-auto border border-outline hover:bg-surface-container-low text-on-surface font-label-md text-label-md py-2.5 px-6 rounded-lg transition-colors flex items-center justify-center gap-2">
                            Track Package
                          </button>
                        </>
                      ) : (
                        <>
                          <button className="w-full sm:w-auto border border-primary text-primary hover:bg-primary-container/10 font-label-md text-label-md py-2.5 px-6 rounded-lg transition-colors flex items-center justify-center gap-2">
                            <span className="material-symbols-outlined text-[18px]">rate_review</span>
                            Leave Review
                          </button>
                          <button className="w-full sm:w-auto text-on-surface-variant hover:text-on-surface font-label-md text-label-md py-2 px-4 rounded-lg transition-colors flex items-center justify-center gap-2">
                            Buy Again
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-12 bg-surface-container-lowest border border-outline-variant rounded-xl">
                <span className="material-symbols-outlined text-[48px] text-outline mb-2">order_play</span>
                <p className="text-on-surface-variant">No orders match your filter criteria.</p>
              </div>
            )}
          </div>

        </div>
      </main>

      <footer className="w-full bg-surface border-t border-outline-variant py-stack-lg mt-auto">
        <div className="max-w-container-max mx-auto px-margin-desktop text-center">
          <p className="font-label-sm text-label-sm text-on-surface-variant">© 2026 JatiStore. All rights reserved.</p>
        </div>
      </footer>

      {/* Toast Notification */}
      {showToast && (
        <div className="fixed bottom-6 right-6 z-50" id="toast-container">
          <div className={`bg-inverse-surface text-inverse-on-surface border border-outline px-6 py-4 rounded-xl shadow-lg flex items-center gap-3 max-w-sm ${toastAnimationClass}`}>
            <span className="material-symbols-outlined text-primary-fixed">check_circle</span>
            <div>
              <p className="font-label-md text-label-md font-bold">Action Successful</p>
              <p className="font-body-sm text-body-sm text-outline-variant">Thank you for confirming your order.</p>
            </div>
            <button 
              onClick={handleCloseToast}
              aria-label="Close notification" 
              className="ml-auto text-outline-variant hover:text-white transition-colors"
            >
              <span className="material-symbols-outlined text-[20px]">close</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderHistoryPage;