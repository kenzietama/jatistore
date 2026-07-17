package com.indivaragroup.jatistore.repository.checkout;

import com.indivaragroup.jatistore.data.entity.checkout.SellerLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SellerLedgerRepository extends JpaRepository<SellerLedger, UUID> {
}
