import { useEffect, useState } from 'react';
import { adminService } from '../../service/admin/admin.service';
import type { AdminSellerResponse } from '../../service/admin/admin.service';

export function SellerManagement() {
  const [sellers, setSellers] = useState<AdminSellerResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  // Filters
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  const fetchSellers = async () => {
    setLoading(true);
    try {
      const response = await adminService.getSellers(page, 5, statusFilter, searchQuery);
      setSellers(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error("Failed to fetch sellers", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSellers();
  }, [page, statusFilter, searchQuery]);

  const toggleSellerStatus = async (sellerId: string, currentStatus: boolean) => {
    try {
      await adminService.updateSellerStatus(sellerId, { active: !currentStatus });
      // Update local state instead of refetching everything to be faster
      setSellers(sellers.map(s => 
        s.id === sellerId ? { ...s, active: !currentStatus } : s
      ));
    } catch (error) {
      console.error("Failed to update seller status", error);
      alert("Failed to update seller status");
    }
  };

  return (
    <section className="flex flex-col gap-4">
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-stack-sm w-full mt-stack-md mb-stack-md">
        <h2 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Sellers Management</h2>
        
        <div className="flex gap-3">
          <div className="relative">
            <span className="material-symbols-outlined absolute left-2.5 top-2 text-outline-variant text-[18px]">search</span>
            <input 
              className="pl-9 pr-3 py-1.5 border border-outline-variant rounded text-[13px] font-mono-data focus:border-primary focus:ring-1 focus:ring-primary outline-none bg-surface-container-lowest w-64" 
              placeholder="Search stores..." 
              type="text" 
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setPage(0);
              }}
            />
          </div>
          <select 
            className="px-3 py-1.5 border border-outline-variant rounded text-[13px] font-mono-data focus:border-primary focus:ring-1 focus:ring-primary outline-none bg-surface-container-lowest"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
          >
            <option value="">Status: All</option>
            <option value="ACTIVE">Status: Active</option>
            <option value="INACTIVE">Status: Inactive</option>
          </select>
        </div>
      </header>

      <div className="bg-surface-container-lowest border border-outline-variant rounded overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-variant border-b border-outline-variant">
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap">Store Name</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap">Owner Email</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap text-right">Products</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap text-center">Status</th>
                <th className="p-stack-sm font-label-md text-label-md text-on-surface-variant whitespace-nowrap text-center">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={5} className="p-stack-md text-center">
                    <div className="animate-spin inline-block w-6 h-6 border-2 border-primary border-t-transparent rounded-full"></div>
                  </td>
                </tr>
              ) : sellers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="p-stack-md text-center text-on-surface-variant">
                    No sellers found.
                  </td>
                </tr>
              ) : (
                sellers.map((seller, idx) => (
                  <tr key={seller.id} className={`border-b border-outline-variant hover:bg-surface-container-low transition-colors ${idx % 2 === 0 ? 'bg-surface-container-lowest' : 'bg-[#f8fafc]'}`}>
                    <td className="p-stack-sm">
                      <div className="font-label-md text-on-surface">{seller.storeName}</div>
                      <div className="text-[12px] text-on-surface-variant">{seller.sellerName}</div>
                    </td>
                    <td className="p-stack-sm font-mono-data text-[13px] text-on-surface-variant">
                      {seller.email}
                    </td>
                    <td className="p-stack-sm font-mono-data text-right text-on-surface">
                      {seller.productCount.toLocaleString()}
                    </td>
                    <td className="p-stack-sm text-center">
                      <span className={`inline-block px-2 py-0.5 rounded-sm font-mono-data text-[11px] font-bold tracking-wider ${seller.active ? 'bg-primary/10 text-primary border border-primary/20' : 'bg-error/10 text-error border border-error/20'}`}>
                        {seller.active ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </td>
                    <td className="p-stack-sm text-center">
                      <button
                        onClick={() => toggleSellerStatus(seller.id, seller.active)}
                        className={`px-3 py-1.5 rounded-sm font-label-sm transition-colors border ${
                          seller.active 
                            ? 'border-error text-error hover:bg-error/10' 
                            : 'border-primary text-primary hover:bg-primary/10'
                        }`}
                      >
                        {seller.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        
        {/* Pagination */}
        {!loading && totalPages > 1 && (
          <div className="p-stack-sm border-t border-outline-variant flex items-center justify-between bg-surface-variant/50">
            <span className="font-label-sm text-on-surface-variant">
              Page {page + 1} of {totalPages}
            </span>
            <div className="flex gap-2">
              <button 
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
                className="px-3 py-1 rounded-sm border border-outline-variant bg-surface-container-lowest disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-variant font-label-md transition-colors"
              >
                Previous
              </button>
              <button 
                disabled={page >= totalPages - 1}
                onClick={() => setPage(p => p + 1)}
                className="px-3 py-1 rounded-sm border border-outline-variant bg-surface-container-lowest disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-variant font-label-md transition-colors"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
