import { useState } from 'react';

type OrderStatus = 'PENDING' | 'SHIPPED' | 'ON_HOLD' | 'RECEIVED';

interface OrderData {
  id: string;
  buyerInitials: string;
  buyerName: string;
  buyerColorClass: string;
  productsDesc: string;
  sku?: string;
  earnings: number;
  status: OrderStatus;
}

const dummyOrders: OrderData[] = [
  {
    id: 'ORD-99321',
    buyerInitials: 'JD',
    buyerName: 'Jane Doe',
    buyerColorClass: 'bg-secondary-container text-on-secondary-container',
    productsDesc: 'Premium Widget (x2)',
    sku: 'WIDG-001',
    earnings: 2250000,
    status: 'PENDING',
  },
  {
    id: 'ORD-99320',
    buyerInitials: 'MS',
    buyerName: 'Michael Smith',
    buyerColorClass: 'bg-tertiary-container text-on-tertiary-container',
    productsDesc: 'Basic Gadget (x1)',
    earnings: 450000,
    status: 'SHIPPED',
  },
  {
    id: 'ORD-99318',
    buyerInitials: 'AL',
    buyerName: 'Amanda Lee',
    buyerColorClass: 'bg-error-container text-on-error-container',
    productsDesc: 'Super Doohickey (x5)',
    earnings: 7500000,
    status: 'ON_HOLD',
  },
  {
    id: 'ORD-99315',
    buyerInitials: 'RJ',
    buyerName: 'Robert Johnson',
    buyerColorClass: 'bg-primary-container text-on-primary-container',
    productsDesc: 'Widget Pro Max (x1)',
    earnings: 13500000,
    status: 'RECEIVED',
  },
];

export function OrderTable() {
  const [orders, setOrders] = useState<OrderData[]>(dummyOrders);
  const [filterOpen, setFilterOpen] = useState(false);
  const [sortOpen, setSortOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');
  const [sortOpt, setSortOpt] = useState('');

  const getStatusBadge = (status: OrderStatus) => {
    switch (status) {
      case 'PENDING':
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-tertiary bg-tertiary/10 text-tertiary font-label-sm text-label-sm uppercase">Pending</span>;
      case 'SHIPPED':
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-secondary bg-secondary/10 text-secondary font-label-sm text-label-sm uppercase">Shipped</span>;
      case 'ON_HOLD':
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-error bg-error/10 text-error font-label-sm text-label-sm uppercase">On Hold</span>;
      case 'RECEIVED':
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-primary bg-primary/10 text-primary font-label-sm text-label-sm uppercase">Received</span>;
      default:
        return null;
    }
  };

  const handleMarkShipped = (orderId: string) => {
    setOrders((prev) =>
      prev.map((o) => (o.id === orderId ? { ...o, status: 'SHIPPED' } : o))
    );
  };

  const filteredOrders = orders.filter(o => statusFilter === '' || o.status === statusFilter);

  return (
    <>
      {/* Search and Filters */}
      <div className="flex items-center gap-stack-md bg-surface-container-lowest p-stack-sm rounded-lg border border-outline-variant shadow-sm w-full mb-stack-lg">
        <div className="flex-grow flex items-center relative">
          <span className="material-symbols-outlined absolute left-stack-sm text-on-surface-variant">search</span>
          <input 
            className="w-full pl-10 pr-stack-sm py-stack-sm bg-transparent border-none focus:ring-0 font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant focus:outline-none" 
            placeholder="Search orders by ID, buyer, or product..." 
            type="text" 
          />
        </div>
        <div className="h-6 w-px bg-outline-variant"></div>
        <div className="relative">
          <button 
            className="flex items-center gap-unit px-stack-sm py-stack-sm font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors rounded hover:bg-surface-variant"
            onClick={() => { setFilterOpen(!filterOpen); setSortOpen(false); }}
          >
            <span className="material-symbols-outlined text-[18px]">filter_list</span>
            Filters {statusFilter && <span className="w-2 h-2 rounded-full bg-primary ml-1"></span>}
          </button>
          
          {filterOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-surface-container-lowest border border-outline-variant rounded shadow-lg z-50 py-2">
              <div className="px-4 py-1 text-label-sm font-label-sm text-on-surface-variant">Filter by Status</div>
              {['', 'PENDING', 'SHIPPED', 'ON_HOLD', 'RECEIVED'].map((s) => (
                <button
                  key={s}
                  className={`w-full text-left px-4 py-2 font-body-sm text-body-sm hover:bg-surface-variant/50 transition-colors ${statusFilter === s ? 'text-primary bg-primary/10 font-medium' : 'text-on-surface'}`}
                  onClick={() => { setStatusFilter(s); setFilterOpen(false); }}
                >
                  {s === '' ? 'All Status' : s.replace('_', ' ')}
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="relative">
          <button 
            className="flex items-center gap-unit px-stack-sm py-stack-sm font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors rounded hover:bg-surface-variant"
            onClick={() => { setSortOpen(!sortOpen); setFilterOpen(false); }}
          >
            <span className="material-symbols-outlined text-[18px]">sort</span>
            Sort {sortOpt && <span className="w-2 h-2 rounded-full bg-primary ml-1"></span>}
          </button>
          
          {sortOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-surface-container-lowest border border-outline-variant rounded shadow-lg z-50 py-2">
              <div className="px-4 py-1 text-label-sm font-label-sm text-on-surface-variant">Sort Orders</div>
              {[
                { label: 'Newest First', val: 'newest' },
                { label: 'Oldest First', val: 'oldest' },
                { label: 'Highest Earnings', val: 'highest' },
                { label: 'Lowest Earnings', val: 'lowest' }
              ].map((opt) => (
                <button
                  key={opt.val}
                  className={`w-full text-left px-4 py-2 font-body-sm text-body-sm hover:bg-surface-variant/50 transition-colors ${sortOpt === opt.val ? 'text-primary bg-primary/10 font-medium' : 'text-on-surface'}`}
                  onClick={() => { setSortOpt(opt.val); setSortOpen(false); }}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-surface-container-lowest rounded-lg border border-outline-variant overflow-hidden shadow-sm flex flex-col h-[calc(100vh-280px)]">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse whitespace-nowrap">
            <thead>
              <tr className="bg-surface-container text-on-surface-variant border-b border-outline-variant">
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider">Order ID</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider">Buyer</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider">Products &amp; Qty</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider text-right">Earnings</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider text-center">Status</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider text-right">Action</th>
              </tr>
            </thead>
            <tbody className="font-body-sm text-body-sm">
              {filteredOrders.map((order, idx) => (
                <tr key={order.id} className={`border-b border-outline-variant hover:bg-surface-variant/30 transition-colors group ${idx % 2 !== 0 ? 'bg-surface-container-low/30' : ''}`}>
                  <td className="p-stack-md font-mono-data text-mono-data text-on-surface">#{order.id}</td>
                  <td className="p-stack-md text-on-surface-variant">
                    <div className="flex items-center gap-stack-sm">
                      <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${order.buyerColorClass}`}>
                        {order.buyerInitials}
                      </div>
                      <span>{order.buyerName}</span>
                    </div>
                  </td>
                  <td className="p-stack-md text-on-surface-variant">
                    {order.productsDesc}<br/>
                    {order.sku && <span className="text-xs text-outline">SKU: {order.sku}</span>}
                  </td>
                  <td className="p-stack-md font-mono-data text-mono-data text-right text-primary-container font-medium">
                    Rp {order.earnings.toLocaleString('id-ID')}
                  </td>
                  <td className="p-stack-md text-center">
                    {getStatusBadge(order.status)}
                  </td>
                  <td className="p-stack-md text-right">
                    <div className="flex justify-end items-center gap-stack-sm">
                      {order.status === 'PENDING' && (
                        <button 
                          className="bg-primary text-on-primary px-3 py-1 rounded text-xs font-label-md hover:bg-surface-tint transition-colors"
                          onClick={() => handleMarkShipped(order.id)}
                        >
                          Mark as Shipped
                        </button>
                      )}
                      
                      {order.status === 'RECEIVED' && (
                        <span className="px-2 py-1 text-xs font-label-md text-primary font-medium flex items-center gap-1">
                           <span className="material-symbols-outlined text-[16px]">check_circle</span>
                           Received
                        </span>
                      )}

                      <button className="text-on-surface-variant hover:text-primary transition-colors p-1 rounded hover:bg-surface-variant flex items-center justify-center" title="View Details">
                        <span className="material-symbols-outlined text-[20px]">visibility</span>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              
              {filteredOrders.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-stack-lg text-center text-on-surface-variant">
                    No orders found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="mt-auto border-t border-outline-variant p-stack-sm flex items-center justify-between bg-surface-container-low">
          <span className="font-label-sm text-label-sm text-on-surface-variant">Showing 1-{filteredOrders.length} of {filteredOrders.length} orders</span>
          <div className="flex items-center gap-unit">
            <button className="p-1 rounded text-on-surface-variant hover:bg-surface-variant disabled:opacity-50 transition-colors" disabled>
              <span className="material-symbols-outlined text-[20px]">chevron_left</span>
            </button>
            <button className="w-8 h-8 rounded bg-primary-container text-on-primary-container font-label-md text-label-md flex items-center justify-center">1</button>
            <button className="p-1 rounded text-on-surface-variant hover:bg-surface-variant transition-colors disabled:opacity-50" disabled>
              <span className="material-symbols-outlined text-[20px]">chevron_right</span>
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
