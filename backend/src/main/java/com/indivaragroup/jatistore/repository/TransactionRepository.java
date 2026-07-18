package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

}
