package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.store.seller.id = :sellerId AND p.deletedAt IS NULL")
    long countActiveProductsBySellerId(@Param("sellerId") UUID sellerId);
}
