import { useState, useEffect } from 'react';
import { orderService } from '../../../service/seller/order.service';
import type { SellerOrder, PageData } from '../../../service/seller/order.service';

export function OrderTable() {
  const [data, setData] = useState<PageData<SellerOrder> | null>(null);
  const [filterOpen, setFilterOpen] = useState(false);
  const [sortOpen, setSortOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('date');
  const [sortDir, setSortDir] = useState('desc');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<SellerOrder | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [orderToShip, setOrderToShip] = useState<string | null>(null);
  const [isShippingModalOpen, setIsShippingModalOpen] = useState(false);
  const [isShipping, setIsShipping] = useState(false);

  const handleViewDetail = async (orderId: string) => {
    try {
      setLoadingDetail(true);
      setIsDetailModalOpen(true);
      const res = await orderService.getOrderDetail(orderId);
      setSelectedOrder(res);
    } catch (error) {
      console.error(error);
      alert('Failed to load order detail');
      setIsDetailModalOpen(false);
    } finally {
      setLoadingDetail(false);
    }
  };

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const res = await orderService.getOrders(statusFilter, search, sortBy, sortDir, page, 10);
      setData(res);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchOrders();
    }, 300);
    return () => clearTimeout(timer);
  }, [statusFilter, search, sortBy, sortDir, page]);

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

  const handleMarkShipped = (orderId: string) => {
    setOrderToShip(orderId);
    setIsShippingModalOpen(true);
  };

  const confirmShipOrder = async () => {
    if (!orderToShip) return;
    
    setIsShipping(true);
    try {
      await orderService.markAsShipped(orderToShip);
      fetchOrders(); // Refresh table
      setIsShippingModalOpen(false);
      setOrderToShip(null);
    } catch (error) {
      console.error('Failed to update status', error);
      alert('Failed to mark as shipped. Status must be PAID_ON_HOLD.');
    } finally {
      setIsShipping(false);
    }
  };

  const cancelShipOrder = () => {
    setIsShippingModalOpen(false);
    setOrderToShip(null);
  };

  return (
    <>
      {/* Orders Table */}
      <div className="bg-surface-container-lowest rounded-lg border border-outline-variant shadow-sm overflow-hidden flex-grow flex flex-col">
        
        <div className="flex items-center gap-stack-md bg-surface-container-lowest p-stack-sm border-b border-outline-variant w-full shrink-0">
          <div className="flex-grow flex items-center relative">
            <span className="material-symbols-outlined absolute left-stack-sm text-on-surface-variant">search</span>
            <input 
              className="w-full pl-10 pr-stack-sm py-stack-sm bg-transparent border-none focus:ring-0 font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant transition-all" 
              placeholder="Search orders..." 
              type="text" 
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
            />
          </div>
          <div className="h-6 w-px bg-outline-variant"></div>

          {/* Filter Dropdown */}
            <div className="relative">
              <button 
                className="flex items-center gap-unit px-2 py-1.5 font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors"
                onClick={() => { setFilterOpen(!filterOpen); setSortOpen(false); }}
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

            {/* Sort Dropdown */}
            <div className="relative">
              <button 
                className="flex items-center gap-unit px-2 py-1.5 font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors"
                onClick={() => { setSortOpen(!sortOpen); setFilterOpen(false); }}
              >
                <span className="material-symbols-outlined text-[18px]">sort</span>
                Sort
              </button>

              {sortOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-surface border border-outline-variant rounded shadow-lg z-50 py-2">
                  <div className="px-4 py-1 text-label-sm text-on-surface-variant uppercase">Sort By</div>
                  {[
                    { label: 'Date (Newest)', by: 'date', dir: 'desc' },
                    { label: 'Date (Oldest)', by: 'date', dir: 'asc' },
                    { label: 'Amount (Highest)', by: 'amount', dir: 'desc' },
                    { label: 'Amount (Lowest)', by: 'amount', dir: 'asc' },
                    { label: 'Status (A-Z)', by: 'status', dir: 'asc' },
                  ].map((opt, i) => (
                    <button
                      key={i}
                      className={`w-full text-left px-4 py-2 font-body-sm text-body-sm hover:bg-surface-container transition-colors ${sortBy === opt.by && sortDir === opt.dir ? 'text-primary bg-primary-container/10 font-medium' : 'text-on-surface'}`}
                      onClick={() => { setSortBy(opt.by); setSortDir(opt.dir); setPage(0); setSortOpen(false); }}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

        <div className="overflow-x-auto flex-1">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-container text-on-surface-variant border-b border-outline-variant text-label-sm font-label-sm">
                <th className="py-3 px-4 font-medium uppercase tracking-wider">Order ID</th>
                <th className="py-3 px-4 font-medium uppercase tracking-wider">Buyer</th>
                <th className="py-3 px-4 font-medium uppercase tracking-wider">Products & Qty</th>
                <th className="py-3 px-4 font-medium uppercase tracking-wider text-right">Earnings</th>
                <th className="py-3 px-4 font-medium uppercase tracking-wider text-center">Status</th>
                <th className="py-3 px-4 font-medium uppercase tracking-wider text-right">Action</th>
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
                      <td className="py-3 px-4 font-mono-data text-mono-data text-on-surface">
                        #ORD-{shortId}
                      </td>
                      <td className="py-3 px-4 text-on-surface-variant">
                        <div className="flex items-center gap-stack-sm">
                          <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${getAvatarColor(order.customerName)}`}>
                            {getInitials(order.customerName)}
                          </div>
                          <span>{order.customerName}</span>
                        </div>
                      </td>
                      <td className="py-3 px-4 text-on-surface-variant">
                        {firstItem && (
                          <>
                            <div className="flex items-center gap-1 flex-wrap">
                              <span>{firstItem.productName} (x{firstItem.quantity})</span>
                              {firstItem.flashSale && (
                                <span className="text-[10px] text-error bg-error-container/30 px-2 py-0.5 rounded-full">
                                  ⚡ Flash Sale
                                </span>
                              )}
                            </div>
                            <span className="text-xs text-outline">SKU: {firstItem.productId.substring(0,8).toUpperCase()}</span>
                          </>
                        )}
                        {otherItemsCount > 0 && (
                          <div className="text-xs text-outline mt-1">+{otherItemsCount} other items</div>
                        )}
                      </td>
                      <td className="py-3 px-4 font-mono-data text-mono-data text-right text-primary-container font-medium">
                        Rp {order.totalAmount.toLocaleString('id-ID')}
                      </td>
                      <td className="py-3 px-4 text-center">
                        {getStatusBadge(order.status)}
                      </td>
                      <td className="py-3 px-4 text-right">
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
                          <button 
                            onClick={() => handleViewDetail(order.orderId)}
                            className="text-on-surface-variant hover:text-primary transition-colors p-1" 
                            title="View Details"
                          >
                            <span className="material-symbols-outlined text-[20px]">visibility</span>
                          </button>
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
          <div className="mt-auto border-t border-outline-variant p-stack-sm flex items-center justify-between bg-surface-container-low shrink-0">
            <span className="font-label-sm text-label-sm text-on-surface-variant">
              Showing {data.totalElements > 0 ? (page * data.size) + 1 : 0}-{Math.min((page + 1) * data.size, data.totalElements)} of {data.totalElements} orders
            </span>
            <div className="flex items-center gap-unit">
              <button 
                disabled={page === 0}
                onClick={() => setPage(Math.max(0, page - 1))}
                className="p-2 border border-outline-variant rounded-lg disabled:opacity-50 hover:bg-surface-tint transition-colors"
              >
                <span className="material-symbols-outlined text-[18px] text-on-surface">chevron_left</span>
              </button>
              <span className="text-body-sm font-body-sm text-on-surface px-2">
                Page {page + 1} of {data.totalPages || 1}
              </span>
              <button 
                disabled={page >= data.totalPages - 1 || data.totalPages === 0}
                onClick={() => setPage(page + 1)}
                className="p-2 border border-outline-variant rounded-lg disabled:opacity-50 hover:bg-surface-tint transition-colors"
              >
                <span className="material-symbols-outlined text-[18px] text-on-surface">chevron_right</span>
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Order Detail Modal */}
      {isDetailModalOpen && (
        <div className="fixed inset-0 bg-on-background/30 backdrop-blur-sm z-[999] flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest border border-outline-variant rounded-lg w-full max-w-3xl shadow-lg flex flex-col max-h-[90vh]">
            
            {/* Modal Header */}
            <div className="p-stack-md border-b border-outline-variant flex justify-between items-center bg-surface-container-low shrink-0 rounded-t-lg">
              <div className="flex items-center gap-stack-sm">
                <h3 className="text-headline-sm font-headline-sm text-on-surface">Order Details</h3>
                {selectedOrder && (
                  <span className="font-mono-data text-label-md text-on-surface-variant bg-surface-variant px-2 py-1 rounded">
                    #ORD-{selectedOrder.orderId.substring(0,8).toUpperCase()}
                  </span>
                )}
              </div>
              <button 
                onClick={() => setIsDetailModalOpen(false)}
                className="text-on-surface-variant hover:text-on-surface hover:bg-surface-variant p-1 rounded-full transition-colors flex items-center justify-center"
              >
                <span className="material-symbols-outlined text-[20px]">close</span>
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-stack-md overflow-y-auto flex-1">
              {loadingDetail ? (
                <div className="flex justify-center items-center py-12">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                </div>
              ) : selectedOrder ? (
                <div className="space-y-stack-lg">
                  {/* Info Grid */}
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-stack-md bg-surface-container-lowest p-stack-sm rounded-lg border border-outline-variant">
                    <div>
                      <p className="text-label-sm font-label-sm text-on-surface-variant uppercase mb-1">Status</p>
                      <div>{getStatusBadge(selectedOrder.status)}</div>
                    </div>
                    <div>
                      <p className="text-label-sm font-label-sm text-on-surface-variant uppercase mb-1">Date</p>
                      <p className="font-body-sm text-body-sm text-on-surface">
                        {new Date(selectedOrder.orderDate).toLocaleDateString('id-ID', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                      </p>
                    </div>
                    <div>
                      <p className="text-label-sm font-label-sm text-on-surface-variant uppercase mb-1">Customer</p>
                      <p className="font-body-sm text-body-sm text-on-surface flex items-center gap-2">
                        <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold ${getAvatarColor(selectedOrder.customerName)}`}>
                          {getInitials(selectedOrder.customerName)}
                        </span>
                        {selectedOrder.customerName}
                      </p>
                    </div>
                    <div>
                      <p className="text-label-sm font-label-sm text-on-surface-variant uppercase mb-1">Total Amount</p>
                      <p className="font-label-md text-label-md text-primary font-bold">
                        Rp {selectedOrder.totalAmount.toLocaleString('id-ID')}
                      </p>
                    </div>
                  </div>

                  {/* Items List */}
                  <div>
                    <h4 className="font-title-md text-title-md text-on-surface mb-stack-sm flex items-center gap-2">
                      <span className="material-symbols-outlined text-[20px] text-on-surface-variant">inventory_2</span>
                      Order Items
                    </h4>
                    <div className="border border-outline-variant rounded-lg overflow-hidden">
                      <table className="w-full text-left border-collapse">
                        <thead className="bg-surface-container-low text-on-surface-variant border-b border-outline-variant text-label-sm font-label-sm uppercase">
                          <tr>
                            <th className="p-stack-sm">Product</th>
                            <th className="p-stack-sm text-center">Qty</th>
                            <th className="p-stack-sm text-right">Price</th>
                            <th className="p-stack-sm text-right">Subtotal</th>
                          </tr>
                        </thead>
                        <tbody className="bg-surface-container-lowest">
                          {selectedOrder.items.map((item, idx) => (
                            <tr key={idx} className="border-b border-outline-variant last:border-0 hover:bg-[#f8fafc]">
                              <td className="p-stack-sm">
                                <div className="flex items-center gap-stack-sm">
                                  <img 
                                    src={item.productImage || 'https://via.placeholder.com/40'} 
                                    alt={item.productName}
                                    className="w-10 h-10 object-cover rounded border border-outline-variant bg-surface-variant shrink-0"
                                  />
                                  <div className="flex flex-col">
                                    <span className="font-label-md text-label-md text-on-surface line-clamp-1">{item.productName}</span>
                                    <div className="flex items-center gap-2 mt-0.5">
                                      <span className="text-[10px] text-on-surface-variant font-mono-data">SKU: {item.productId.substring(0,8).toUpperCase()}</span>
                                      {item.flashSale && (
                                        <span className="text-[10px] text-error bg-error-container/30 px-2 py-0.5 rounded-full">
                                          ⚡ Flash Sale
                                        </span>
                                      )}
                                    </div>
                                  </div>
                                </div>
                              </td>
                              <td className="p-stack-sm text-center font-body-sm text-body-sm text-on-surface">
                                {item.quantity}
                              </td>
                              <td className="p-stack-sm text-right font-mono-data text-body-sm text-on-surface-variant">
                                Rp {item.pricePerItem.toLocaleString('id-ID')}
                              </td>
                              <td className="p-stack-sm text-right font-mono-data text-label-md text-on-surface font-medium">
                                Rp {item.subtotal.toLocaleString('id-ID')}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                  
                </div>
              ) : (
                <div className="text-center py-8 text-on-surface-variant">
                  No data available.
                </div>
              )}
            </div>
            
            {/* Modal Footer */}
            <div className="p-stack-md border-t border-outline-variant bg-surface-container-lowest flex justify-end shrink-0 rounded-b-lg">
              <button 
                onClick={() => setIsDetailModalOpen(false)}
                className="px-6 py-2 rounded bg-primary text-on-primary font-label-md text-label-md hover:opacity-90 transition-opacity"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Shipping Confirmation Modal */}
      {isShippingModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
          <div className="bg-surface-container-lowest rounded-xl shadow-lg border border-outline-variant overflow-hidden w-full max-w-sm flex flex-col">
            <div className="p-stack-md border-b border-outline-variant flex items-center gap-2">
              <span className="material-symbols-outlined text-secondary">local_shipping</span>
              <h3 className="font-title-md text-title-md text-on-surface m-0">Confirm Shipment</h3>
            </div>
            
            <div className="p-stack-md">
              <p className="text-body-md text-on-surface-variant m-0">
                Are you sure you want to mark order <strong>{orderToShip?.substring(0,8).toUpperCase()}</strong> as shipped? This action cannot be undone.
              </p>
            </div>
            
            <div className="p-stack-md border-t border-outline-variant bg-surface flex justify-end gap-stack-sm rounded-b-xl">
              <button 
                onClick={cancelShipOrder}
                disabled={isShipping}
                className="px-4 py-2 rounded font-label-md text-label-md text-on-surface-variant hover:bg-surface-variant transition-colors disabled:opacity-50"
              >
                Cancel
              </button>
              <button 
                onClick={confirmShipOrder}
                disabled={isShipping}
                className="px-4 py-2 rounded bg-primary text-on-primary font-label-md text-label-md hover:opacity-90 transition-opacity flex items-center gap-2 disabled:opacity-50"
              >
                {isShipping ? (
                  <>
                    <span className="material-symbols-outlined animate-spin text-[18px]">progress_activity</span>
                    Processing...
                  </>
                ) : (
                  'Confirm Shipped'
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
