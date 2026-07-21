import { useEffect, useState } from 'react';
import { SellerLayout } from '../../../components/layout/seller/SellerLayout';
import { financialsService, type SellerFinancialDashboardResponse } from '../../../service/seller/financials.service';
import WithdrawalModal from './WithdrawalModal';

export default function Financials() {
    const [dashboardData, setDashboardData] = useState<SellerFinancialDashboardResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isWithdrawModalOpen, setIsWithdrawModalOpen] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchDashboard = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await financialsService.getDashboard();
            setDashboardData(data);
        } catch (err: any) {
            setError(err.message || 'Failed to fetch financial data');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchDashboard();
    }, []);

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
            year: 'numeric'
        }).format(date);
    };

    return (
        <SellerLayout>
            <header className="mb-stack-lg">
                <h2 className="text-headline-md font-headline-md text-on-surface mb-2">Financials</h2>
                <p className="text-body-sm font-body-sm text-on-surface-variant">Manage your earnings, withdrawals, and transaction history.</p>
            </header>

            {isLoading && !dashboardData ? (
                <div className="flex justify-center py-10">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                </div>
            ) : error ? (
                <div className="bg-error-container text-on-error-container p-4 rounded-lg mb-6">
                    {error}
                </div>
            ) : dashboardData ? (
                <>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-gutter mb-stack-lg">
                        {/* Available Balance Card */}
                        <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-stack-md flex flex-col justify-between shadow-sm">
                            <div>
                                <h3 className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wide mb-1">Available Balance</h3>
                                <div className="text-headline-lg font-headline-lg text-primary">
                                    {formatCurrency(dashboardData.availableBalance)}
                                </div>
                            </div>
                            <div className="mt-stack-md flex justify-end">
                                <button 
                                    onClick={() => setIsWithdrawModalOpen(true)}
                                    className="bg-primary text-on-primary px-4 py-2 rounded font-label-md text-label-md hover:bg-surface-tint transition-colors shadow-sm"
                                    disabled={dashboardData.availableBalance <= 0}
                                >
                                    Withdraw
                                </button>
                            </div>
                        </div>

                        {/* On Hold Balance Card */}
                        <div className="bg-surface-container-lowest border border-outline-variant rounded-lg p-stack-md flex flex-col justify-between shadow-sm">
                            <div>
                                <h3 className="text-label-sm font-label-sm text-on-surface-variant uppercase tracking-wide mb-1">On Hold Balance</h3>
                                <div className="text-headline-md font-headline-md text-on-surface-variant">
                                    {formatCurrency(dashboardData.onHoldBalance)}
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Transaction Table */}
                    <div className="bg-surface-container-lowest border border-outline-variant rounded-lg overflow-hidden shadow-sm">
                        <div className="p-4 border-b border-outline-variant bg-surface-container-low flex justify-between items-center">
                            <h3 className="text-label-md font-label-md font-semibold text-on-surface">Recent Transactions</h3>
                        </div>
                        <div className="overflow-x-auto">
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
                                    {dashboardData.recentTransactions.length === 0 ? (
                                        <tr>
                                            <td colSpan={4} className="py-8 text-center text-on-surface-variant">
                                                No recent transactions
                                            </td>
                                        </tr>
                                    ) : (
                                        dashboardData.recentTransactions.map((trx, index) => (
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
                    </div>
                </>
            ) : null}

            {isWithdrawModalOpen && dashboardData && (
                <WithdrawalModal 
                    availableBalance={dashboardData.availableBalance}
                    onClose={() => setIsWithdrawModalOpen(false)}
                    onSuccess={() => {
                        setIsWithdrawModalOpen(false);
                        fetchDashboard();
                    }}
                />
            )}
        </SellerLayout>
    );
}
