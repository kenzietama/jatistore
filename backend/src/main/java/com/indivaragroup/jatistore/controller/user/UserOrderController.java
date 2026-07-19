package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderConfirmReceiptResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderHistoryItemResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.user.UserOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.USER_CHECKOUT_BASE_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserOrderController {

    private final UserOrderService userOrderService;

    @GetMapping
    public RestApiResponse<Page<OrderHistoryItemResponse>> getOrderHistory(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws CoreThrowHandler {
        Pageable pageable = PageRequest.of(page, size);
        return userOrderService.getOrderHistory(userDetails.getUsername(), status, pageable);
    }

    @PostMapping(RestApiPath.USER_ORDER_CONFIRM_RECEIPT_PATH)
    public RestApiResponse<OrderConfirmReceiptResponse> confirmReceipt(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws CoreThrowHandler {
        return userOrderService.confirmReceipt(userDetails.getUsername(), orderId);
    }
}
