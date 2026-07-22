package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaleOrderSweepService {

    private final OrderRepository orderRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000) // Every 5 minutes
    @Transactional
    public void expireStalePendingOrders() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(15));

        orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff)
                .forEach(order -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                    log.info("Auto-cancelled stale PENDING order: {}", order.getId());
                });
    }
}
