package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    List<CartItem> findByCartId(UUID cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.product.id IN :productIds")
    List<CartItem> findByCartAndProductIdIn(@Param("cart") com.indivaragroup.jatistore.data.entity.Cart cart, @Param("productIds") List<UUID> productIds);

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