package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant createdAtBefore);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderDetails od " +
           "WHERE od.product.store.seller.id = :sellerId " +
           "AND (COALESCE(CAST(:status AS text), '') = '' OR o.status = :status) " +
           "AND (COALESCE(:search, '') = '' OR " +
           "LOWER(CAST(o.id AS text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(od.product.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Order> findOrdersBySellerAndFilters(
            @Param("sellerId") UUID sellerId,
            @Param("status") OrderStatus status,
            @Param("search") String search,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "UPDATE trx_orders SET status = CAST(:status AS order_status) WHERE id = :orderId", nativeQuery = true)
    void updateOrderStatus(@Param("orderId") UUID orderId, @Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM trx_orders WHERE status IN ('PAID_ON_HOLD', 'SHIPPED', 'RECEIVED')", nativeQuery = true)
    long countSuccessfulTransactions();

    @Query(value = "SELECT o.id, o.created_at, o.total_amount, o.status " +
                   "FROM trx_orders o " +
                   "WHERE o.user_id = :userId " +
                   "AND (COALESCE(:status, '') = '' OR o.status = CAST(:status AS order_status)) " +
                   "ORDER BY o.created_at DESC",
           nativeQuery = true)
    Page<Object[]> findOrderHistoryByUserId(@Param("userId") UUID userId,
                                             @Param("status") String status,
                                             Pageable pageable);

    @Query(value = "SELECT p.name, od.quantity, od.price_per_item, od.flash_sale " +
                   "FROM trx_order_details od " +
                   "JOIN mst_products p ON od.product_id = p.id " +
                   "WHERE od.order_id = :orderId",
           nativeQuery = true)
    List<Object[]> findOrderItemsByOrderId(@Param("orderId") UUID orderId);

    @Query(value = "SELECT DISTINCT s.id " +
                   "FROM trx_order_details od " +
                   "JOIN mst_products p ON od.product_id = p.id " +
                   "JOIN mst_stores st ON p.store_id = st.id " +
                   "JOIN mst_sellers s ON st.seller_id = s.id " +
                   "WHERE od.order_id = :orderId",
           nativeQuery = true)
    UUID findSellerIdByOrderId(@Param("orderId") UUID orderId);
}
