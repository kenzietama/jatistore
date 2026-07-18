package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.seller.id = :sellerId AND p.deletedAt IS NULL")
    long countActiveProductsBySellerId(@Param("sellerId") UUID sellerId);

    long countByDeletedAtIsNull();
    
    long countByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    @Query("SELECT COUNT(DISTINCT p.store.id) FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
    long countDistinctStoresByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT p FROM Product p WHERE p.store.seller.id = :sellerId AND p.deletedAt IS NULL AND " +
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
}
