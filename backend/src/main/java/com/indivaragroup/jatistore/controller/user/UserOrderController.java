package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.request.user.CreateOrderRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.CreateOrderResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderConfirmReceiptResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderHistoryItemResponse;
import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.user.UserCheckoutService;
import com.indivaragroup.jatistore.service.user.UserOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.USER_ORDER_BASE_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserOrderController {

    private final UserOrderService userOrderService;
    private final UserCheckoutService userCheckoutService;

    @Audit(action = "ORDER_CREATE", affectedModule = "ORDERS", description = "User created an order")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestApiResponse<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest,
            Principal principal
    ) throws CoreThrowHandler {
        return userCheckoutService.createOrder(createOrderRequest, principal.getName());
    }

    @Audit(action = "ORDER_HISTORY_FETCH", affectedModule = "ORDERS", description = "User fetched order history")
    @GetMapping
    public RestApiResponse<Page<OrderHistoryItemResponse>> getOrderHistory(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal
    ) throws CoreThrowHandler {
        Pageable pageable = PageRequest.of(page, size);
        return userOrderService.getOrderHistory(principal.getName(), status, pageable);
    }

    @Audit(action = "ORDER_CONFIRM_RECEIPT", affectedModule = "ORDERS", description = "User confirmed receipt of order")
    @PostMapping(RestApiPath.USER_ORDER_CONFIRM_RECEIPT_PATH)
    public RestApiResponse<OrderConfirmReceiptResponse> confirmReceipt(
            @PathVariable UUID orderId,
            Principal principal
    ) throws CoreThrowHandler {
        return userOrderService.confirmReceipt(principal.getName(), orderId);
    }
}
