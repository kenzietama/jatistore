package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.FlashSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, UUID> {
    
    long countByFlashSaleId(UUID flashSaleId);

    boolean existsByFlashSaleIdAndProductId(UUID flashSaleId, UUID productId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(fsi) FROM FlashSaleItem fsi JOIN fsi.flashSale fs " +
           "WHERE fsi.product.id = :productId " +
           "AND fs.id != :flashSaleId " +
           "AND fs.startTime < :eventEndTime AND fs.endTime > :eventStartTime")
    long countTimeConflicts(@org.springframework.data.repository.query.Param("productId") UUID productId, 
                            @org.springframework.data.repository.query.Param("flashSaleId") UUID flashSaleId,
                            @org.springframework.data.repository.query.Param("eventStartTime") java.time.Instant eventStartTime,
                            @org.springframework.data.repository.query.Param("eventEndTime") java.time.Instant eventEndTime);

    @org.springframework.data.jpa.repository.Query("SELECT fsi FROM FlashSaleItem fsi " +
           "WHERE fsi.flashSale.id = :flashSaleId AND fsi.product.store.seller.id = :sellerId")
    org.springframework.data.domain.Page<FlashSaleItem> findByFlashSaleIdAndSellerId(
           @org.springframework.data.repository.query.Param("flashSaleId") UUID flashSaleId, 
           @org.springframework.data.repository.query.Param("sellerId") UUID sellerId, 
           org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(fsi) FROM FlashSaleItem fsi " +
           "WHERE fsi.flashSale.id = :flashSaleId AND fsi.product.store.seller.id = :sellerId")
    long countByFlashSaleIdAndSellerId(@org.springframework.data.repository.query.Param("flashSaleId") UUID flashSaleId, @org.springframework.data.repository.query.Param("sellerId") UUID sellerId);
}
