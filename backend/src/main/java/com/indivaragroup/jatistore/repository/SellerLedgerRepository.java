package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.SellerLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SellerLedgerRepository extends JpaRepository<SellerLedger, UUID> {
}
