package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.CartItem;
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

    @Query(value = "SELECT COUNT(DISTINCT p.store_id) " +
            "FROM trx_cart_items ci " +
            "JOIN mst_products p ON ci.product_id = p.id " +
            "WHERE ci.id IN (:cartItemsIds)", nativeQuery = true)
    int selectDistinctStore(@Param("cartItemsIds") UUID[] cartItemsIds);

    @Query(value = "SELECT p.name AS product, (p.stock - ci.quantity >= 0) AS available " +
            "FROM trx_cart_items ci " +
            "JOIN mst_products p ON ci.product_id = p.id " +
            "WHERE ci.id IN (:cartItemsIds)", nativeQuery = true)
    Map<String, Boolean> isStockAvailable(@Param("cartItemsIds") UUID[] cartItemsIds);

    @Query(value = "SELECT COALESCE(SUM(p.price * ci.quantity), 0.0000) " +
            "FROM trx_cart_items ci " +
            "JOIN mst_products p ON ci.product_id = p.id " +
            "WHERE ci.id IN (:cartItemsIds)", nativeQuery = true)
    BigDecimal calculateTotalAmount(@Param("cartItemsIds") UUID[] cartItemsIds);

    @Query(value = """
            SELECT\s
                ci.id as cartItemId,
                p.id as productId,
                p.name as productName,
                p.image as productImage,
                COALESCE(
                    (SELECT fsi.flash_price\s
                     FROM mst_flash_sale_items fsi\s
                     JOIN mst_flash_sales fs ON fs.id = fsi.flash_sale_id\s
                     WHERE fsi.product_id = p.id\s
                       AND NOW() BETWEEN fs.start_time AND fs.end_time\s
                     LIMIT 1),\s
                    p.price
                ) as unitPrice,
                CASE\s
                    WHEN (SELECT fsi.flash_price\s
                          FROM mst_flash_sale_items fsi\s
                          JOIN mst_flash_sales fs ON fs.id = fsi.flash_sale_id\s
                          WHERE fsi.product_id = p.id\s
                            AND NOW() BETWEEN fs.start_time AND fs.end_time\s
                          LIMIT 1) IS NOT NULL\s
                    THEN p.price\s
                    ELSE NULL\s
                END as originalPrice,
                ci.quantity,
                p.stock as maxStock,
                s.id as storeId,
                s.store_name as storeName
            FROM trx_cart_items ci
            JOIN mst_products p ON p.id = ci.product_id
            LEFT JOIN mst_stores s ON s.id = p.store_id
            WHERE ci.cart_id = :cartId AND p.deleted_at IS NULL
            """, nativeQuery = true)
    List<Object[]> getCartItemsWithFlashSale(@Param("cartId") UUID cartId);
}