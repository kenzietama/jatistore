package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderDetailResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderListResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.seller.SellerOrderService;
import com.indivaragroup.jatistore.service.seller.SellerSecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.SELLER_ORDERS_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;
    private final SellerSecurityHelper securityHelper;

    @GetMapping
    public RestApiResponse<PageData<SellerOrderListResponse>> getOrders(
            Principal principal,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws CoreThrowHandler {
            
        Page<SellerOrderListResponse> orders = sellerOrderService.getSellerOrders(securityHelper.getSellerIdFromPrincipal(principal), status, page, size);
        return RestApiResponse.success(PageData.from(orders));
    }

    @GetMapping("/{orderId}")
    public RestApiResponse<SellerOrderDetailResponse> getOrderDetail(Principal principal, @PathVariable UUID orderId) throws CoreThrowHandler {
        return RestApiResponse.success(sellerOrderService.getSellerOrderDetail(securityHelper.getSellerIdFromPrincipal(principal), orderId));
    }

    @PatchMapping("/{orderId}/ship")
    public RestApiResponse<Void> markAsShipped(Principal principal, @PathVariable UUID orderId) throws CoreThrowHandler {
        sellerOrderService.markOrderAsShipped(securityHelper.getSellerIdFromPrincipal(principal), orderId);
        return RestApiResponse.success(null);
    }
}
