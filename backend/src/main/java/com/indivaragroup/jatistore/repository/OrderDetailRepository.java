package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {
    
    @Query("SELECT COUNT(DISTINCT od.order.id) FROM OrderDetail od WHERE od.product.store.seller.id = :sellerId")
    long countDistinctOrdersBySellerId(@Param("sellerId") UUID sellerId);

    @Query("SELECT od FROM OrderDetail od WHERE od.product.store.seller.id = :sellerId " +
           "AND (COALESCE(:search, '') = '' OR LOWER(od.product.name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) OR CAST(od.order.id AS text) LIKE CONCAT('%', CAST(:search AS text), '%')) " +
           "AND (COALESCE(CAST(:status AS text), '') = '' OR od.order.status = :status)")
    Page<OrderDetail> searchAndFilterOrders(@Param("sellerId") UUID sellerId,
                                            @Param("search") String search,
                                            @Param("status") OrderStatus status,
                                            Pageable pageable);
}
