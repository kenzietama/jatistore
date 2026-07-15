package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderDetailResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderItemDTO;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderListResponse;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {

    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;

    private Seller getSellerById(UUID sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller store not found"));
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
    public Page<SellerOrderListResponse> getSellerOrders(UUID sellerId, String status, int page, int size) {
        Seller seller = getSellerById(sellerId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Order> orders = orderRepository.findOrdersBySellerAndFilters(sellerId, (status == null || status.isEmpty()) ? null : status, pageRequest);
        
        return orders.map(order -> {
            List<SellerOrderItemDTO> items = mapToOrderItems(order, seller.getId());
            return SellerOrderListResponse.builder()
                    .orderId(order.getId())
                    .orderDate(order.getCreatedAt())
                    .customerName(order.getUser().getFullName())
                    .totalAmount(calculateSellerTotalAmount(items))
                    .status(order.getStatus())
                    .items(items)
                    .build();
        });
    }

    @Override
    public SellerOrderDetailResponse getSellerOrderDetail(UUID sellerId, UUID orderId) {
        Seller seller = getSellerById(sellerId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
                
        List<SellerOrderItemDTO> items = mapToOrderItems(order, seller.getId());
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: Order does not contain your products");
        }
        
        return SellerOrderDetailResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getCreatedAt())
                .customerName(order.getUser().getFullName())
                .customerEmail(order.getUser().getEmail())
                .customerPhone(order.getUser().getPhoneNumber())
                .totalAmount(calculateSellerTotalAmount(items))
                .status(order.getStatus())
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public void markOrderAsShipped(UUID sellerId, UUID orderId) {
        Seller seller = getSellerById(sellerId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
                
        boolean hasSellerProducts = order.getOrderDetails().stream()
                .anyMatch(od -> od.getProduct().getStore().getSeller().getId().equals(seller.getId()));
                
        if (!hasSellerProducts) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: Order does not contain your products");
        }
        
        if (!"PAID_ON_HOLD".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status transition");
        }
        
        orderRepository.updateOrderStatus(orderId, "SHIPPED");
    }
}
