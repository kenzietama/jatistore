package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.FlashSaleItem;
import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.repository.FlashSaleItemRepository;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaleOrderSweepServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FlashSaleItemRepository flashSaleItemRepository;

    @InjectMocks
    private StaleOrderSweepService staleOrderSweepService;

    @Test
    void expireStalePendingOrders_ShouldCancelOrders() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any()))
                .thenReturn(List.of(order));

        staleOrderSweepService.expireStalePendingOrders();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void expireStalePendingOrders_RestoresStockAndFlashQuota() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setStock(10);

        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setQuantity(2);
        detail.setFlashSale(true);

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDetails(List.of(detail));

        FlashSaleItem flashSaleItem = new FlashSaleItem();
        flashSaleItem.setId(UUID.randomUUID());
        flashSaleItem.setProduct(product);
        flashSaleItem.setRemainingQuota(5);

        when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any()))
                .thenReturn(List.of(order));
        when(flashSaleItemRepository.findByProductAndActiveFlashSaleForUpdate(productId))
                .thenReturn(Optional.of(flashSaleItem));

        staleOrderSweepService.expireStalePendingOrders();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(12, product.getStock());
        assertEquals(7, flashSaleItem.getRemainingQuota());

        verify(productRepository, times(1)).save(product);
        verify(flashSaleItemRepository, times(1)).save(flashSaleItem);
        verify(orderRepository, times(1)).save(order);
    }
}
