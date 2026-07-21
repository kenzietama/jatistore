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
}