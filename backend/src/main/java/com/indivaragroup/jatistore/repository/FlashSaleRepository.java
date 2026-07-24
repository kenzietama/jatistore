package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, UUID> {

    @Query("SELECT f FROM FlashSale f WHERE " +
            "(:search = '' OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status = '' OR " +
            "(:status = 'UPCOMING' AND f.startTime > :now) OR " +
            "(:status = 'ACTIVE' AND f.startTime <= :now AND f.endTime >= :now) OR " +
            "(:status = 'ENDED' AND f.endTime < :now))")
    Page<FlashSale> findBySearchAndStatus(@Param("search") String search, @Param("status") String status, @Param("now") Instant now, Pageable pageable);

    @Query("SELECT f FROM FlashSale f WHERE f.startTime > :now ORDER BY f.startTime ASC")
    java.util.List<FlashSale> findAvailableFlashSales(@Param("now") Instant now);

    @Query("SELECT f FROM FlashSale f WHERE f.startTime <= :now AND f.endTime >= :now ORDER BY f.startTime DESC")
    Optional<FlashSale> findActiveFlashSale(@Param("now") Instant now);
}
