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
      {/* Search & Filter Bar */}
      <div className="flex items-center gap-[12px] bg-white border border-[#e6e9eb] rounded-[10px] py-[10px] px-[14px] mb-[16px]">
        <div className="flex-1 flex items-center gap-[8px] text-[#6b7876] text-[14px]">
          🔍
          <input 
            className="border-none outline-none text-[14px] w-full bg-transparent placeholder:text-[#6b7876]" 
            placeholder="Search orders by ID or item..." 
            type="text" 
            value={search}
            onChange={handleSearchChange}
          />
        </div>
        
        {/* Filter Dropdown */}
        <div className="relative">
          <button 
            className="text-[13px] font-semibold text-[#14201e] flex items-center gap-[6px] py-[6px] px-[10px] rounded-[6px] cursor-pointer border border-[#e6e9eb]"
            onClick={() => { setFilterOpen(!filterOpen); setSortOpen(false); }}
          >
            ⇅ Filters {status && <span className="w-2 h-2 rounded-full bg-[#0f9b8e] ml-1"></span>}
          </button>
          
          {filterOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-white border border-[#e6e9eb] rounded shadow-lg z-50 py-2">
              <div className="px-4 py-1 text-[12px] font-medium text-[#6b7876]">Filter by Status</div>
              {['', 'PENDING', 'PAID_ON_HOLD', 'SHIPPED', 'RECEIVED', 'CANCELLED'].map((s) => (
                <button
                  key={s}
                  className={`w-full text-left px-4 py-2 text-[14px] hover:bg-[#f4f6f7] transition-colors ${status === s ? 'text-[#0f9b8e] bg-[#e6f6f4] font-medium' : 'text-[#14201e]'}`}
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
            className="text-[13px] font-semibold text-[#14201e] flex items-center gap-[6px] py-[6px] px-[10px] rounded-[6px] cursor-pointer border border-[#e6e9eb]"
            onClick={() => { setSortOpen(!sortOpen); setFilterOpen(false); }}
          >
            ↕ Sort
          </button>
          
          {sortOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-white border border-[#e6e9eb] rounded shadow-lg z-50 py-2">
              <div className="px-4 py-1 text-[12px] font-medium text-[#6b7876]">Sort Orders</div>
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
                  className={`w-full text-left px-4 py-2 text-[14px] hover:bg-[#f4f6f7] transition-colors ${sortBy === opt.by && sortDir === opt.dir ? 'text-[#0f9b8e] bg-[#e6f6f4] font-medium' : 'text-[#14201e]'}`}
                  onClick={() => { setSortBy(opt.by); setSortDir(opt.dir); setPage(1); setSortOpen(false); }}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Orders Table */}
      <div className="bg-white border border-[#e6e9eb] rounded-[10px] shadow-sm flex flex-col h-[calc(100vh-380px)]">
        <div className="flex items-center justify-between py-[16px] px-[20px] border-b border-[#e6e9eb]">
          <h2 className="text-[16px] font-bold m-0 text-[#14201e]">Recent Orders</h2>
          <a className="text-[13px] font-semibold text-[#0c7d73] no-underline hover:underline" href="#">View All</a>
        </div>
        <div className="flex-1 overflow-auto rounded-t-[10px]">
          <table className="w-full border-collapse text-left">
            <thead className="sticky top-0 z-10 bg-surface-container">
              <tr className="text-on-surface-variant border-b border-[#e6e9eb]">
                <th className="text-left text-[11.5px] font-bold tracking-[0.04em] uppercase text-[#6b7876] py-[10px] px-[20px] border-b border-[#e6e9eb] bg-[#fafbfb] w-[130px]">Order ID</th>
                <th className="text-left text-[11.5px] font-bold tracking-[0.04em] uppercase text-[#6b7876] py-[10px] px-[20px] border-b border-[#e6e9eb] bg-[#fafbfb]">Item</th>
                <th className="text-right text-[11.5px] font-bold tracking-[0.04em] uppercase text-[#6b7876] py-[10px] px-[20px] border-b border-[#e6e9eb] bg-[#fafbfb] w-[150px]">Amount</th>
                <th className="text-left text-[11.5px] font-bold tracking-[0.04em] uppercase text-[#6b7876] py-[10px] px-[20px] border-b border-[#e6e9eb] bg-[#fafbfb] w-[120px]">Status</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={4} className="py-[12px] px-[20px] border-b border-[#e6e9eb] text-center text-[#6b7876] text-[14px]">Loading orders...</td>
                </tr>
              ) : !orderPage || orderPage.content.length === 0 ? (
                <tr>
                  <td colSpan={4} className="py-[12px] px-[20px] border-b border-[#e6e9eb] text-center text-[#6b7876] text-[14px]">No recent orders found.</td>
                </tr>
              ) : (
                orderPage.content.map(order => {
                  let badgeClass = "bg-[#f4f6f7] text-[#6b7876]";
                  if (order.status.includes('RECEIVED')) badgeClass = "bg-[#e6f6f4] text-[#0c7d73]";
                  if (order.status.includes('SHIPPED')) badgeClass = "bg-[#e8f0fe] text-[#2255c7]";
                  if (order.status.includes('PENDING') || order.status.includes('HOLD')) badgeClass = "bg-[#fdf0e3] text-[#b4650a]";

                  return (
                    <tr key={order.orderId} className="hover:bg-[#fafbfb] transition-colors group cursor-pointer">
                      <td className="py-[12px] px-[20px] border-b border-[#e6e9eb] align-middle text-[14px]">
                        <span className="font-mono text-[13px] text-[#6b7876]">{order.displayId}</span>
                      </td>
                      <td className="py-[12px] px-[20px] border-b border-[#e6e9eb] align-middle text-[14px]">
                        <div className="flex items-center gap-[10px]">
                          <div className="w-[44px] h-[44px] rounded-[8px] bg-[#eef1f1] flex shrink-0 items-center justify-center overflow-hidden">
                            <img alt={order.itemName} className="w-full h-full object-cover" src={order.itemImage} />
                          </div>
                          <div>
                            <div className="font-semibold text-[14px] text-[#14201e]">{order.itemName}</div>
                            <div className="text-[12px] text-[#6b7876] mt-[2px]">Qty {order.quantity || 1}</div>
                          </div>
                        </div>
                      </td>
                      <td className="py-[12px] px-[20px] border-b border-[#e6e9eb] align-middle text-[14px] text-right font-semibold tabular-nums text-[#14201e]">
                        Rp {order.amount.toLocaleString('id-ID')}
                      </td>
                      <td className="py-[12px] px-[20px] border-b border-[#e6e9eb] align-middle text-[14px]">
                        <span className={`inline-flex items-center gap-[6px] py-[4px] px-[10px] rounded-full text-[11.5px] font-bold tracking-[0.02em] uppercase ${badgeClass}`}>
                          <span className="w-[6px] h-[6px] rounded-full bg-current"></span>
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
        <div className="mt-auto flex items-center justify-between py-[12px] px-[20px] text-[13px] text-[#6b7876] bg-surface-container-low border-t border-[#e6e9eb]">
          <div>
            Showing {orderPage?.totalElements === 0 ? 0 : (page - 1) * limit + 1}-{Math.min(page * limit, orderPage?.totalElements || 0)} of {orderPage?.totalElements || 0} orders
          </div>
          <div className="flex items-center gap-[6px]">
            <button 
              className="text-[#6b7876] hover:text-[#14201e] disabled:opacity-50 cursor-pointer text-[18px] bg-transparent border-none" 
              disabled={page <= 1}
              onClick={() => setPage(p => Math.max(1, p - 1))}
            >
              ‹
            </button>
            <div className="w-[26px] h-[26px] rounded-[6px] bg-[#0f9b8e] text-white font-bold text-[12.5px] flex items-center justify-center">
              {page}
            </div>
            <button 
              className="text-[#6b7876] hover:text-[#14201e] disabled:opacity-50 cursor-pointer text-[18px] bg-transparent border-none" 
              disabled={!orderPage || page >= orderPage.totalPages}
              onClick={() => setPage(p => p + 1)}
            >
              ›
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
