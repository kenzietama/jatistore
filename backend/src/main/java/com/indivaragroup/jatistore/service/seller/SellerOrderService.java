package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderDetailResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderListResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface SellerOrderService {
    Page<SellerOrderListResponse> getSellerOrders(UUID sellerId, String status, int page, int size);
    SellerOrderDetailResponse getSellerOrderDetail(UUID sellerId, UUID orderId);
    void markOrderAsShipped(UUID sellerId, UUID orderId);
}
