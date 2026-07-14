import api from '../../lib/api';

export interface DashboardStats {
    sellerName: string;
    totalOrders: number;
    totalProducts: number;
}

export interface SellerProfile {
    storeName: string;
    storeImage: string;
    email: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface FinancialOverview {
    availableBalance: number;
    onHoldBalance: number;
}

export interface RecentOrder {
    orderId: string;
    displayId: string;
    itemName: string;
    itemImage: string;
    amount: number;
    quantity: number;
    status: string;
}

export const dashboardService = {
    async getProfile(): Promise<SellerProfile> {
        const response = await api.get<SellerProfile>('/api/seller/dashboard/profile');
        return response.data;
    },

    async getStats(): Promise<DashboardStats> {
        const response = await api.get<DashboardStats>('/api/seller/dashboard/stats');
        return response.data;
    },

    async getFinancials(): Promise<FinancialOverview> {
        const response = await api.get<FinancialOverview>('/api/seller/dashboard/financial');
        return response.data;
    },

    async getRecentOrders(search?: string, status?: string, sortBy?: string, sortDir?: string, page?: number, limit: number = 5): Promise<PageResponse<RecentOrder>> {
        const params = new URLSearchParams();
        if (search) params.append('search', search);
        if (status) params.append('status', status);
        if (sortBy) params.append('sortBy', sortBy);
        if (sortDir) params.append('sortDir', sortDir);
        if (page !== undefined) params.append('page', page.toString());
        params.append('limit', limit.toString());

        const response = await api.get<PageResponse<RecentOrder>>(`/api/seller/dashboard/orders/recent?${params.toString()}`);
        return response.data;
    }
};
