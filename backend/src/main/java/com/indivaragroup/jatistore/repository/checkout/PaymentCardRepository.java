package com.indivaragroup.jatistore.repository.checkout;

import com.indivaragroup.jatistore.data.entity.checkout.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID> {

    Optional<PaymentCard> findByCardNumber(String cardNumber);
}
