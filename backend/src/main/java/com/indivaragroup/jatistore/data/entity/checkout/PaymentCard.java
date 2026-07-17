package com.indivaragroup.jatistore.data.entity.checkout;

import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.data.utility.table.schema.PaymentCardVariable;
import com.indivaragroup.jatistore.data.utility.table.schema.TransactionVariable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = PaymentCardVariable.TABLE_MST_PAYMENT_CARDS)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCard {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = PaymentCardVariable.COLUMN_MST_PAYMENT_CARDS_ID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = PaymentCardVariable.COLUMN_MST_PAYMENT_CARDS_USER_ID, nullable = false)
    private User user;

    @Size(min = 16, max = 16)
    @Column(name = PaymentCardVariable.COLUMN_MST_PAYMENT_CARDS_CARD_NUMBER, nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.CHAR)
    private String cardNumber;

    @Column(name = PaymentCardVariable.COLUMN_MST_PAYMENT_CARDS_CARD_HOLDER_NAME, nullable = false)
    private String cardHolderName;

    @Size(min = 5, max = 5)
    @Column(name = PaymentCardVariable.COLUMN_MST_PAYMENT_CARDS_EXPIRY_DATE, nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.CHAR)
    private String expiryDate;
}
