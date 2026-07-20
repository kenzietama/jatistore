package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderDetailResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderListResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerOrderServiceImpl sellerOrderService;

    private Seller setupMockSeller(UUID sellerId) {
        Seller seller = new Seller();
        seller.setId(sellerId);
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        return seller;
    }

    private Order setupMockOrder(UUID orderId, UUID sellerId, OrderStatus status) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(status);
        
        User user = new User();
        user.setFullName("Customer Name");
        user.setEmail("customer@test.com");
        user.setPhoneNumber("123");
        order.setUser(user);

        Seller seller = new Seller();
        seller.setId(sellerId);
        Store store = new Store();
        store.setSeller(seller);
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Prod");
        product.setStore(store);

        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setQuantity(2);
        detail.setPricePerItem(new BigDecimal("100"));
        order.setOrderDetails(List.of(detail));
        
        return order;
    }

    @Test
    void getSellerOrders_shouldReturnPage() throws CoreThrowHandler {
        UUID sellerId = UUID.randomUUID();
        setupMockSeller(sellerId);
        
        Order order = setupMockOrder(UUID.randomUUID(), sellerId, OrderStatus.SHIPPED);

        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findOrdersBySellerAndFilters(eq(sellerId), eq(OrderStatus.SHIPPED), any(Pageable.class))).thenReturn(page);

        Page<SellerOrderListResponse> result = sellerOrderService.getSellerOrders(sellerId, OrderStatus.SHIPPED, 0, 10);
        
        assertEquals(1, result.getTotalElements());
        assertEquals("SHIPPED", result.getContent().get(0).getStatus());
        assertEquals(new BigDecimal("200"), result.getContent().get(0).getTotalAmount());
    }

    @Test
    void getSellerOrders_emptyStatus_shouldReturnPage() throws CoreThrowHandler {
        UUID sellerId = UUID.randomUUID();
        setupMockSeller(sellerId);
        
        Order order = setupMockOrder(UUID.randomUUID(), sellerId, OrderStatus.SHIPPED);

        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findOrdersBySellerAndFilters(eq(sellerId), eq(null), any(Pageable.class))).thenReturn(page);

        Page<SellerOrderListResponse> resultNull = sellerOrderService.getSellerOrders(sellerId, null, 0, 10);
        
        assertEquals(1, resultNull.getTotalElements());
    }

    @Test
    void getSellerOrders_sellerNotFound_shouldThrow() {
        UUID sellerId = UUID.randomUUID();
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> sellerOrderService.getSellerOrders(sellerId, OrderStatus.SHIPPED, 0, 10));
    }

    @Test
    void getSellerOrderDetail_shouldReturnDetail() throws CoreThrowHandler {
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        setupMockSeller(sellerId);

        Order order = setupMockOrder(orderId, sellerId, OrderStatus.SHIPPED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        SellerOrderDetailResponse response = sellerOrderService.getSellerOrderDetail(sellerId, orderId);
        assertEquals(orderId, response.getOrderId());
        assertEquals(new BigDecimal("200"), response.getTotalAmount());
    }

    @Test
    void getSellerOrderDetail_notFound_shouldThrow() {
        UUID sellerId = UUID.randomUUID();
        setupMockSeller(sellerId);
        
        when(orderRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> sellerOrderService.getSellerOrderDetail(sellerId, UUID.randomUUID()));
    }

    @Test
    void markOrderAsShipped_shouldUpdateStatus() throws CoreThrowHandler {
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        setupMockSeller(sellerId);

        Order order = setupMockOrder(orderId, sellerId, OrderStatus.PAID_ON_HOLD);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        sellerOrderService.markOrderAsShipped(sellerId, orderId);
        verify(orderRepository).updateOrderStatus(orderId, "SHIPPED");
    }

    @Test
    void markOrderAsShipped_notFound_shouldThrow() {
        UUID sellerId = UUID.randomUUID();
        setupMockSeller(sellerId);
        
        when(orderRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> sellerOrderService.markOrderAsShipped(sellerId, UUID.randomUUID()));
    }

    @Test
    void markOrderAsShipped_invalidStatus_shouldThrow() {
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        setupMockSeller(sellerId);

        Order order = setupMockOrder(orderId, sellerId, OrderStatus.PENDING); // not PAID_ON_HOLD
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CoreThrowHandler.class, () -> sellerOrderService.markOrderAsShipped(sellerId, orderId));
    }

    @Test
    void getSellerOrderDetail_emptyItems_shouldThrow() {
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        setupMockSeller(sellerId);

        Order order = new Order();
        order.setId(orderId);
        order.setOrderDetails(List.of()); // empty
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CoreThrowHandler.class, () -> sellerOrderService.getSellerOrderDetail(sellerId, orderId));
    }

    @Test
    void getSellerOrderDetail_noSellerProducts_shouldThrow() {
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        setupMockSeller(sellerId);

        Order order = setupMockOrder(orderId, UUID.randomUUID(), OrderStatus.SHIPPED); // different sellerId
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CoreThrowHandler.class, () -> sellerOrderService.getSellerOrderDetail(sellerId, orderId));
    }

    @Test
    void markOrderAsShipped_noSellerProducts_shouldThrow() {
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        setupMockSeller(sellerId);

        Order order = setupMockOrder(orderId, UUID.randomUUID(), OrderStatus.PAID_ON_HOLD); // different sellerId
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(CoreThrowHandler.class, () -> sellerOrderService.markOrderAsShipped(sellerId, orderId));
    }
}
