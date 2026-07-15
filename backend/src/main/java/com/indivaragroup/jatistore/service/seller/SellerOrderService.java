package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderDetailResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderListResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface SellerOrderService {
    Page<SellerOrderListResponse> getSellerOrders(UUID sellerId, String status, int page, int size) throws CoreThrowHandler;
    SellerOrderDetailResponse getSellerOrderDetail(UUID sellerId, UUID orderId) throws CoreThrowHandler;
    void markOrderAsShipped(UUID sellerId, UUID orderId) throws CoreThrowHandler;
}
