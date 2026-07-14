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

const API_BASE = 'http://localhost:8080/api/v1/seller/products';
const UTILITY_API = 'http://localhost:8080/api/v1/utility';

export const productService = {
    async uploadImage(file: File): Promise<string> {
        const formData = new FormData();
        formData.append('file', file);
        const response = await fetch(`${UTILITY_API}/upload-image`, {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) throw new Error('Failed to upload image');
        const json: ApiResponse<{url: string}> = await response.json();
        return json.data.url;
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

        const response = await fetch(`${API_BASE}?${params.toString()}`);
        if (!response.ok) throw new Error('Failed to fetch products');
        const json: ApiResponse<PageData<Product>> = await response.json();
        return json.data;
    },

    async createProduct(data: Partial<Product> & { productCategoryId: string }): Promise<Product> {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!response.ok) throw new Error('Failed to create product');
        const json: ApiResponse<Product> = await response.json();
        return json.data;
    },

    async updateProduct(id: string, data: Partial<Product> & { productCategoryId?: string }): Promise<void> {
        const response = await fetch(`${API_BASE}/${id}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!response.ok) throw new Error('Failed to update product');
    },

    async deleteProduct(id: string): Promise<void> {
        const response = await fetch(`${API_BASE}/${id}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error('Failed to delete product');
    }
};
