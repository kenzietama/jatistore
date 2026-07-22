import api from '../../lib/api';

export interface OrderItem {
    productId: string;
    productName: string;
    productImage: string;
    quantity: number;
    pricePerItem: number;
    subtotal: number;
    flashSale: boolean;
}

export interface SellerOrder {
    orderId: string;
    orderDate: string;
    customerName: string;
    totalAmount: number;
    status: string;
    items: OrderItem[];
}

export interface PageData<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

// Wrapper format response standar
export interface RestApiResponse<T> {
    code: number;
    status: string;
    message: string;
    data: T;
}

export const orderService = {
    async getOrders(status: string, search: string, sortBy: string, sortDir: string, page: number, size: number): Promise<PageData<SellerOrder>> {
        const response = await api.get<RestApiResponse<PageData<SellerOrder>>>('/api/v1/seller/orders', {
            params: { status: status || undefined, search: search || undefined, sortBy: sortBy || undefined, sortDir: sortDir || undefined, page, size }
        });
        return response.data.data;
    },
    
    async getOrderDetail(orderId: string): Promise<SellerOrder> {
        const response = await api.get<RestApiResponse<SellerOrder>>(`/api/v1/seller/orders/${orderId}`);
        return response.data.data;
    },

    async markAsShipped(orderId: string): Promise<void> {
        await api.patch<RestApiResponse<void>>(`/api/v1/seller/orders/${orderId}/ship`);
    }
};
