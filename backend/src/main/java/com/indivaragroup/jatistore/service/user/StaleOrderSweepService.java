package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.repository.FlashSaleItemRepository;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
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
    private final ProductRepository productRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void expireStalePendingOrders() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(5));

        orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff)
                .forEach(order -> {
                    if (order.getOrderDetails() != null) {
                        order.getOrderDetails().forEach(detail -> {
                            Product product = detail.getProduct();
                            if (product != null) {
                                product.setStock(product.getStock() + detail.getQuantity());
                                productRepository.save(product);

                                if (Boolean.TRUE.equals(detail.getFlashSale())) {
                                    flashSaleItemRepository.findByProductAndActiveFlashSaleForUpdate(product.getId())
                                            .ifPresent(fsi -> {
                                                fsi.setRemainingQuota(fsi.getRemainingQuota() + detail.getQuantity());
                                                flashSaleItemRepository.save(fsi);
                                            });
                                }
                            }
                        });
                    }
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                    log.info("Auto-cancelled stale PENDING order: {}", order.getId());
                });
    }
}
