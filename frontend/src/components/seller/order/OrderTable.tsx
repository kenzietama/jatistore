import { useState, useEffect } from 'react';
import { orderService } from '../../../service/seller/order.service';
import type { SellerOrder, PageData } from '../../../service/seller/order.service';

export function OrderTable() {
  const [data, setData] = useState<PageData<SellerOrder> | null>(null);
  const [filterOpen, setFilterOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const res = await orderService.getOrders(statusFilter, page, 10);
      setData(res);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [statusFilter, page]);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-tertiary bg-tertiary/10 text-tertiary font-label-sm text-label-sm uppercase">Pending</span>;
      case 'PAID_ON_HOLD':
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-error bg-error/10 text-error font-label-sm text-label-sm uppercase">On Hold</span>;
      case 'SHIPPED':
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-secondary bg-secondary/10 text-secondary font-label-sm text-label-sm uppercase">Shipped</span>;
      case 'RECEIVED':
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-primary bg-primary/10 text-primary font-label-sm text-label-sm uppercase">Received</span>;
      case 'CANCELLED':
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-outline bg-surface-variant text-on-surface-variant font-label-sm text-label-sm uppercase">Cancelled</span>;
      default:
        return <span className="inline-flex items-center px-2 py-1 rounded-full border border-outline bg-surface text-on-surface font-label-sm text-label-sm uppercase">{status}</span>;
    }
  };

  const getInitials = (name: string) => {
    if (!name) return '??';
    const parts = name.split(' ');
    if (parts.length > 1) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  const getAvatarColor = (name: string) => {
    const colors = ['bg-secondary-container text-on-secondary-container', 'bg-tertiary-container text-on-tertiary-container', 'bg-error-container text-on-error-container', 'bg-primary-container text-on-primary-container'];
    const index = name.length % colors.length;
    return colors[index];
  };

  const handleMarkShipped = async (orderId: string) => {
    if (confirm('Mark this order as shipped?')) {
      try {
        await orderService.markAsShipped(orderId);
        fetchOrders(); // Refresh table
      } catch (error) {
        console.error('Failed to update status', error);
        alert('Failed to mark as shipped. Status must be PAID_ON_HOLD.');
      }
    }
  };

  return (
    <>
      {/* Search and Filters */}
      <div className="flex items-center gap-stack-md bg-surface-container-lowest p-stack-sm rounded-lg border border-outline-variant shadow-sm w-full mb-stack-lg">
        <div className="flex-grow flex items-center relative">
          <span className="material-symbols-outlined absolute left-stack-sm text-on-surface-variant">search</span>
          <input 
            className="w-full pl-10 pr-stack-sm py-stack-sm bg-transparent border-none focus:ring-0 font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant" 
            placeholder="Search orders by ID, buyer, or product..." 
            type="text" 
            disabled
          />
        </div>
        <div className="h-6 w-px bg-outline-variant"></div>
        <div className="relative">
          <button 
            className="flex items-center gap-unit px-stack-sm py-stack-sm font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors"
            onClick={() => setFilterOpen(!filterOpen)}
          >
            <span className="material-symbols-outlined text-[18px]">filter_list</span>
            Filters {statusFilter && <span className="w-2 h-2 rounded-full bg-primary ml-1"></span>}
          </button>
          
          {filterOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-surface border border-outline-variant rounded shadow-lg z-50 py-2">
              <div className="px-4 py-1 text-label-sm text-on-surface-variant uppercase">Status</div>
              {['', 'PENDING', 'PAID_ON_HOLD', 'SHIPPED', 'RECEIVED', 'CANCELLED'].map((s) => (
                <button
                  key={s}
                  className={`w-full text-left px-4 py-2 font-body-sm text-body-sm hover:bg-surface-container transition-colors ${statusFilter === s ? 'text-primary bg-primary-container/10 font-medium' : 'text-on-surface'}`}
                  onClick={() => { setStatusFilter(s); setPage(0); setFilterOpen(false); }}
                >
                  {s === '' ? 'All Status' : s.replace(/_/g, ' ')}
                </button>
              ))}
            </div>
          )}
        </div>
        <button className="flex items-center gap-unit px-stack-sm py-stack-sm font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors">
            <span className="material-symbols-outlined text-[18px]">sort</span>
            Sort
        </button>
      </div>

      {/* Orders Table */}
      <div className="bg-surface-container-lowest rounded-lg border border-outline-variant overflow-hidden shadow-sm flex flex-col">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-container text-on-surface-variant border-b border-outline-variant">
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider">Order ID</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider">Buyer</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider">Products & Qty</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider text-right">Earnings</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider text-center">Status</th>
                <th className="p-stack-md font-label-md text-label-md uppercase tracking-wider text-right">Action</th>
              </tr>
            </thead>
            <tbody className="font-body-sm text-body-sm">
              {loading ? (
                <tr>
                  <td colSpan={6} className="text-center py-8 text-on-surface-variant">Loading orders...</td>
                </tr>
              ) : !data || data.content.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-8 text-on-surface-variant">No orders found.</td>
                </tr>
              ) : (
                data.content.map((order, index) => {
                  const firstItem = order.items[0];
                  const otherItemsCount = order.items.length - 1;
                  const rowBg = index % 2 === 1 ? 'bg-[#f8fafc]' : '';
                  const shortId = order.orderId.substring(0,8).toUpperCase();
                  
                  return (
                    <tr key={order.orderId} className={`border-b border-outline-variant ${rowBg} hover:bg-[#f0fdfa] transition-colors group`}>
                      <td className="p-stack-md font-mono-data text-mono-data text-on-surface">
                        #ORD-{shortId}
                      </td>
                      <td className="p-stack-md text-on-surface-variant">
                        <div className="flex items-center gap-stack-sm">
                          <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${getAvatarColor(order.customerName)}`}>
                            {getInitials(order.customerName)}
                          </div>
                          <span>{order.customerName}</span>
                        </div>
                      </td>
                      <td className="p-stack-md text-on-surface-variant">
                        {firstItem && (
                          <>
                            {firstItem.productName} (x{firstItem.quantity})<br/>
                            <span className="text-xs text-outline">SKU: {firstItem.productId.substring(0,8).toUpperCase()}</span>
                          </>
                        )}
                        {otherItemsCount > 0 && (
                          <div className="text-xs text-outline mt-1">+{otherItemsCount} other items</div>
                        )}
                      </td>
                      <td className="p-stack-md font-mono-data text-mono-data text-right text-primary-container font-medium">
                        Rp {order.totalAmount.toLocaleString('id-ID')}
                      </td>
                      <td className="p-stack-md text-center">
                        {getStatusBadge(order.status)}
                      </td>
                      <td className="p-stack-md text-right">
                        <div className="flex justify-end items-center gap-stack-sm">
                          {order.status === 'PAID_ON_HOLD' ? (
                            <button 
                              onClick={() => handleMarkShipped(order.orderId)}
                              className="bg-primary text-on-primary px-3 py-1 rounded text-xs font-label-md hover:bg-surface-tint transition-colors"
                            >
                              Mark as Shipped
                            </button>
                          ) : (
                            <button disabled className="bg-outline-variant/30 text-on-surface-variant/50 px-3 py-1 rounded text-xs font-label-md opacity-50 cursor-not-allowed">
                              Mark as Shipped
                            </button>
                          )}
                          <button className="text-on-surface-variant hover:text-primary transition-colors p-1" title="View Details">
                            <span className="material-symbols-outlined text-[20px]">visibility</span>
                          </button>
                          {order.status === 'SHIPPED' ? (
                            <button className="border border-primary text-primary hover:bg-primary/10 px-3 py-1 rounded text-xs font-label-md transition-colors">
                              Received
                            </button>
                          ) : (
                            <button disabled className="border border-outline-variant text-on-surface-variant px-3 py-1 rounded text-xs font-label-md opacity-50 cursor-not-allowed">
                              Received
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        {data && (
          <div className="mt-auto border-t border-outline-variant p-stack-sm flex items-center justify-between bg-surface-container-low">
            <span className="font-label-sm text-label-sm text-on-surface-variant">
              Showing {data.totalElements > 0 ? (page * data.size) + 1 : 0}-{Math.min((page + 1) * data.size, data.totalElements)} of {data.totalElements} orders
            </span>
            <div className="flex items-center gap-unit">
              <button 
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="p-1 rounded text-on-surface-variant hover:bg-surface-variant disabled:opacity-50 transition-colors"
              >
                <span className="material-symbols-outlined text-[20px]">chevron_left</span>
              </button>
              
              {[...Array(data.totalPages)].map((_, i) => (
                <button
                  key={i}
                  onClick={() => setPage(i)}
                  className={`w-8 h-8 rounded font-label-md text-label-md flex items-center justify-center transition-colors ${
                    page === i 
                      ? 'bg-primary-container text-on-primary-container' 
                      : 'text-on-surface-variant hover:bg-surface-variant'
                  }`}
                >
                  {i + 1}
                </button>
              ))}

              <button 
                onClick={() => setPage(page + 1)}
                disabled={page >= data.totalPages - 1 || data.totalPages === 0}
                className="p-1 rounded text-on-surface-variant hover:bg-surface-variant disabled:opacity-50 transition-colors"
              >
                <span className="material-symbols-outlined text-[20px]">chevron_right</span>
              </button>
            </div>
          </div>
        )}
      </div>
    </>
  );
}
