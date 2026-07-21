import api from '../../lib/api';
import type { PageResponse, ApiResponse } from './dashboard.service';

export interface SellerLedgerTransactionResponse {
    id: string;
    type: string;
    amount: number;
    balanceType: string;
    orderId: string | null;
    description: string;
    createdAt: string;
}

export interface SellerFinancialDashboardResponse {
    availableBalance: number;
    onHoldBalance: number;
    totalEarnings: number;
    recentTransactions: SellerLedgerTransactionResponse[];
}

export interface SellerBalanceSummaryResponse {
    availableBalance: number;
    onHoldBalance: number;
    totalEarnings: number;
}

export interface SellerWithdrawalRequest {
    amount: number;
}

export interface SellerWithdrawalResponse {
    amount: number;
    newAvailableBalance: number;
    withdrawalId: string;
    mockGatewayRef: string;
}

export const financialsService = {
    async getDashboard(): Promise<SellerFinancialDashboardResponse> {
        const response = await api.get<ApiResponse<SellerFinancialDashboardResponse>>('/api/v1/seller/financials');
        return response.data.data;
    },

    async getBalanceSummary(): Promise<SellerBalanceSummaryResponse> {
        const response = await api.get<ApiResponse<SellerBalanceSummaryResponse>>('/api/v1/seller/financials/balance');
        return response.data.data;
    },

    async getTransactionHistory(type?: string, page: number = 0, size: number = 20): Promise<PageResponse<SellerLedgerTransactionResponse>> {
        const params = new URLSearchParams();
        if (type) params.append('type', type);
        params.append('page', page.toString());
        params.append('size', size.toString());

        const response = await api.get<ApiResponse<PageResponse<SellerLedgerTransactionResponse>>>(`/api/v1/seller/financials/transactions?${params.toString()}`);
        return response.data.data;
    },

    async simulateWithdrawal(request: SellerWithdrawalRequest): Promise<SellerWithdrawalResponse> {
        const response = await api.post<ApiResponse<SellerWithdrawalResponse>>('/api/v1/seller/financials/withdraw', request);
        return response.data.data;
    }
};
