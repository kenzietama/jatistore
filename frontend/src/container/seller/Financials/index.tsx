import { useEffect, useState, useCallback } from 'react';
import { SellerLayout } from '../../../components/layout/seller/SellerLayout';
import { financialsService, type SellerBalanceSummaryResponse, type SellerLedgerTransactionResponse } from '../../../service/seller/financials.service';
import type { PageResponse } from '../../../service/seller/dashboard.service';
import WithdrawalModal from './WithdrawalModal';
import SuccessModal from './SuccessModal';

export default function Financials() {
    const [balanceData, setBalanceData] = useState<SellerBalanceSummaryResponse | null>(null);
    const [transactions, setTransactions] = useState<PageResponse<SellerLedgerTransactionResponse> | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isWithdrawModalOpen, setIsWithdrawModalOpen] = useState(false);
    const [successGatewayRef, setSuccessGatewayRef] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    // Pagination & Filter State
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);
    const [size] = useState(20);
    const [typeFilter, setTypeFilter] = useState('ALL');
    const [sort, setSort] = useState('date_desc');

    const [filterOpen, setFilterOpen] = useState(false);
    const [sortOpen, setSortOpen] = useState(false);

    const fetchBalance = async () => {
        try {
            const data = await financialsService.getBalanceSummary();
            setBalanceData(data);
        } catch (err: any) {
            console.error('Failed to fetch balance', err);
        }
    };

    const fetchTransactions = useCallback(async (showLoading = true) => {
        if (showLoading) setIsLoading(true);
        setError(null);
        try {
            const data = await financialsService.getTransactionHistory(typeFilter, search, page, size, sort);
            setTransactions(data);
        } catch (err: any) {
            setError(err.message || 'Failed to fetch transaction data');
        } finally {
            if (showLoading) setIsLoading(false);
        }
    }, [page, size, typeFilter, search, sort]);

    useEffect(() => {
        fetchBalance();
        fetchTransactions(true);
        
        // Auto-refresh data every 5 seconds without showing the loading spinner
        const intervalId = setInterval(() => {
            fetchBalance();
            fetchTransactions(false);
        }, 5000);

        return () => clearInterval(intervalId);
    }, [fetchTransactions]);

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('id-ID', {
            style: 'currency',
            currency: 'IDR',
            minimumFractionDigits: 0
        }).format(amount);
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    };

    return (
        <SellerLayout>
            <div className="w-full h-full flex flex-col gap-stack-lg min-h-[calc(100vh-100px)]">
                <header className="flex justify-between items-center w-full mt-stack-md">
                    <h2 className="font-headline-lg text-[32px] font-bold text-on-surface m-0">Seller Financials</h2>
                </header>

                {error && (
                    <div className="bg-error-container text-on-error-container p-4 rounded-lg mb-6">
                        {error}
                    </div>
                )}

                {balanceData && (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-gutter shrink-0">
                        {/* Available Balance Card */}
                        <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-stack-md flex justify-between items-center shadow-sm">
                            <div>
                                <h3 className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wide mb-1">Available Balance</h3>
                                <div className="text-headline-lg font-headline-lg text-primary">
                                    {formatCurrency(balanceData.availableBalance)}
                                </div>
                            </div>
                            <button 
                                onClick={() => setIsWithdrawModalOpen(true)}
                                className="bg-primary text-on-primary px-4 py-2 rounded font-label-md text-label-md hover:bg-surface-tint transition-colors shadow-sm flex items-center gap-2 shrink-0"
                                disabled={balanceData.availableBalance <= 0}
                            >
                                <span className="material-symbols-outlined text-[18px]">account_balance_wallet</span>
                                Withdraw
                            </button>
                        </div>

                        {/* On Hold Balance Card */}
                        <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-stack-md flex flex-col justify-center shadow-sm">
                            <h3 className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wide mb-1">On Hold Balance</h3>
                            <div className="text-headline-md font-headline-md text-on-surface-variant">
                                {formatCurrency(balanceData.onHoldBalance)}
                            </div>
                        </div>
                    </div>
                )}

                {/* Transaction Table */}
                <div className="bg-surface-container-lowest border border-outline-variant rounded-lg overflow-hidden shadow-sm flex flex-col flex-grow">
                    <div className="p-stack-sm border-b border-outline-variant bg-surface-container-lowest flex items-center gap-stack-md shrink-0 w-full">
                        <div className="flex-grow flex items-center relative">
                            <span className="material-symbols-outlined absolute left-stack-sm text-on-surface-variant">search</span>
                            <input 
                                className="w-full pl-10 pr-stack-sm py-stack-sm bg-transparent border-none focus:ring-0 font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant transition-all" 
                                placeholder="Search transactions by ID or description..." 
                                type="text" 
                                value={search}
                                onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                            />
                        </div>
                        <div className="h-6 w-px bg-outline-variant"></div>

                        <div className="flex items-center gap-3">
                            {/* Filter Dropdown */}
                            <div className="relative">
                                <button 
                                    className="flex items-center gap-unit px-stack-sm py-stack-sm font-label-md text-label-md text-on-surface-variant hover:text-on-surface transition-colors"
                                    onClick={() => { setFilterOpen(!filterOpen); setSortOpen(false); }}
                                >
                                    <span className="material-symbols-outlined text-[18px]">filter_list</span>
                                    Filters {typeFilter !== 'ALL' && <span className="w-2 h-2 rounded-full bg-primary ml-1"></span>}
                                </button>
                                
                                {filterOpen && (
                                    <div className="absolute right-0 mt-2 w-56 bg-surface border border-outline-variant rounded shadow-lg z-50 py-2">
                                        <div className="px-4 py-1 text-label-sm text-on-surface-variant uppercase">Transaction Type</div>
                                        {[
                                            { val: 'ALL', label: 'All Types' },
                                            { val: 'CREDIT', label: 'Sale (Available)' },
                                            { val: 'CREDIT_ON_HOLD', label: 'Sale (On Hold)' },
                                            { val: 'TRANSFER_OUT', label: 'Funds Released' },
                                            { val: 'DEBIT', label: 'Withdrawal' }
                                        ].map((opt) => (
                                            <button
                                                key={opt.val}
                                                className={`w-full text-left px-4 py-2 font-body-sm text-body-sm hover:bg-surface-container transition-colors ${typeFilter === opt.val ? 'text-primary bg-primary-container/10 font-medium' : 'text-on-surface'}`}
                                                onClick={() => { setTypeFilter(opt.val); setPage(0); setFilterOpen(false); }}
                                            >
                                                {opt.label}
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
                                            { val: 'date_desc', label: 'Newest First' },
                                            { val: 'date_asc', label: 'Oldest First' },
                                            { val: 'amount_desc', label: 'Highest Amount' },
                                            { val: 'amount_asc', label: 'Lowest Amount' },
                                        ].map((opt) => (
                                            <button
                                                key={opt.val}
                                                className={`w-full text-left px-4 py-2 font-body-sm text-body-sm hover:bg-surface-container transition-colors ${sort === opt.val ? 'text-primary bg-primary-container/10 font-medium' : 'text-on-surface'}`}
                                                onClick={() => { setSort(opt.val); setPage(0); setSortOpen(false); }}
                                            >
                                                {opt.label}
                                            </button>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                    
                    <div className="overflow-x-auto flex-1">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-surface-container text-on-surface-variant text-label-sm font-label-sm border-b border-outline-variant">
                                    <th className="py-3 px-4 font-medium uppercase">Date</th>
                                    <th className="py-3 px-4 font-medium uppercase">Type</th>
                                    <th className="py-3 px-4 font-medium uppercase">Description</th>
                                    <th className="py-3 px-4 font-medium uppercase text-right">Amount</th>
                                </tr>
                            </thead>
                            <tbody className="text-body-sm font-body-sm">
                                {isLoading ? (
                                    <tr>
                                        <td colSpan={4} className="py-8 text-center text-on-surface-variant">
                                            <div className="animate-spin inline-block w-6 h-6 border-2 border-primary border-t-transparent rounded-full"></div>
                                        </td>
                                    </tr>
                                ) : !transactions || transactions.content.length === 0 ? (
                                    <tr>
                                        <td colSpan={4} className="py-8 text-center text-on-surface-variant">
                                            No recent transactions
                                        </td>
                                    </tr>
                                ) : (
                                    transactions.content.map((trx, index) => (
                                        <tr key={trx.id} className={`border-b border-outline-variant hover:bg-[#f0fdfa] transition-colors group ${index % 2 === 1 ? 'bg-[#f8fafc]' : ''}`}>
                                            <td className="py-3 px-4 text-on-surface-variant font-mono-data text-mono-data">
                                                {formatDate(trx.createdAt)}
                                            </td>
                                            <td className="py-3 px-4">
                                                <span className="inline-flex items-center gap-1 bg-surface-container-high px-2 py-1 rounded text-label-sm font-label-sm text-on-surface">
                                                    <span className="material-symbols-outlined text-[16px]" data-icon={
                                                        trx.type === 'credit' ? 'shopping_cart' :
                                                        trx.type === 'credit_on_hold' ? 'pending_actions' :
                                                        trx.type === 'transfer_out' ? 'swap_horiz' :
                                                        'account_balance'
                                                    }>
                                                        {
                                                            trx.type === 'credit' ? 'shopping_cart' :
                                                            trx.type === 'credit_on_hold' ? 'pending_actions' :
                                                            trx.type === 'transfer_out' ? 'swap_horiz' :
                                                            'account_balance'
                                                        }
                                                    </span> 
                                                    {
                                                        trx.type === 'credit' ? 'Sale (Available)' :
                                                        trx.type === 'credit_on_hold' ? 'Sale (On Hold)' :
                                                        trx.type === 'transfer_out' ? 'Funds Released' :
                                                        'Withdrawal'
                                                    }
                                                </span>
                                            </td>
                                            <td className="py-3 px-4 text-on-surface-variant font-mono-data text-mono-data">
                                                {trx.description}
                                            </td>
                                            <td className={`py-3 px-4 text-right font-medium font-mono-data text-mono-data ${trx.type.startsWith('credit') ? 'text-primary' : trx.type === 'transfer_out' ? 'text-on-surface-variant' : 'text-error'}`}>
                                                {trx.type.startsWith('credit') ? '+' : ''}{formatCurrency(trx.amount)}
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>

                    {/* Pagination Footer */}
                    {transactions && (() => {
                        const totalElements = (transactions as any)?.page?.totalElements ?? transactions?.totalElements ?? 0;
                        const totalPages = (transactions as any)?.page?.totalPages ?? transactions?.totalPages ?? 1;
                        return (
                            <div className="border-t border-outline-variant p-stack-sm flex items-center justify-between bg-surface-container-low shrink-0">
                                <span className="font-label-sm text-label-sm text-on-surface-variant">
                                    Showing {totalElements ? (page * size) + 1 : 0}-{Math.min((page + 1) * size, totalElements)} of {totalElements} transactions
                                </span>
                                <div className="flex items-center gap-unit">
                                    <button 
                                        disabled={page === 0}
                                        onClick={() => setPage(p => p - 1)}
                                        className="p-2 border border-outline-variant rounded-lg disabled:opacity-50 hover:bg-surface-tint transition-colors"
                                    >
                                        <span className="material-symbols-outlined text-[18px] text-on-surface">chevron_left</span>
                                    </button>
                                    <span className="text-body-sm font-body-sm text-on-surface px-2">
                                        Page {page + 1} of {totalPages}
                                    </span>
                                    <button 
                                        disabled={page + 1 >= totalPages}
                                        onClick={() => setPage(p => p + 1)}
                                        className="p-2 border border-outline-variant rounded-lg disabled:opacity-50 hover:bg-surface-tint transition-colors"
                                    >
                                        <span className="material-symbols-outlined text-[18px] text-on-surface">chevron_right</span>
                                    </button>
                                </div>
                            </div>
                        );
                    })()}
                </div>
            </div>

            {isWithdrawModalOpen && balanceData && (
                <WithdrawalModal 
                    availableBalance={balanceData.availableBalance}
                    onClose={() => setIsWithdrawModalOpen(false)}
                    onSuccess={(gatewayRef) => {
                        setIsWithdrawModalOpen(false);
                        setSuccessGatewayRef(gatewayRef);
                        fetchBalance();
                        fetchTransactions(false);
                    }}
                />
            )}

            {successGatewayRef && (
                <SuccessModal 
                    gatewayRef={successGatewayRef}
                    onClose={() => setSuccessGatewayRef(null)}
                />
            )}
        </SellerLayout>
    );
}
