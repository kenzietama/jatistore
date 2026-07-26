import api from "../../lib/api";
import type { PageData, RestApiResponse } from "../seller/order.service"; // Reusing the interfaces from order.service for now, or redefine

export interface AdminDashboardResponse {
  totalUsers: number;
  totalSellers: number;
  totalProducts: number;
  totalTransactions: number;
}

export interface AdminSellerResponse {
  id: string;
  storeName: string;
  sellerName: string;
  email: string;
  productCount: number;
  active: boolean;
  joinDate: string;
}

export interface AdminCategoryResponse {
  id: string;
  name: string;
  productCount: number;
  sellerCount: number;
}

export interface AuditTrailResponse {
  id: string;
  userId: string | null;
  username: string | null;
  userRole: string | null;
  action: string;
  affectedModule: string;
  entityId: string | null;
  description: string;
  payload: string | null;
  ipAddress: string;
  createdAt: string;
}

export interface CategoryRequest {
  name: string;
}

export interface UpdateSellerStatusRequest {
  active: boolean;
  deactivationReason?: string;
}

export const adminService = {
  getDashboardSummary: async (): Promise<AdminDashboardResponse> => {
    const response = await api.get<RestApiResponse<AdminDashboardResponse>>("/api/v1/admin/dashboard");
    return response.data.data;
  },

  getSellers: async (page: number = 0, size: number = 20, status?: string, search?: string, category?: string): Promise<PageData<AdminSellerResponse>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (status) params.append('status', status);
    if (search) params.append('search', search);
    if (category) params.append('category', category);
    const response = await api.get<RestApiResponse<PageData<AdminSellerResponse>>>(`/api/v1/admin/sellers?${params.toString()}`);
    return response.data.data;
  },

  updateSellerStatus: async (sellerId: string, request: UpdateSellerStatusRequest): Promise<void> => {
    await api.patch<RestApiResponse<void>>(`/api/v1/admin/sellers/${sellerId}/status`, request);
  },

  getCategories: async (): Promise<AdminCategoryResponse[]> => {
    const response = await api.get<RestApiResponse<AdminCategoryResponse[]>>("/api/v1/admin/categories");
    return response.data.data;
  },

  createCategory: async (request: CategoryRequest): Promise<AdminCategoryResponse> => {
    const response = await api.post<RestApiResponse<AdminCategoryResponse>>("/api/v1/admin/categories", request);
    return response.data.data;
  },

  updateCategory: async (categoryId: string, request: CategoryRequest): Promise<void> => {
    await api.put<RestApiResponse<void>>(`/api/v1/admin/categories/${categoryId}`, request);
  },

  deleteCategory: async (categoryId: string): Promise<void> => {
    await api.delete<RestApiResponse<void>>(`/api/v1/admin/categories/${categoryId}`);
  },

  getAuditTrails: async (page: number = 0, size: number = 50, action?: string, module?: string): Promise<PageData<AuditTrailResponse>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (action && action !== 'ALL') params.append('action', action);
    if (module && module !== 'ALL') params.append('module', module);
    const response = await api.get<RestApiResponse<PageData<AuditTrailResponse>>>(`/api/v1/admin/audit-trails?${params.toString()}`);
    return response.data.data;
  }
};
