package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Optional<Seller> findByUserId(UUID userId);
    
    org.springframework.data.domain.Page<Seller> findByActive(Boolean active, org.springframework.data.domain.Pageable pageable);
}
