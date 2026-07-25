package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.SellerLedger;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderConfirmReceiptResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderHistoryItemResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderItemResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.dto.utility.RestApiSuccess;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.SellerLedgerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOrderService {

    private final AuthRepository authRepository;
    private final OrderRepository orderRepository;
    private final SellerLedgerRepository sellerLedgerRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public RestApiResponse<Page<OrderHistoryItemResponse>> getOrderHistory(
            String email,
            OrderStatus status,
            Pageable pageable
    ) throws CoreThrowHandler {
        log.info("Fetching order history for user email: {}, status: {}", email, status);
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User with email {} not found", email);
                    return new CoreThrowHandler(RestApiError.GEN_0005);
                });

        Page<Object[]> orderPage = orderRepository.findOrderHistoryByUserId(
                user.getId(),
                status != null ? status.name() : null,
                pageable
        );

        List<OrderHistoryItemResponse> content = orderPage.getContent().stream()
                .map(row -> {
                    UUID orderId = UUID.fromString(row[0].toString());
                    Instant createdAt = (Instant) row[1];
                    BigDecimal totalAmount = (BigDecimal) row[2];
                    OrderStatus orderStatus = OrderStatus.valueOf(row[3].toString());

                    List<OrderItemResponse> items = orderRepository.findOrderItemsByOrderId(orderId).stream()
                            .map(itemRow -> OrderItemResponse.builder()
                                    .productName((String) itemRow[0])
                                    .quantity((Integer) itemRow[1])
                                    .pricePerItem((BigDecimal) itemRow[2])
                                    .isFlashSale((Boolean) itemRow[3])
                                    .imageUrl((String) itemRow[4])
                                    .build())
                            .toList();

                    return OrderHistoryItemResponse.builder()
                            .orderId(orderId)
                            .orderDate(createdAt)
                            .totalAmount(totalAmount)
                            .status(orderStatus)
                            .items(items)
                            .build();
                })
                .toList();

        Page<OrderHistoryItemResponse> page = new PageImpl<>(content, pageable, orderPage.getTotalElements());

        log.info("Successfully fetched {} order history items for user email: {}", content.size(), email);
        return RestApiResponse.<Page<OrderHistoryItemResponse>>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Order history retrieved successfully.")
                .restApiResponseData(page)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }

    @Audit(action = "ORDER_RECEIVED", affectedModule = "ORDERS", description = "User confirms order delivery")
    @Transactional
    public RestApiResponse<OrderConfirmReceiptResponse> confirmReceipt(
            String email,
            UUID orderId
    ) throws CoreThrowHandler {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.GEN_0005));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0015));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new CoreThrowHandler(RestApiError.USR_0015);
        }

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new CoreThrowHandler(RestApiError.USR_0016);
        }

        UUID sellerId = orderRepository.findSellerIdByOrderId(orderId);
        Seller seller = entityManager.getReference(Seller.class, sellerId);

        SellerLedger onHoldLedger = SellerLedger.builder()
                .seller(seller)
                .order(order)
                .amount(order.getTotalAmount().negate())
                .balanceType(BalanceType.ON_HOLD)
                .build();
        sellerLedgerRepository.save(onHoldLedger);

        SellerLedger availableLedger = SellerLedger.builder()
                .seller(seller)
                .order(order)
                .amount(order.getTotalAmount())
                .balanceType(BalanceType.AVAILABLE)
                .build();
        sellerLedgerRepository.save(availableLedger);

        order.setStatus(OrderStatus.RECEIVED);
        orderRepository.save(order);

        OrderConfirmReceiptResponse response = OrderConfirmReceiptResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .build();

        return RestApiResponse.<OrderConfirmReceiptResponse>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage(RestApiSuccess.ORDER_RECEIPT_CONFIRMED.getMessage())
                .restApiResponseData(response)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }
}
