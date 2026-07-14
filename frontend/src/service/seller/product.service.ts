export interface Product {
    id: string;
    name: string;
    description: string;
    image: string;
    price: number;
    stock: number;
    categoryId: string;
    categoryName: string;
    status: string;
}

export interface PageData<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface ApiResponse<T> {
    code: string;
    status: string;
    message: string;
    data: T;
    timestamp: string;
    requestId: string;
}

import api from '../../lib/api';

export const productService = {
    async uploadImage(file: File): Promise<string> {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post<ApiResponse<{url: string}>>('/api/v1/utility/upload-image', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        return response.data.data.url;
    },

    async getProducts(search?: string, category?: string, status?: string, sortBy?: string, sortDir?: string, page: number = 0, size: number = 20): Promise<PageData<Product>> {
        const params = new URLSearchParams();
        if (search) params.append('search', search);
        if (category) params.append('category', category);
        if (status) params.append('status', status);
        if (sortBy) params.append('sortBy', sortBy);
        if (sortDir) params.append('sortDir', sortDir);
        params.append('page', page.toString());
        params.append('size', size.toString());

        const response = await api.get<ApiResponse<PageData<Product>>>(`/api/v1/seller/products?${params.toString()}`);
        return response.data.data;
    },

    async createProduct(data: Partial<Product> & { productCategoryId: string }): Promise<Product> {
        const response = await api.post<ApiResponse<Product>>(`/api/v1/seller/products`, data);
        return response.data.data;
    },

    async updateProduct(id: string, data: Partial<Product> & { productCategoryId?: string }): Promise<void> {
        await api.patch(`/api/v1/seller/products/${id}`, data);
    },

    async deleteProduct(id: string): Promise<void> {
        await api.delete(`/api/v1/seller/products/${id}`);
    }
};
