package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.FlashSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, UUID> {
    
    long countByFlashSaleId(UUID flashSaleId);
}
