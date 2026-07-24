package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.repository.projection.CheckoutPriceProjection;
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
            "JOIN mst_products p ON ci.product_id = p.id AND p.deleted_at IS NULL " +
            "WHERE ci.id IN (:cartItemsIds)", nativeQuery = true)
    int selectDistinctStore(@Param("cartItemsIds") UUID[] cartItemsIds);

    @Query(value = "SELECT p.name AS product, (p.stock - ci.quantity >= 0) AS available " +
            "FROM trx_cart_items ci " +
            "JOIN mst_products p ON ci.product_id = p.id AND p.deleted_at IS NULL " +
            "WHERE ci.id IN (:carItemsIds)", nativeQuery = true)
    Map<String, Boolean> isStockAvailable(@Param("cartItemsIds") UUID[] cartItemsIds);

    @Query(value = "SELECT DISTINCT ON (ci.id) " +
            "ci.id AS cartItemId, " +
            "p.id AS productId, " +
            "ci.quantity AS quantity, " +
            "CASE WHEN fs.id IS NOT NULL THEN fsi.flash_price ELSE p.price END AS effectivePrice, " +
            "CASE WHEN fs.id IS NOT NULL THEN true ELSE false END AS flashSale " +
            "FROM trx_cart_items ci " +
            "JOIN mst_products p ON ci.product_id = p.id AND p.deleted_at IS NULL " +
            "LEFT JOIN mst_flash_sale_items fsi ON fsi.product_id = p.id " +
            "LEFT JOIN mst_flash_sales fs ON fs.id = fsi.flash_sale_id " +
            "    AND NOW() BETWEEN fs.start_time AND fs.end_time " +
            "WHERE ci.id IN (:cartItemIds) " +
            "ORDER BY ci.id, fs.start_time DESC NULLS LAST", nativeQuery = true)
    List<CheckoutPriceProjection> findCheckoutPrices(@Param("cartItemIds") UUID[] cartItemIds);
}