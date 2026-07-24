package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(value = """
            SELECT DISTINCT ON (p.id)
                p.id,
                p.name,
                s.store_name as storeName,
                p.description,
                COALESCE(CASE WHEN fs.id IS NOT NULL THEN fsi.flash_price ELSE NULL END, p.price) as price,
                CASE WHEN fsi.flash_price IS NOT NULL THEN p.price ELSE NULL END as originalPrice,
                p.stock,
                CASE WHEN fsi.flash_price IS NOT NULL THEN true ELSE false END as isFlashSale,
                fs.end_time as flashSaleEndTime,
                p.image,
                CAST(p.product_category_id AS VARCHAR) as categoryId
            FROM mst_products p
            LEFT JOIN mst_stores s ON s.id = p.store_id
            LEFT JOIN mst_sellers sl ON sl.id = s.seller_id
            LEFT JOIN mst_flash_sale_items fsi ON fsi.product_id = p.id
            LEFT JOIN mst_flash_sales fs ON fs.id = fsi.flash_sale_id
                AND NOW() BETWEEN fs.start_time AND fs.end_time
            WHERE p.deleted_at IS NULL
                AND sl.active = true
                AND (CAST(:search AS TEXT) IS NULL OR CAST(:search AS TEXT) = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))
                AND (CAST(:categoryId AS TEXT) IS NULL OR CAST(:categoryId AS TEXT) = '' OR CAST(p.product_category_id AS TEXT) = CAST(:categoryId AS TEXT))
            ORDER BY p.id, (fs.id IS NULL), fs.end_time DESC, p.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(p.id)
            FROM mst_products p
            LEFT JOIN mst_stores s ON s.id = p.store_id
            LEFT JOIN mst_sellers sl ON sl.id = s.seller_id
            WHERE p.deleted_at IS NULL
                AND sl.active = true
                AND (CAST(:search AS TEXT) IS NULL OR CAST(:search AS TEXT) = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))
                AND (CAST(:categoryId AS TEXT) IS NULL OR CAST(:categoryId AS TEXT) = '' OR CAST(p.product_category_id AS TEXT) = CAST(:categoryId AS TEXT))
            """,
            nativeQuery = true)
    Page<Object[]> findProductsWithFlashSale(@Param("search") String search, @Param("categoryId") String categoryId, Pageable pageable);

    Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

    @Query(value = """
            SELECT
                COALESCE(CASE WHEN fs.id IS NOT NULL THEN fsi.flash_price ELSE NULL END, p.price) as price,
                CASE WHEN fsi.flash_price IS NOT NULL THEN p.price ELSE NULL END as originalPrice,
                CASE WHEN fsi.flash_price IS NOT NULL THEN true ELSE false END as isFlashSale,
                fs.end_time as flashSaleEndTime
            FROM mst_products p
            LEFT JOIN mst_flash_sale_items fsi ON fsi.product_id = p.id
            LEFT JOIN mst_flash_sales fs ON fs.id = fsi.flash_sale_id
                AND NOW() BETWEEN fs.start_time AND fs.end_time
            WHERE p.id = :productId
            ORDER BY (fs.id IS NULL), fs.end_time DESC
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> getFlashSaleDetailInfo(@Param("productId") UUID productId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.seller.id = :sellerId AND p.deletedAt IS NULL")
    long countActiveProductsBySellerId(@Param("sellerId") UUID sellerId);

    long countByDeletedAtIsNull();

    long countByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    @Query("SELECT COUNT(DISTINCT p.store.id) FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
    long countDistinctStoresByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT p FROM Product p WHERE p.store.seller.id = :sellerId AND p.store.seller.active = true AND p.deletedAt IS NULL AND " +
            "(COALESCE(:search, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(p.id AS string) LIKE CONCAT('%', :search, '%')) AND " +
            "(COALESCE(:category, '') = '' OR p.category.name = :category) AND " +
            "(:minStock IS NULL OR p.stock >= :minStock) AND " +
            "(:maxStock IS NULL OR p.stock <= :maxStock)")
    Page<Product> findProductsBySellerAndFilters(
            @Param("sellerId") UUID sellerId,
            @Param("search") String search,
            @Param("category") String category,
            @Param("minStock") Integer minStock,
            @Param("maxStock") Integer maxStock,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") UUID categoryId);

    @Query(value = """
            SELECT COALESCE(CASE WHEN fs.id IS NOT NULL THEN fsi.flash_price ELSE NULL END, p.price) as current_price
            FROM mst_products p
            LEFT JOIN mst_flash_sale_items fsi ON fsi.product_id = p.id
            LEFT JOIN mst_flash_sales fs ON fs.id = fsi.flash_sale_id
                AND NOW() BETWEEN fs.start_time AND fs.end_time
            WHERE p.id = :productId
            ORDER BY (fs.id IS NULL), fs.end_time DESC
            LIMIT 1
            """, nativeQuery = true)
    BigDecimal getCurrentPrice(@Param("productId") UUID productId);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))")
    Page<Product> findProductsWithFilter(
            @Param("search") String search,
            @Param("category") String category,
            Pageable pageable
    );
}