import React from 'react';

export function SellerHeader() {
  return (
    <header className="flex md:hidden justify-between items-center mb-stack-lg bg-surface border-b border-outline-variant pb-stack-sm">
      <div className="flex items-center gap-unit">
        <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-on-primary">
          <span className="material-symbols-outlined" data-icon="storefront" data-weight="fill">storefront</span>
        </div>
        <h1 className="font-headline-md text-headline-md font-bold text-primary">Store Backoffice</h1>
      </div>
      <button className="text-on-surface">
        <span className="material-symbols-outlined" data-icon="menu">menu</span>
      </button>
    </header>
  );
}
