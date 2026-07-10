import React from 'react';

export function FinancialOverview() {
  return (
    <div className="md:col-span-2 grid grid-cols-1 sm:grid-cols-2 gap-gutter">
      {/* Available Balance */}
      <div className="bg-surface-container-lowest p-stack-md rounded-lg border border-outline-variant shadow-[0_1px_2px_0_rgba(0,0,0,0.05)] flex flex-col justify-between">
        <div className="flex justify-between items-start mb-stack-lg">
          <div className="w-10 h-10 rounded-full bg-primary-container/20 flex items-center justify-center text-primary-container">
            <span className="material-symbols-outlined" data-icon="account_balance_wallet">account_balance_wallet</span>
          </div>
          <span className="bg-primary/10 text-primary border border-primary/20 px-2 py-1 rounded-full font-label-sm text-label-sm uppercase tracking-wider font-mono-data">READY</span>
        </div>
        <div>
          <p className="font-label-md text-label-md text-on-surface-variant mb-unit">Available Balance</p>
          <h3 className="font-display-lg text-display-lg text-on-surface">$1,240.50</h3>
        </div>
      </div>

      {/* On Hold Balance */}
      <div className="bg-surface-container-lowest p-stack-md rounded-lg border border-outline-variant shadow-[0_1px_2px_0_rgba(0,0,0,0.05)] flex flex-col justify-between relative overflow-hidden group">
        <div className="absolute -right-10 -top-10 w-32 h-32 bg-tertiary-fixed-dim/10 rounded-full blur-2xl group-hover:bg-tertiary-fixed-dim/20 transition-colors"></div>
        <div className="flex justify-between items-start mb-stack-lg relative z-10">
          <div className="w-10 h-10 rounded-full bg-surface-container-high flex items-center justify-center text-on-surface-variant">
            <span className="material-symbols-outlined" data-icon="pending">pending</span>
          </div>
          <span className="bg-surface-variant text-on-surface-variant border border-outline-variant px-2 py-1 rounded-full font-label-sm text-label-sm uppercase tracking-wider font-mono-data">PENDING CLEARANCE</span>
        </div>
        <div className="relative z-10">
          <p className="font-label-md text-label-md text-on-surface-variant mb-unit">On Hold Balance</p>
          <h3 className="font-headline-lg text-headline-lg text-on-surface">$320.00</h3>
        </div>
      </div>
    </div>
  );
}
