package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.FlashSaleItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, UUID> {
    
    long countByFlashSaleId(UUID flashSaleId);

    boolean existsByFlashSaleIdAndProductId(UUID flashSaleId, UUID productId);

    Optional<FlashSaleItem> findByFlashSaleIdAndProductId(UUID flashSaleId, UUID productId);

    @Query("SELECT COUNT(fsi) FROM FlashSaleItem fsi JOIN fsi.flashSale fs " +
           "WHERE fsi.product.id = :productId " +
           "AND fs.id != :flashSaleId " +
           "AND fs.startTime < :eventEndTime AND fs.endTime > :eventStartTime")
    long countTimeConflicts(@Param("productId") UUID productId, 
                            @Param("flashSaleId") UUID flashSaleId,
                            @Param("eventStartTime") java.time.Instant eventStartTime,
                            @Param("eventEndTime") java.time.Instant eventEndTime);

    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "WHERE fsi.flashSale.id = :flashSaleId AND fsi.product.store.seller.id = :sellerId")
    org.springframework.data.domain.Page<FlashSaleItem> findByFlashSaleIdAndSellerId(
           @Param("flashSaleId") UUID flashSaleId, 
           @Param("sellerId") UUID sellerId, 
           org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(fsi) FROM FlashSaleItem fsi " +
           "WHERE fsi.flashSale.id = :flashSaleId AND fsi.product.store.seller.id = :sellerId")
    long countByFlashSaleIdAndSellerId(@Param("flashSaleId") UUID flashSaleId, @Param("sellerId") UUID sellerId);

    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN fsi.flashSale fs " +
           "WHERE fsi.product.id = :productId " +
           "AND CURRENT_TIMESTAMP BETWEEN fs.startTime AND fs.endTime")
    Optional<FlashSaleItem> findByProductAndActiveFlashSale(@Param("productId") UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN fsi.flashSale fs " +
           "WHERE fsi.product.id = :productId " +
           "AND CURRENT_TIMESTAMP BETWEEN fs.startTime AND fs.endTime")
    Optional<FlashSaleItem> findByProductAndActiveFlashSaleForUpdate(@Param("productId") UUID productId);
}
