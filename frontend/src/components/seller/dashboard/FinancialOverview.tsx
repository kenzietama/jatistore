import React, { useEffect, useState } from 'react';
import { dashboardService } from '../../../service/seller/dashboard.service';
import type { FinancialOverview as FinancialData } from '../../../service/seller/dashboard.service';

export function FinancialOverview() {
  const [financials, setFinancials] = useState<FinancialData | null>(null);

  useEffect(() => {
    dashboardService.getFinancials()
      .then(setFinancials)
      .catch(console.error);
  }, []);

  const formatMoney = (val: number | undefined) => {
    if (val === undefined) return <span className="text-[21px] font-bold text-[#0c7d73]">...</span>;
    const formatted = val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const [whole, cents] = formatted.split('.');
    return (
      <span className="text-[21px] font-bold text-[#0c7d73] leading-[1.1] tracking-[-0.01em] m-0">
        ${whole}<span className="text-[13px] font-semibold text-[#6b7876]">.{cents}</span>
      </span>
    );
  };

  return (
    <>
      {/* Available Balance */}
      <div className="bg-white border border-[#e6e9eb] rounded-[10px] py-[18px] px-[20px] flex items-start gap-[12px] shadow-sm">
        <div className="w-[34px] h-[34px] shrink-0 rounded-[8px] bg-[#e6f6f4] text-[#0c7d73] flex items-center justify-center text-[16px]">
          💳
        </div>
        <div className="min-w-0">
          <p className="text-[12.5px] font-medium text-[#6b7876] m-0 mb-[6px] whitespace-nowrap">Available Balance</p>
          {formatMoney(financials?.availableBalance)}
        </div>
      </div>

      {/* On Hold Balance */}
      <div className="bg-white border border-[#e6e9eb] rounded-[10px] py-[18px] px-[20px] flex items-start gap-[12px] shadow-sm">
        <div className="w-[34px] h-[34px] shrink-0 rounded-[8px] bg-[#e6f6f4] text-[#0c7d73] flex items-center justify-center text-[16px]">
          ⏳
        </div>
        <div className="min-w-0">
          <p className="text-[12.5px] font-medium text-[#6b7876] m-0 mb-[6px] whitespace-nowrap">On Hold Balance</p>
          {formatMoney(financials?.onHoldBalance)}
        </div>
      </div>
    </>
  );
}
