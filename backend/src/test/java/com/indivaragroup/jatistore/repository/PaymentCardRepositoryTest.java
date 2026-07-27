package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.PaymentCard;
import com.indivaragroup.jatistore.data.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentCardRepositoryTest {

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("carduser");
        testUser.setEmail("carduser@example.com");
        testUser.setPhoneNumber("081299998888");
        testUser.setPasswordHash("hashedpassword");
        testUser.setFullName("Card User");
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void testFindByUserIdAndDeletedAtIsNull_IgnoresSoftDeleted() {
        PaymentCard activeCard = PaymentCard.builder()
                .user(testUser)
                .cardNumber("1234567812345678")
                .cardHolderName("Active User")
                .expiryDate("12/28")
                .deletedAt(null)
                .build();

        PaymentCard deletedCard = PaymentCard.builder()
                .user(testUser)
                .cardNumber("8765432187654321")
                .cardHolderName("Deleted User")
                .expiryDate("12/28")
                .deletedAt(Instant.now())
                .build();

        entityManager.persist(activeCard);
        entityManager.persist(deletedCard);
        entityManager.flush();

        List<PaymentCard> activeCards = paymentCardRepository.findByUserIdAndDeletedAtIsNull(testUser.getId());
        assertThat(activeCards).hasSize(1);
        assertThat(activeCards.get(0).getCardNumber()).isEqualTo("1234567812345678");
    }

    @Test
    void testFindByIdAndUserIdAndDeletedAtIsNull() {
        PaymentCard activeCard = PaymentCard.builder()
                .user(testUser)
                .cardNumber("1111222233334444")
                .cardHolderName("Active Card")
                .expiryDate("10/26")
                .deletedAt(null)
                .build();

        PaymentCard deletedCard = PaymentCard.builder()
                .user(testUser)
                .cardNumber("5555666677778888")
                .cardHolderName("Deleted Card")
                .expiryDate("10/26")
                .deletedAt(Instant.now())
                .build();

        entityManager.persist(activeCard);
        entityManager.persist(deletedCard);
        entityManager.flush();

        Optional<PaymentCard> foundActive = paymentCardRepository.findByIdAndUserIdAndDeletedAtIsNull(activeCard.getId(), testUser.getId());
        assertThat(foundActive).isPresent();

        Optional<PaymentCard> foundDeleted = paymentCardRepository.findByIdAndUserIdAndDeletedAtIsNull(deletedCard.getId(), testUser.getId());
        assertThat(foundDeleted).isEmpty();
    }

    @Test
    void testFindByUserIdAndCardNumberAndDeletedAtIsNull() {
        PaymentCard activeCard = PaymentCard.builder()
                .user(testUser)
                .cardNumber("9999888877776666")
                .cardHolderName("Card Holder")
                .expiryDate("05/27")
                .deletedAt(null)
                .build();

        PaymentCard deletedCard = PaymentCard.builder()
                .user(testUser)
                .cardNumber("1122334455667788")
                .cardHolderName("Deleted Card Holder")
                .expiryDate("05/27")
                .deletedAt(Instant.now())
                .build();

        entityManager.persist(activeCard);
        entityManager.persist(deletedCard);
        entityManager.flush();

        Optional<PaymentCard> foundActive = paymentCardRepository.findByUserIdAndCardNumberAndDeletedAtIsNull(testUser.getId(), "9999888877776666");
        assertThat(foundActive).isPresent();

        Optional<PaymentCard> foundDeleted = paymentCardRepository.findByUserIdAndCardNumberAndDeletedAtIsNull(testUser.getId(), "1122334455667788");
        assertThat(foundDeleted).isEmpty();
    }
}
