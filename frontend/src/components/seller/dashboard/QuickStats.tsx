import React from 'react';

export function QuickStats() {
  return (
    <div className="flex flex-col gap-gutter">
      {/* Total Products */}
      <div className="bg-surface-container-lowest p-stack-md rounded-lg border border-outline-variant shadow-[0_1px_2px_0_rgba(0,0,0,0.05)] flex items-center gap-stack-md flex-1 hover:shadow-md transition-shadow cursor-pointer">
        <div className="w-12 h-12 rounded bg-surface-container flex items-center justify-center text-primary">
          <span className="material-symbols-outlined" data-icon="inventory_2">inventory_2</span>
        </div>
        <div>
          <h4 className="font-headline-md text-headline-md text-on-surface">12</h4>
          <p className="font-label-md text-label-md text-on-surface-variant">Total Products</p>
        </div>
        <div className="ml-auto">
          <span className="material-symbols-outlined text-outline-variant" data-icon="chevron_right">chevron_right</span>
        </div>
      </div>
    </div>
  );
}
