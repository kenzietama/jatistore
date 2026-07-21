import React, { useEffect, useState } from 'react';
import { dashboardService } from '../../../service/seller/dashboard.service';
import type { RecentOrder, PageResponse } from '../../../service/seller/dashboard.service';

export function RecentOrders() {
  const [orderPage, setOrderPage] = useState<PageResponse<RecentOrder> | null>(null);
  const [loading, setLoading] = useState(true);

  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  const [page, setPage] = useState(1);
  const limit = 5;

  // Dropdown states
  const [filterOpen, setFilterOpen] = useState(false);
  const [sortOpen, setSortOpen] = useState(false);

  const fetchOrders = () => {
    setLoading(true);
    dashboardService.getRecentOrders(search, status, sortBy, sortDir, page, limit)
      .then(data => {
        setOrderPage(data);
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  };

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      fetchOrders();
    }, 400); // 400ms debounce
    return () => clearTimeout(timeoutId);
  }, [search, status, sortBy, sortDir, page]);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(e.target.value);
    setPage(1); // Reset to first page on new search
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
              placeholder="Search orders by ID or item..." 
              type="text" 
              value={search}
              onChange={handleSearchChange}
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
              Filters {status && <span className="w-2 h-2 rounded-full bg-primary ml-1"></span>}
            </button>
            
            {filterOpen && (
              <div className="absolute right-0 mt-2 w-48 bg-surface border border-outline-variant rounded shadow-lg z-50 py-2">
                <div className="px-4 py-1 text-label-sm text-on-surface-variant uppercase">Filter by Status</div>
                {['', 'PENDING', 'PAID_ON_HOLD', 'SHIPPED', 'RECEIVED', 'CANCELLED'].map((s) => (
                  <button
                    key={s}
                    className={`w-full text-left px-4 py-2 font-body-sm text-body-sm hover:bg-surface-container transition-colors ${status === s ? 'text-primary bg-primary-container/10 font-medium' : 'text-on-surface'}`}
                    onClick={() => { setStatus(s); setPage(1); setFilterOpen(false); }}
                  >
                    {s === '' ? 'All Status' : s.replace('_', ' ')}
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
                <div className="px-4 py-1 text-label-sm text-on-surface-variant uppercase">Sort Orders</div>
                {[
                  { label: 'Newest First', by: 'createdAt', dir: 'desc' },
                  { label: 'Oldest First', by: 'createdAt', dir: 'asc' },
                  { label: 'Highest Amount', by: 'amount', dir: 'desc' },
                  { label: 'Lowest Amount', by: 'amount', dir: 'asc' },
                  { label: 'Name (A-Z)', by: 'name', dir: 'asc' },
                  { label: 'Name (Z-A)', by: 'name', dir: 'desc' },
                ].map((opt, i) => (
                  <button
                    key={i}
                    className={`w-full text-left px-4 py-2 font-body-sm text-body-sm hover:bg-surface-container transition-colors ${sortBy === opt.by && sortDir === opt.dir ? 'text-primary bg-primary-container/10 font-medium' : 'text-on-surface'}`}
                    onClick={() => { setSortBy(opt.by); setSortDir(opt.dir); setPage(1); setSortOpen(false); }}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
        <div className="flex-1 overflow-auto rounded-t-[10px]">
          <table className="w-full border-collapse text-left">
            <thead className="sticky top-0 z-10">
              <tr className="bg-surface-container text-on-surface-variant border-b border-outline-variant text-label-sm font-label-sm">
                <th className="py-3 px-4 font-medium uppercase tracking-wider">Order ID</th>
                <th className="py-3 px-4 font-medium uppercase tracking-wider">Item</th>
                <th className="py-3 px-4 font-medium uppercase tracking-wider text-right">Amount</th>
                <th className="py-3 px-4 font-medium uppercase tracking-wider">Status</th>
              </tr>
            </thead>
            <tbody className="font-body-sm text-body-sm">
              {loading ? (
                <tr>
                  <td colSpan={4} className="py-8 text-center text-on-surface-variant">Loading orders...</td>
                </tr>
              ) : !orderPage || orderPage.content.length === 0 ? (
                <tr>
                  <td colSpan={4} className="py-8 text-center text-on-surface-variant">No recent orders found.</td>
                </tr>
              ) : (
                orderPage.content.map((order, idx) => {
                  let badgeClass = "bg-surface-container-high text-on-surface-variant";
                  if (order.status.includes('RECEIVED')) badgeClass = "bg-primary-container/20 text-primary";
                  if (order.status.includes('SHIPPED')) badgeClass = "bg-info-container text-on-info-container";
                  if (order.status.includes('PENDING') || order.status.includes('HOLD')) badgeClass = "bg-tertiary-container/20 text-tertiary";

                  return (
                    <tr key={`${order.orderId}-${idx}`} className={`hover:bg-primary-container/5 transition-colors group cursor-pointer border-b border-outline-variant`}>
                      <td className="py-3 px-4 align-middle text-on-surface-variant font-mono-data text-mono-data">
                        {order.displayId}
                      </td>
                      <td className="py-3 px-4 align-middle">
                        <div className="flex items-center gap-3">
                          <div className="w-11 h-11 rounded bg-surface-container flex shrink-0 items-center justify-center overflow-hidden border border-outline-variant">
                            <img alt={order.itemName} className="w-full h-full object-cover" src={order.itemImage} />
                          </div>
                          <div>
                            <div className="font-medium text-on-surface">{order.itemName}</div>
                            <div className="text-xs text-on-surface-variant mt-0.5">Qty {order.quantity || 1}</div>
                          </div>
                        </div>
                      </td>
                      <td className="py-3 px-4 align-middle text-right font-medium font-mono-data text-mono-data text-on-surface">
                        Rp {order.amount.toLocaleString('id-ID')}
                      </td>
                      <td className="py-3 px-4 align-middle">
                        <span className={`inline-flex items-center gap-1.5 py-1 px-2.5 rounded-full text-[11.5px] font-bold tracking-[0.02em] uppercase ${badgeClass}`}>
                          <span className="w-1.5 h-1.5 rounded-full bg-current"></span>
                          {order.status}
                        </span>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
        <div className="mt-auto border-t border-outline-variant p-stack-sm flex items-center justify-between bg-surface-container-low shrink-0">
          <span className="font-label-sm text-label-sm text-on-surface-variant">
            Showing {orderPage?.totalElements === 0 ? 0 : (page - 1) * limit + 1}-{Math.min(page * limit, orderPage?.totalElements || 0)} of {orderPage?.totalElements || 0} orders
          </span>
          <div className="flex items-center gap-unit">
            <button 
              className="p-2 border border-outline-variant rounded-lg disabled:opacity-50 hover:bg-surface-tint transition-colors" 
              disabled={page <= 1}
              onClick={() => setPage(p => Math.max(1, p - 1))}
            >
              <span className="material-symbols-outlined text-[18px] text-on-surface">chevron_left</span>
            </button>
            <span className="text-body-sm font-body-sm text-on-surface px-2">
              Page {page} of {orderPage?.totalPages || 1}
            </span>
            <button 
              className="p-2 border border-outline-variant rounded-lg disabled:opacity-50 hover:bg-surface-tint transition-colors" 
              disabled={!orderPage || page >= orderPage.totalPages}
              onClick={() => setPage(p => p + 1)}
            >
              <span className="material-symbols-outlined text-[18px] text-on-surface">chevron_right</span>
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
