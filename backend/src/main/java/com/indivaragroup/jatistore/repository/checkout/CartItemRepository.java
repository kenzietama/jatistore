package com.indivaragroup.jatistore.repository.checkout;

import com.indivaragroup.jatistore.data.entity.checkout.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    @Query(value = "SELECT COUNT(DISTINCT p.store_id) " +
            "FROM trx_cart_items ci " +
            "JOIN mst_products p ON ci.product_id = p.id " +
            "WHERE ci.id IN (:cartItemsIds)", nativeQuery = true)
    int selectDistinctStore(@Param("cartItemsIds") UUID[] cartItemsIds);

    @Query(value = "SELECT p.name AS product, (p.stock - ci.quantity >= 0) AS available " +
            "FROM trx_cart_items ci " +
            "JOIN mst_products p ON ci.product_id = p.id " +
            "WHERE ci.id IN (:carItemsIds)", nativeQuery = true)
    Map<String, Boolean> isStockAvailable(@Param("cartItemsIds") UUID[] cartItemsIds);

    @Query(value = "SELECT COALESCE(SUM(p.price * ci.quantity), 0.0000) " +
            "FROM trx_cart_items ci " +
            "JOIN mst_products p ON ci.product_id = p.id " +
            "WHERE ci.id IN (:cartItemsIds)", nativeQuery = true)
    BigDecimal calculateTotalAmount(@Param("cartItemsIds") UUID[] cartItemsIds);
}
