package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaleOrderSweepServiceTest {

    @Mock
    private OrderRepository orderRepository;

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
}
