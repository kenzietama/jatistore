import React from 'react';

export function SellerSidebar() {
  return (
    <nav className="fixed left-0 top-0 flex-col p-stack-md z-40 h-full w-[240px] bg-surface-container dark:bg-inverse-surface border-r border-outline-variant dark:border-outline hidden md:flex">
      {/* Header */}
      <div className="mb-stack-lg">
        <div className="flex items-center gap-unit mb-unit">
          <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-on-primary">
            <span className="material-symbols-outlined" data-icon="storefront" data-weight="fill">storefront</span>
          </div>
          <h1 className="text-headline-sm font-bold text-primary dark:text-inverse-primary font-headline-sm">Store Backoffice</h1>
        </div>
        <span className="text-label-sm font-label-sm font-mono-data text-on-surface-variant px-2 py-1 rounded bg-surface-variant/50 inline-block">SELLER</span>
      </div>

      {/* Navigation Links */}
      <ul className="space-y-unit flex-1">
        {/* Active Tab: Dashboard */}
        <li>
          <a className="flex items-center gap-stack-sm p-2 bg-secondary-container dark:bg-secondary text-on-secondary-container dark:text-on-secondary rounded-lg scale-[0.98] transition-transform" href="#">
            <span className="material-symbols-outlined text-on-secondary-container" data-icon="dashboard">dashboard</span>
            <span className="font-label-md text-label-md">Dashboard</span>
          </a>
        </li>
        {/* Inactive Tabs */}
        <li>
          <a className="flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-colors" href="#">
            <span className="material-symbols-outlined" data-icon="inventory_2">inventory_2</span>
            <span className="font-label-md text-label-md">Products</span>
          </a>
        </li>
        <li>
          <a className="flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-colors" href="#">
            <span className="material-symbols-outlined" data-icon="shopping_bag">shopping_bag</span>
            <span className="font-label-md text-label-md">Orders</span>
          </a>
        </li>
        <li>
          <a className="flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-colors" href="#">
            <span className="material-symbols-outlined" data-icon="bolt">bolt</span>
            <span className="font-label-md text-label-md">Flash Sale</span>
          </a>
        </li>
        <li>
          <a className="flex items-center gap-stack-sm p-2 text-on-surface-variant dark:text-surface-variant hover:bg-surface-container-high dark:hover:bg-surface-variant rounded-lg hover:bg-surface-container-highest dark:hover:bg-surface-dim transition-colors" href="#">
            <span className="material-symbols-outlined" data-icon="payments">payments</span>
            <span className="font-label-md text-label-md">Financials</span>
          </a>
        </li>
      </ul>

      {/* Bottom Profile */}
      <div className="mt-auto pt-stack-md border-t border-outline-variant">
        <div className="flex items-center gap-stack-sm">
          <img alt="Seller Profile" className="w-10 h-10 rounded-full object-cover border border-outline-variant" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCkIs4AXIbS0ZETMjiEGEQ9nUPCyWmF3KB0zxowwVVxiVOyxu85qfo7ws7eo9Hsy8OL5FqO1z2AgGHrYnpiZdHE8kEpZUd33g4PKkvSE-5NhMSHnGPeyrGSDU7-SmWtdiItMyQlEAicDATJWbIel1PjpsMJzYLrG0daUsFgO5Rn4zq2ihS2SsYmDoHPiZl3gAIKdpHM8QTRE74f-C9-2zkL3mWb6QLfchLiqhpa8PS7-Bmv0YKhKArhlLyqSyvc7rBjfE83xhQaHfxf" />
          <div className="flex flex-col">
            <span className="font-label-md text-label-md text-on-surface">Alex Mercer</span>
            <span className="font-label-sm text-label-sm text-on-surface-variant cursor-pointer hover:underline">View Profile</span>
          </div>
        </div>
      </div>
    </nav>
  );
}
