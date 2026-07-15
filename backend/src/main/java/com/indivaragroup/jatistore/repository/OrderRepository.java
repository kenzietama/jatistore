package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderDetails od " +
           "WHERE od.product.store.seller.id = :sellerId " +
           "AND (:status IS NULL OR CAST(o.status AS text) = CAST(:status AS text))")
    Page<Order> findOrdersBySellerAndFilters(
            @Param("sellerId") UUID sellerId,
            @Param("status") String status,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "UPDATE trx_orders SET status = CAST(:status AS order_status) WHERE id = :orderId", nativeQuery = true)
    void updateOrderStatus(@Param("orderId") UUID orderId, @Param("status") String status);
}
