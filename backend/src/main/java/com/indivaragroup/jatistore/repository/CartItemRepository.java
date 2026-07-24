package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.repository.projection.CheckoutPriceProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
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

    @Query(value = "SELECT COALESCE(SUM(p.price * ci.quantity), 0.0000) " +
            "FROM trx_cart_items ci " +
            "JOIN mst_products p ON ci.product_id = p.id " +
            "WHERE ci.id IN (:cartItemsIds)", nativeQuery = true)
    BigDecimal calculateTotalAmount(@Param("cartItemsIds") UUID[] cartItemsIds);

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

    @Query(value = """
            SELECT 
                ci.id as cartItemId,
                p.id as productId,
                p.name as productName,
                p.image as productImage,
                COALESCE(
                    (SELECT fsi.flash_price 
                     FROM mst_flash_sale_items fsi 
                     JOIN mst_flash_sales fs ON fs.id = fsi.flash_sale_id 
                     WHERE fsi.product_id = p.id 
                       AND NOW() BETWEEN fs.start_time AND fs.end_time 
                     LIMIT 1), 
                    p.price
                ) as unitPrice,
                CASE 
                    WHEN (SELECT fsi.flash_price 
                          FROM mst_flash_sale_items fsi 
                          JOIN mst_flash_sales fs ON fs.id = fsi.flash_sale_id 
                          WHERE fsi.product_id = p.id 
                            AND NOW() BETWEEN fs.start_time AND fs.end_time 
                          LIMIT 1) IS NOT NULL 
                    THEN p.price 
                    ELSE NULL 
                END as originalPrice,
                ci.quantity,
                p.stock as maxStock,
                s.id as storeId,
                s.store_name as storeName,
                sl.active as sellerActive
            FROM trx_cart_items ci
            JOIN mst_products p ON p.id = ci.product_id
            LEFT JOIN mst_stores s ON s.id = p.store_id
            LEFT JOIN mst_sellers sl ON sl.id = s.seller_id
            WHERE ci.cart_id = :cartId AND p.deleted_at IS NULL
            """, nativeQuery = true)
    List<Object[]> getCartItemsWithFlashSale(@Param("cartId") UUID cartId);
}