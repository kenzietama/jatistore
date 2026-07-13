export interface DashboardStats {
    sellerName: string;
    totalOrders: number;
    totalProducts: number;
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

const API_BASE = 'http://localhost:8080/api/seller/dashboard';

export const dashboardService = {
    async getStats(): Promise<DashboardStats> {
        const response = await fetch(`${API_BASE}/stats`);
        if (!response.ok) throw new Error('Failed to fetch stats');
        return response.json();
    },

    async getFinancials(): Promise<FinancialOverview> {
        const response = await fetch(`${API_BASE}/financial`);
        if (!response.ok) throw new Error('Failed to fetch financials');
        return response.json();
    },

    async getRecentOrders(search?: string, status?: string, sortBy?: string, sortDir?: string, page?: number, limit: number = 5): Promise<PageResponse<RecentOrder>> {
        const params = new URLSearchParams();
        if (search) params.append('search', search);
        if (status) params.append('status', status);
        if (sortBy) params.append('sortBy', sortBy);
        if (sortDir) params.append('sortDir', sortDir);
        if (page) params.append('page', page.toString());
        params.append('limit', limit.toString());

        const response = await fetch(`${API_BASE}/orders/recent?${params.toString()}`);
        if (!response.ok) throw new Error('Failed to fetch recent orders');
        return response.json();
    }
};
