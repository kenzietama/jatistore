import { useState, useEffect } from 'react';
import { productService } from '../../../service/seller/product.service';
import type { Product, PageData } from '../../../service/seller/product.service';

export function ProductTable({ refreshTrigger, onEdit }: { refreshTrigger?: number, onEdit?: (product: Product) => void }) {
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [sortBy, setSortBy] = useState('');
  const [sortDir, setSortDir] = useState('');
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  
  const [filterOpen, setFilterOpen] = useState(false);
  const [sortOpen, setSortOpen] = useState(false);

  const [data, setData] = useState<PageData<Product> | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const res = await productService.getProducts(search, undefined, status, sortBy, sortDir, page, size);
      setData(res);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // Use debounced search or just fetch on enter, but for simplicity we fetch on effect.
  useEffect(() => {
    const timer = setTimeout(() => {
      fetchProducts();
    }, 300);
    return () => clearTimeout(timer);
  }, [refreshTrigger, search, status, sortBy, sortDir, page, size]);

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deleteTargetId, setDeleteTargetId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const handleDelete = (id: string) => {
    setDeleteTargetId(id);
    setIsDeleteModalOpen(true);
  };

  const confirmDelete = async () => {
    if (!deleteTargetId) return;
    setIsDeleting(true);
    try {
      await productService.deleteProduct(deleteTargetId);
      fetchProducts();
    } catch (error) {
      console.error('Failed to delete product', error);
    } finally {
      setIsDeleting(false);
      setIsDeleteModalOpen(false);
      setDeleteTargetId(null);
    }
  };

  const cancelDelete = () => {
    setIsDeleteModalOpen(false);
    setDeleteTargetId(null);
  };

  return (
    <div className="bg-surface-container-lowest rounded-lg border border-outline-variant shadow-sm overflow-hidden flex-grow flex flex-col">
      
      <div className="flex items-center gap-stack-md bg-surface-container-lowest p-stack-sm border-b border-outline-variant w-full">
        <div className="flex-grow flex items-center relative">
          <span className="material-symbols-outlined absolute left-stack-sm text-on-surface-variant">search</span>
          <input 
            className="w-full pl-10 pr-stack-sm py-stack-sm bg-transparent border-none focus:ring-0 font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant" 
            placeholder="Search products by name, ID, or category..." 
            type="text"
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          />
        </div>
        <div className="h-6 w-px bg-outline-variant"></div>
        
        {/* Filter Dropdown */}
        <div className="relative">
          <button 
            className="flex items-center gap-unit px-stack-sm py-stack-sm font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors"
            onClick={() => { setFilterOpen(!filterOpen); setSortOpen(false); }}
          >
            <span className="material-symbols-outlined text-[18px]">filter_list</span>
            Filters {status && <span className="w-2 h-2 rounded-full bg-primary ml-1"></span>}
          </button>
          
          {filterOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-surface border border-outline-variant rounded shadow-lg z-50 py-2">
              <div className="px-4 py-1 text-label-sm text-on-surface-variant uppercase">Status</div>
              {['', 'ACTIVE', 'LOW STOCK', 'OUT OF STOCK'].map((s) => (
                <button
                  key={s}
                  className={`w-full text-left px-4 py-2 font-body-sm text-body-sm hover:bg-surface-container transition-colors ${status === s ? 'text-primary bg-primary-container/10 font-medium' : 'text-on-surface'}`}
                  onClick={() => { setStatus(s); setPage(0); setFilterOpen(false); }}
                >
                  {s === '' ? 'All Status' : s}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Sort Dropdown */}
        <div className="relative">
          <button 
            className="flex items-center gap-unit px-stack-sm py-stack-sm font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors"
            onClick={() => { setSortOpen(!sortOpen); setFilterOpen(false); }}
          >
            <span className="material-symbols-outlined text-[18px]">sort</span>
            Sort
          </button>

          {sortOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-surface border border-outline-variant rounded shadow-lg z-50 py-2">
              <div className="px-4 py-1 text-label-sm text-on-surface-variant uppercase">Sort By</div>
              {[
                { label: 'Price (Low to High)', by: 'price', dir: 'asc' },
                { label: 'Price (High to Low)', by: 'price', dir: 'desc' },
                { label: 'Stock (Lowest)', by: 'stock', dir: 'asc' },
                { label: 'Stock (Highest)', by: 'stock', dir: 'desc' },
                { label: 'Name (A-Z)', by: 'name', dir: 'asc' },
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

      {/* Product Table */}
      <div className="overflow-x-auto w-full flex-grow">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-surface-container border-b border-outline-variant font-label-sm text-label-sm text-on-surface-variant uppercase">
              <th className="p-stack-md font-medium whitespace-nowrap">Product</th>
              <th className="p-stack-md font-medium whitespace-nowrap">Price</th>
              <th className="p-stack-md font-medium whitespace-nowrap">Stock</th>
              <th className="p-stack-md font-medium whitespace-nowrap">Category</th>
              <th className="p-stack-md font-medium whitespace-nowrap">Status</th>
              <th className="p-stack-md font-medium whitespace-nowrap text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="font-body-sm text-body-sm text-on-surface divide-y divide-outline-variant">
            {loading ? (
              <tr>
                <td colSpan={6} className="text-center py-8 text-on-surface-variant">Loading...</td>
              </tr>
            ) : data?.content.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center py-8 text-on-surface-variant">No products found.</td>
              </tr>
            ) : data?.content.map((prod) => {
              const isLowStock = prod.status === 'LOW STOCK';
              const isOutOfStock = prod.status === 'OUT OF STOCK';
              const sku = prod.id ? prod.id.substring(0, 8).toUpperCase() : '';

              return (
                <tr key={prod.id} className={`hover:bg-primary-container/5 transition-colors group ${isLowStock ? 'bg-surface-container-lowest' : ''} ${isOutOfStock ? 'bg-surface-bright opacity-60' : ''}`}>
                  <td className="p-stack-md">
                    <div className="flex items-center gap-stack-md">
                      <div className={`w-12 h-12 rounded bg-surface-container overflow-hidden flex-shrink-0 border border-outline-variant flex items-center justify-center ${isOutOfStock ? 'grayscale' : ''}`}>
                        {prod.image ? (
                           <img alt={prod.name} className="w-full h-full object-cover" src={prod.image} />
                        ) : (
                           <span className="material-symbols-outlined text-outline">image</span>
                        )}
                      </div>
                      <div className="flex flex-col">
                        <span className={`font-medium ${isOutOfStock ? 'text-on-surface-variant line-through' : ''}`}>{prod.name}</span>
                        <span className={`${isOutOfStock ? 'text-outline' : 'text-on-surface-variant'} text-xs mt-0.5`}>SKU: {sku}</span>
                      </div>
                    </div>
                  </td>
                  <td className={`p-stack-md font-mono-data text-mono-data font-medium ${isOutOfStock ? 'text-on-surface-variant' : ''}`}>
                    Rp {prod.price?.toLocaleString('id-ID')}
                  </td>
                  <td className={`p-stack-md font-mono-data text-mono-data ${isOutOfStock ? 'text-on-surface-variant' : ''}`}>
                    {isLowStock ? (
                      <div className="flex items-center gap-2">
                        <span className="text-tertiary font-mono-data">{prod.stock}</span>
                        <span className="material-symbols-outlined text-[16px] text-tertiary" title="Low Stock">warning</span>
                      </div>
                    ) : (
                      prod.stock
                    )}
                  </td>
                  <td className={`p-stack-md ${isOutOfStock ? 'text-outline' : 'text-on-surface-variant'}`}>
                    {prod.categoryName}
                  </td>
                  <td className="p-stack-md">
                    {isOutOfStock ? (
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-label-sm bg-surface-variant text-on-surface-variant border border-outline-variant uppercase">OUT OF STOCK</span>
                    ) : isLowStock ? (
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-label-sm bg-tertiary/10 text-tertiary border border-tertiary/20 uppercase">LOW STOCK</span>
                    ) : (
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-label-sm bg-primary/10 text-primary border border-primary/20 uppercase">ACTIVE</span>
                    )}
                  </td>
                  <td className="p-stack-md text-right">
                    <div className="flex justify-end gap-unit transition-opacity">
                        <button 
                          className="p-1.5 text-on-surface-variant hover:text-primary hover:bg-surface-tint rounded-full transition-colors"
                          onClick={() => onEdit && onEdit(prod)}
                        >
                          <span className="material-symbols-outlined text-[18px]">edit</span>
                        </button>
                      <button className="p-1.5 text-on-surface-variant hover:text-error hover:bg-error/10 rounded transition-colors" title="Delete" onClick={() => handleDelete(prod.id)}>
                        <span className="material-symbols-outlined text-[20px]">delete</span>
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Pagination Footer */}
      <div className="border-t border-outline-variant p-stack-sm flex items-center justify-between bg-surface-container-low">
        <span className="font-label-sm text-label-sm text-on-surface-variant">
          Showing {data?.totalElements ? (page * size) + 1 : 0}-{Math.min((page + 1) * size, data?.totalElements || 0)} of {data?.totalElements || 0} products
        </span>
        <div className="flex items-center gap-unit">
          <button 
            disabled={page === 0}
            onClick={() => setPage(p => p - 1)}
            className="p-2 border border-[#e6e9eb] rounded-lg disabled:opacity-50 hover:bg-surface-tint transition-colors"
          >
            <span className="material-symbols-outlined text-[18px] text-on-surface">chevron_left</span>
          </button>
          <span className="text-body-sm font-body-sm text-on-surface">
            Page {page + 1} of {data?.totalPages || 1}
          </span>
          <button 
            disabled={page + 1 >= (data?.totalPages || 1)}
            onClick={() => setPage(p => p + 1)}
            className="p-2 border border-[#e6e9eb] rounded-lg disabled:opacity-50 hover:bg-surface-tint transition-colors"
          >
            <span className="material-symbols-outlined text-[18px] text-on-surface">chevron_right</span>
          </button>
        </div>
      </div>

      {isDeleteModalOpen && (
        <div className="fixed inset-0 bg-on-background/30 backdrop-blur-sm z-[999] flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-6 max-w-sm w-full shadow-lg">
            <h3 className="text-headline-md font-headline-md text-on-surface mb-2">Confirm Delete</h3>
            <p className="text-body-md font-body-md text-on-surface-variant mb-6">Are you sure you want to delete this product? This action cannot be undone.</p>
            <div className="flex justify-end gap-3">
              <button 
                onClick={cancelDelete}
                disabled={isDeleting}
                className="px-4 py-2 rounded text-on-surface-variant hover:bg-surface-container-high transition-colors font-label-md text-label-md disabled:opacity-50"
              >
                Cancel
              </button>
              <button 
                onClick={confirmDelete}
                disabled={isDeleting}
                className="px-4 py-2 rounded bg-error text-on-error hover:opacity-90 transition-opacity font-label-md text-label-md flex items-center gap-2 disabled:opacity-50"
              >
                {isDeleting ? (
                  <span className="material-symbols-outlined text-[18px] animate-spin">refresh</span>
                ) : (
                  <span className="material-symbols-outlined text-[18px]">delete</span>
                )}
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
