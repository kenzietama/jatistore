package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID> {

    Optional<PaymentCard> findByCardNumber(String cardNumber);
}
