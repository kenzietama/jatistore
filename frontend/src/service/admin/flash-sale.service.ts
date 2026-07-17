import api from "../../lib/api";
import type { RestApiResponse, PageData } from "../seller/order.service";

export interface FlashSaleRequest {
  name: string;
  startTime: string;
  endTime: string;
}

export interface FlashSaleResponse {
  id: string;
  name: string;
  startTime: string;
  endTime: string;
  status: "UPCOMING" | "ACTIVE" | "ENDED";
  itemCount: number;
}

export const adminFlashSaleService = {
  getFlashSales: async (
    page: number = 0,
    size: number = 20,
    status?: string,
    search?: string,
    sortBy?: string,
    direction?: string
  ): Promise<RestApiResponse<PageData<FlashSaleResponse>>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (status) params.append("status", status);
    if (search) params.append("search", search);
    if (sortBy) params.append("sortBy", sortBy);
    if (direction) params.append("direction", direction);

    const response = await api.get(`/api/v1/admin/flash-sales?${params.toString()}`);
    return response.data;
  },

  createFlashSale: async (
    data: FlashSaleRequest
  ): Promise<RestApiResponse<FlashSaleResponse>> => {
    const response = await api.post("/api/v1/admin/flash-sales", data);
    return response.data;
  },

  getFlashSaleDetail: async (
    id: string
  ): Promise<RestApiResponse<FlashSaleResponse>> => {
    const response = await api.get(`/api/v1/admin/flash-sales/${id}`);
    return response.data;
  },

  updateFlashSale: async (
    id: string,
    data: FlashSaleRequest
  ): Promise<RestApiResponse<void>> => {
    const response = await api.put(`/api/v1/admin/flash-sales/${id}`, data);
    return response.data;
  },

  deleteFlashSale: async (id: string): Promise<RestApiResponse<void>> => {
    const response = await api.delete(`/api/v1/admin/flash-sales/${id}`);
    return response.data;
  },
};
