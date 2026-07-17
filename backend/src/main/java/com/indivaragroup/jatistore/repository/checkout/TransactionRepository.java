package com.indivaragroup.jatistore.repository.checkout;

import com.indivaragroup.jatistore.data.entity.checkout.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

}
