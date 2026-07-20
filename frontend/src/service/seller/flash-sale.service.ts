import api from '../../lib/api';

export interface AvailableFlashSaleResponse {
    id: string;
    name: string;
    startTime: string;
    endTime: string;
    status: 'upcoming' | 'active' | 'ended';
    yourProductCount: number;
}

export interface SellerFlashSaleItemResponse {
    itemId: string;
    productId: string;
    productName: string;
    productImage: string;
    originalPrice: number;
    flashPrice: number;
    remainingQuota: number;
    discountPercentage: number;
}

export interface FlashSaleItemRequest {
    productId: string;
    flashPrice: number;
    remainingQuota: number;
}

interface ApiResponse<T> {
    code: number;
    status: string;
    message: string;
    data: T;
}

export interface SellerFlashSaleItemsWrapperResponse {
    eventId: string;
    eventName: string;
    startTime: string;
    endTime: string;
    items: SellerFlashSaleItemResponse[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export const sellerFlashSaleService = {
    async getAvailableFlashSales(): Promise<AvailableFlashSaleResponse[]> {
        const response = await api.get<ApiResponse<AvailableFlashSaleResponse[]>>('/api/v1/seller/flash-sales/available');
        return response.data.data;
    },

    async getFlashSaleItems(flashSaleId: string, page: number = 0, size: number = 20): Promise<SellerFlashSaleItemsWrapperResponse> {
        const response = await api.get<ApiResponse<SellerFlashSaleItemsWrapperResponse>>(`/api/v1/seller/flash-sales/${flashSaleId}/items`, {
            params: { page, size }
        });
        return response.data.data;
    },

    async addFlashSaleItem(flashSaleId: string, request: FlashSaleItemRequest): Promise<{ flashSaleId: string }> {
        const response = await api.post<ApiResponse<{ flashSaleId: string }>>(`/api/v1/seller/flash-sales/${flashSaleId}/items`, request);
        return response.data.data;
    },

    async removeFlashSaleItem(flashSaleId: string, productId: string): Promise<void> {
        await api.delete(`/api/v1/seller/flash-sales/${flashSaleId}/items/${productId}`);
    },

    async updateFlashSaleItem(flashSaleId: string, productId: string, data: { productId: string; flashPrice: number; remainingQuota: number }): Promise<void> {
        await api.patch(`/api/v1/seller/flash-sales/${flashSaleId}/items/${productId}`, data);
    }
};
