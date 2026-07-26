package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderDetailResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderItemDTO;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderListResponse;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerOrderServiceImpl implements SellerOrderService {

    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;

    private @NonNull Seller getSellerById(UUID sellerId) throws CoreThrowHandler {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> {
                    log.error("Seller not found for ID: {}", sellerId);
                    return new CoreThrowHandler(RestApiError.SLR_0002);
                });
    }

    private List<SellerOrderItemDTO> mapToOrderItems(Order order, UUID sellerId) {
        return order.getOrderDetails().stream()
                .filter(od -> od.getProduct().getStore().getSeller().getId().equals(sellerId))
                .map(od -> SellerOrderItemDTO.builder()
                        .productId(od.getProduct().getId())
                        .productName(od.getProduct().getName())
                        .productImage(od.getProduct().getImage())
                        .quantity(od.getQuantity())
                        .pricePerItem(od.getPricePerItem())
                        .subtotal(od.getPricePerItem().multiply(new BigDecimal(od.getQuantity())))
                        .flashSale(od.getFlashSale())
                        .build())
                .toList();
    }

    private BigDecimal calculateSellerTotalAmount(List<SellerOrderItemDTO> items) {
        return items.stream()
                .map(SellerOrderItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Page<SellerOrderListResponse> getSellerOrders(UUID sellerId, OrderStatus status, String search, String sortBy, String sortDir, int page, int size) throws CoreThrowHandler {
        log.info("Fetching orders for seller ID: {}", sellerId);
        Seller seller = getSellerById(sellerId);
        
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = "createdAt";
        if ("amount".equalsIgnoreCase(sortBy)) {
            sortProperty = "totalAmount";
        } else if ("status".equalsIgnoreCase(sortBy)) {
            sortProperty = "status";
        }
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortProperty));
        
        Page<Order> orders = orderRepository.findOrdersBySellerAndFilters(sellerId, status, search, pageRequest);
        
        return orders.map(order -> {
            List<SellerOrderItemDTO> items = mapToOrderItems(order, seller.getId());
            return SellerOrderListResponse.builder()
                    .orderId(order.getId())
                    .orderDate(order.getCreatedAt())
                    .customerName(order.getUser().getFullName())
                    .totalAmount(calculateSellerTotalAmount(items))
                    .status(order.getStatus() != null ? order.getStatus().name() : null)
                    .items(items)
                    .build();
        });
    }

    @Override
    public SellerOrderDetailResponse getSellerOrderDetail(UUID sellerId, UUID orderId) throws CoreThrowHandler {
        log.info("Fetching order detail. sellerId: {}, orderId: {}", sellerId, orderId);
        Seller seller = getSellerById(sellerId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order {} not found", orderId);
                    return new CoreThrowHandler(RestApiError.USR_0015);
                });
                
        List<SellerOrderItemDTO> items = mapToOrderItems(order, seller.getId());
        if (items.isEmpty()) {
            log.warn("Order {} has no items for seller {}", orderId, sellerId);
            throw new CoreThrowHandler(RestApiError.SLR_0021);
        }
        
        return SellerOrderDetailResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getCreatedAt())
                .customerName(order.getUser().getFullName())
                .customerEmail(order.getUser().getEmail())
                .customerPhone(order.getUser().getPhoneNumber())
                .totalAmount(calculateSellerTotalAmount(items))
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public void markOrderAsShipped(UUID sellerId, UUID orderId) throws CoreThrowHandler {
        log.info("Marking order {} as shipped for seller {}", orderId, sellerId);
        Seller seller = getSellerById(sellerId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order {} not found", orderId);
                    return new CoreThrowHandler(RestApiError.USR_0015);
                });
                
        boolean hasSellerProducts = order.getOrderDetails().stream()
                .anyMatch(od -> od.getProduct().getStore().getSeller().getId().equals(seller.getId()));
                
        if (!hasSellerProducts) {
            log.error("Order {} does not contain products belonging to seller {}", orderId, sellerId);
            throw new CoreThrowHandler(RestApiError.SLR_0021);
        }
        
        if (order.getStatus() != OrderStatus.PAID_ON_HOLD) {
            log.error("Order {} is not in PAID_ON_HOLD status. Current status: {}", orderId, order.getStatus());
            throw new CoreThrowHandler(RestApiError.SLR_0020);
        }
        
        orderRepository.updateOrderStatus(orderId, OrderStatus.SHIPPED.name());
        log.info("Successfully marked order {} as shipped", orderId);
    }
}
