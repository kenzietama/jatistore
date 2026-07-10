import React from 'react';

export function RecentOrders() {
  return (
    <>
      {/* Search & Filter Bar */}
      <div className="md:col-span-3 flex items-center gap-stack-md bg-surface-container-lowest p-stack-sm rounded-lg border border-outline-variant shadow-sm w-full mb-stack-md">
        <div className="flex-grow flex items-center relative">
          <span className="material-symbols-outlined absolute left-stack-sm text-on-surface-variant">search</span>
          <input className="w-full pl-10 pr-stack-sm py-stack-sm bg-transparent border-none focus:ring-0 font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant outline-none" placeholder="Search orders by ID, item, or status..." type="text" />
        </div>
        <div className="h-6 w-px bg-outline-variant"></div>
        <button className="flex items-center gap-unit px-stack-sm py-stack-sm font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer">
          <span className="material-symbols-outlined text-[18px]">filter_list</span>
          Filters
        </button>
        <button className="flex items-center gap-unit px-stack-sm py-stack-sm font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer">
          <span className="material-symbols-outlined text-[18px]">sort</span>
          Sort
        </button>
      </div>

      {/* Orders Table */}
      <div className="md:col-span-3 bg-surface-container-lowest rounded-lg border border-outline-variant shadow-[0_1px_2px_0_rgba(0,0,0,0.05)] overflow-hidden">
        <div className="px-stack-md py-stack-sm border-b border-outline-variant flex justify-between items-center bg-surface-container-lowest">
          <h3 className="font-headline-sm text-headline-sm font-semibold text-on-surface">Recent Orders</h3>
          <a className="font-label-sm text-label-sm text-primary hover:underline" href="#">View All</a>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface font-label-sm text-label-sm text-on-surface-variant border-b border-outline-variant">
                <th className="p-stack-md font-medium">Order ID</th>
                <th className="p-stack-md font-medium">Item</th>
                <th className="p-stack-md font-medium">Amount</th>
                <th className="p-stack-md font-medium">Status</th>
              </tr>
            </thead>
            <tbody className="font-body-sm text-body-sm text-on-surface">
              <tr className="border-b border-outline-variant hover:bg-[#f0fdfa] transition-colors group cursor-pointer">
                <td className="p-stack-md font-mono-data">#ORD-9921</td>
                <td className="p-stack-md flex items-center gap-unit">
                  <img alt="Product Thumbnail" className="w-8 h-8 rounded object-cover border border-outline-variant" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCw3OzMO4sGKgAeoKTyCQD7TE7-EaROV0SSFk7n3Vtvs61iFpa5jqkQBP8a54qPhnFeUe1Tf46DAH5IsDqAmnihYE8swsawHgsK_prTQiSzDsIhB8EeMw0N9j1VH4xty2tdNqMk0wRF7Lf3q0UfzWBujZ_e7fxWXVYPaTyp7stc4B9efoyFam8VXRaS3edfkWWDXalOHNva4zb-gMxdx3xrixjHR8lospd8NaFeFmLjBAoavY0_QiJjgnkb_8mw-hEHaB-5DisT-iFF" />
                  Minimalist Ceramic Mug
                </td>
                <td className="p-stack-md font-medium">$24.00</td>
                <td className="p-stack-md">
                  <span className="inline-flex items-center gap-1 bg-surface-variant text-on-surface-variant border border-outline-variant px-2 py-0.5 rounded font-label-sm text-[11px] uppercase tracking-wider font-mono-data">
                    <span className="w-1.5 h-1.5 rounded-full bg-outline"></span>
                    PAID ON HOLD
                  </span>
                </td>
              </tr>
              <tr className="border-b border-outline-variant hover:bg-[#f0fdfa] transition-colors group cursor-pointer">
                <td className="p-stack-md font-mono-data">#ORD-9920</td>
                <td className="p-stack-md flex items-center gap-unit">
                  <img alt="Product Thumbnail" className="w-8 h-8 rounded object-cover border border-outline-variant" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBjVf_xhw_SNPY5Z2UT1HfVguFBTsWkh9ojfPOzpF_TCQBb4_ngS-r5OIzGgjWQntYDjzZIZR5e8nZpJOsw4iMKoRXf9A2YLJNiikEIMzrtoK-bQkjRaESgwhsFWLZq6iP-o3PEUOtQxH5yWIMm1WDqFQ2HRXnWiTgg-UyUjcUfd7NiOepUsVZiH7bUcCbwojzSdv7idb__eZzSNQZf16eFaTzI-Io8A32E6LIiQWd-ARBfclWzjgg3ZDM9jaYEno-Is4yiqqtfVxhf" />
                  Artisan Desk Lamp
                </td>
                <td className="p-stack-md font-medium">$89.50</td>
                <td className="p-stack-md">
                  <span className="inline-flex items-center gap-1 bg-primary/10 text-primary border border-primary/20 px-2 py-0.5 rounded font-label-sm text-[11px] uppercase tracking-wider font-mono-data">
                    <span className="w-1.5 h-1.5 rounded-full bg-primary"></span>
                    SHIPPED
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div className="mt-auto border-t border-outline-variant p-stack-sm flex items-center justify-between bg-surface-container-low">
          <span className="font-label-sm text-label-sm text-on-surface-variant">Showing 1-2 of 45 orders</span>
          <div className="flex items-center gap-unit">
            <button className="p-1 rounded text-on-surface-variant hover:bg-surface-variant disabled:opacity-50 cursor-pointer" disabled>
              <span className="material-symbols-outlined text-[20px]">chevron_left</span>
            </button>
            <button className="w-8 h-8 rounded bg-primary-container text-on-primary-container font-label-md text-label-md flex items-center justify-center cursor-pointer">1</button>
            <button className="w-8 h-8 rounded text-on-surface-variant hover:bg-surface-variant font-label-md text-label-md flex items-center justify-center transition-colors cursor-pointer">2</button>
            <button className="w-8 h-8 rounded text-on-surface-variant hover:bg-surface-variant font-label-md text-label-md flex items-center justify-center transition-colors cursor-pointer">3</button>
            <span className="text-on-surface-variant px-1">...</span>
            <button className="p-1 rounded text-on-surface-variant hover:bg-surface-variant transition-colors cursor-pointer">
              <span className="material-symbols-outlined text-[20px]">chevron_right</span>
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
