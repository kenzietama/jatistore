import { useState } from 'react';

// Dummy data for visual representation
const DUMMY_PRODUCTS = [
  {
    id: 'MUG-001-WHT',
    name: 'Minimalist Ceramic Mug',
    price: 350000,
    stock: 145,
    category: 'Home & Kitchen',
    status: 'ACTIVE',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBQ-YbdwJu72_TK677RjvyR5yxWkRA8LW5ZDD-eiujUQecHaGfbVgA_UG7xW8TY7nlALDEiqMYaVrq6yRnx6tqA9iFxZLk7QH0oTgdqiMvJ366wM_lWBRDRKYjt19KPPpQEWDG85GgLIB5RFraTWdQBbNU5YiUSttebjUS592a90BP0no7otjBdj_gkkaboGfo5FgSuSiohq04zKEmmXYFIKh85GeAVb3KzG8KGZgQY1u7YgHuJ7T_4_nEQCO_NoQebprQEVfraEEFE'
  },
  {
    id: 'KBD-PRO-DKG',
    name: 'Pro Mechanical Keyboard',
    price: 1850000,
    stock: 4,
    category: 'Electronics',
    status: 'LOW STOCK',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBikhxAB2eCvzqAh6KgEpAgcN1Arz8g3qeFp7kQkQ9f88-4l8T0IVyp7UIvFIHZ5_q_8xA4JGz_g6IKmiL1AyutvyZO5-CeTX2a8MmOvwKuPLcBMMOr-EpUu3d2hTMs2qwD7cx3A5G6BHtvXVYsuvu2MjwecFPbXHHPaUNNIDTBn-wc3sj8xYKclbZ7r8ljV9vZcb0A_yC-GHZZEfveGpJYkc8z4INvQ5FyVlZYbpCZO5SKBREhjwIMrHUWY_LqtRAv6viw2oNf0fT2'
  },
  {
    id: 'BOT-ECO-01',
    name: 'Eco Bamboo Water Bottle',
    price: 495000,
    stock: 89,
    category: 'Outdoors',
    status: 'ACTIVE',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuALr1f7NdzFX47YdeUaLYYcYSnqAe1bUgUMIrlEA9OkmJgxxg5O_TpRDCHExOstuu_8GSE4WsUj1VWcX2eP11Diqhd4z6wWqahAgi6fDKmm0s2T6_sZuHyZ8R44ScseAs8avxEdipTNls5D9-hFRoOt3g_WM2xo8TLbaH0wDN8tOQ8QmsJx17wxaPB4tFeqgvfxOojsHgDd0Luf9fd3JCpZSho5FLRTtb4Fc9F5MEvTqGZGB9hlAd1BeM1mPgocA5ZR6Gvy12tndyBS'
  },
  {
    id: 'SNK-CLS-WHT',
    name: 'Classic Canvas Sneakers',
    price: 675000,
    stock: 0,
    category: 'Footwear',
    status: 'OUT OF STOCK',
    image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCYja34gaWRm1i0eFwRrHkIL2bxa08vuaUfDzBbT4nP9oPY-QY9JaOt6dPxK0lEcLH-of7XG9A4cYnA5E3hyKL5SLJJpWJxZ7QIFQ40UQcbGnTqWTvB8xuvrsuVpFo2SaeRzTQn5RYtzNJ-lFPkDuC-LWGu8nWl0QVx5Rk0-W-qPpWgAv5nfshzpQ8WsympxrlMJqBr6npUGMHjAwmiV0vyjv0iXR6CLG0luSjHu9p-1T2AO8q-7Zy1LeTNSOZqx8JDc8FuBLVnXeLH'
  }
];

export function ProductTable() {
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [sortBy, setSortBy] = useState('');
  const [sortDir, setSortDir] = useState('');
  
  const [filterOpen, setFilterOpen] = useState(false);
  const [sortOpen, setSortOpen] = useState(false);

  return (
    <div className="bg-surface-container-lowest rounded-lg border border-outline-variant shadow-sm overflow-hidden flex-grow flex flex-col">
      
      {/* Search and Filter Bar - Inside Table Container or Above? Based on mockup it's above, but let's put it here for simplicity or split it. */}
      {/* Actually, mockup has it outside the table container. I'll include it inside the wrapper for component modularity. */}
      <div className="flex items-center gap-stack-md bg-surface-container-lowest p-stack-sm border-b border-outline-variant w-full">
        <div className="flex-grow flex items-center relative">
          <span className="material-symbols-outlined absolute left-stack-sm text-on-surface-variant">search</span>
          <input 
            className="w-full pl-10 pr-stack-sm py-stack-sm bg-transparent border-none focus:ring-0 font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant" 
            placeholder="Search products by name, ID, or category..." 
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
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
                  onClick={() => { setStatus(s); setFilterOpen(false); }}
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
                  onClick={() => { setSortBy(opt.by); setSortDir(opt.dir); setSortOpen(false); }}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Product Table */}
      <div className="overflow-x-auto w-full">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-surface-container-low border-b border-outline-variant font-label-sm text-label-sm text-on-surface-variant uppercase">
              <th className="p-stack-md font-medium whitespace-nowrap">Product</th>
              <th className="p-stack-md font-medium whitespace-nowrap">Price</th>
              <th className="p-stack-md font-medium whitespace-nowrap">Stock</th>
              <th className="p-stack-md font-medium whitespace-nowrap">Category</th>
              <th className="p-stack-md font-medium whitespace-nowrap">Status</th>
              <th className="p-stack-md font-medium whitespace-nowrap text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="font-body-sm text-body-sm text-on-surface divide-y divide-outline-variant">
            {DUMMY_PRODUCTS.map((prod) => {
              const isLowStock = prod.status === 'LOW STOCK';
              const isOutOfStock = prod.status === 'OUT OF STOCK';

              return (
                <tr key={prod.id} className={`hover:bg-primary-container/5 transition-colors group ${isLowStock ? 'bg-surface-container-lowest' : ''} ${isOutOfStock ? 'bg-surface-bright opacity-60' : ''}`}>
                  <td className="p-stack-md">
                    <div className="flex items-center gap-stack-md">
                      <div className={`w-12 h-12 rounded bg-surface-container overflow-hidden flex-shrink-0 border border-outline-variant ${isOutOfStock ? 'grayscale' : ''}`}>
                        <img alt={prod.name} className="w-full h-full object-cover" src={prod.image} />
                      </div>
                      <div className="flex flex-col">
                        <span className={`font-medium ${isOutOfStock ? 'text-on-surface-variant line-through' : ''}`}>{prod.name}</span>
                        <span className={`${isOutOfStock ? 'text-outline' : 'text-on-surface-variant'} text-xs mt-0.5`}>SKU: {prod.id}</span>
                      </div>
                    </div>
                  </td>
                  <td className={`p-stack-md font-mono-data text-mono-data font-medium ${isOutOfStock ? 'text-on-surface-variant' : ''}`}>
                    Rp {prod.price.toLocaleString('id-ID')}
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
                    {prod.category}
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
                      <button className="p-1.5 text-on-surface-variant hover:text-primary hover:bg-primary/10 rounded transition-colors" title="Edit">
                        <span className="material-symbols-outlined text-[20px]">edit</span>
                      </button>
                      <button className="p-1.5 text-on-surface-variant hover:text-error hover:bg-error/10 rounded transition-colors" title="Delete">
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
      <div className="mt-auto border-t border-outline-variant p-stack-sm flex items-center justify-between bg-surface-container-low">
        <span className="font-label-sm text-label-sm text-on-surface-variant">Showing 1-4 of 128 products</span>
        <div className="flex items-center gap-unit">
          <button className="p-1 rounded text-on-surface-variant hover:bg-surface-variant disabled:opacity-50" disabled>
            <span className="material-symbols-outlined text-[20px]">chevron_left</span>
          </button>
          <button className="w-8 h-8 rounded bg-primary-container text-on-primary-container font-label-md text-label-md flex items-center justify-center">1</button>
          <button className="w-8 h-8 rounded text-on-surface-variant hover:bg-surface-variant font-label-md text-label-md flex items-center justify-center transition-colors">2</button>
          <button className="w-8 h-8 rounded text-on-surface-variant hover:bg-surface-variant font-label-md text-label-md flex items-center justify-center transition-colors">3</button>
          <span className="text-on-surface-variant px-1">...</span>
          <button className="p-1 rounded text-on-surface-variant hover:bg-surface-variant transition-colors">
            <span className="material-symbols-outlined text-[20px]">chevron_right</span>
          </button>
        </div>
      </div>
    </div>
  );
}
