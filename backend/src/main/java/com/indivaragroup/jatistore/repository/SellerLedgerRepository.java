package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.SellerLedger;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SellerLedgerRepository extends JpaRepository<SellerLedger, UUID> {
    Page<SellerLedger> findBySellerIdAndBalanceTypeOrderByCreatedAtDescAmountDesc(UUID sellerId, BalanceType balanceType, Pageable pageable);
    Page<SellerLedger> findBySellerIdOrderByCreatedAtDescAmountDesc(UUID sellerId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT l FROM SellerLedger l LEFT JOIN l.order o WHERE l.seller.id = :sellerId " +
            "AND (:type = 'ALL' OR " +
            "(:type = 'CREDIT' AND l.balanceType = com.indivaragroup.jatistore.data.utility.constant.BalanceType.AVAILABLE AND l.amount > 0) OR " +
            "(:type = 'CREDIT_ON_HOLD' AND l.balanceType = com.indivaragroup.jatistore.data.utility.constant.BalanceType.ON_HOLD AND l.amount > 0) OR " +
            "(:type = 'TRANSFER_OUT' AND l.balanceType = com.indivaragroup.jatistore.data.utility.constant.BalanceType.ON_HOLD AND l.amount < 0) OR " +
            "(:type = 'DEBIT' AND l.balanceType = com.indivaragroup.jatistore.data.utility.constant.BalanceType.AVAILABLE AND l.amount < 0)) " +
            "AND (:search = '' OR LOWER(CAST(l.id AS string)) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(CAST(o.id AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SellerLedger> findTransactionsBySellerId(
            @org.springframework.data.repository.query.Param("sellerId") UUID sellerId,
            @org.springframework.data.repository.query.Param("type") String type,
            @org.springframework.data.repository.query.Param("search") String search,
            Pageable pageable
    );
}