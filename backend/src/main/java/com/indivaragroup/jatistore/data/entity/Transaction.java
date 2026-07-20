package com.indivaragroup.jatistore.data.entity;

import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import com.indivaragroup.jatistore.data.utility.constant.TransactionStatus;
import com.indivaragroup.jatistore.data.utility.table.schema.TransactionVariable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = TransactionVariable.TABLE_TRX_TRANSACTIONS)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = TransactionVariable.COLUMN_TRX_TRANSACTIONS_ID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TransactionVariable.COLUMN_TRX_TRANSACTIONS_ORDER_ID, nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = TransactionVariable.COLUMN_TRX_TRANSACTIONS_PAYMENT_METHOD, nullable = false)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TransactionVariable.COLUMN_TRX_TRANSACTIONS_PAYMENT_CARD_ID)
    private PaymentCard paymentCard;

    @Column(name = TransactionVariable.COLUMN_TRX_TRANSACTIONS_PAYMENT_GATEWAY_REF)
    private String paymentGatewayRef;

    @Enumerated(EnumType.STRING)
    @Column(name = TransactionVariable.COLUMN_TRX_TRANSACTIONS_STATUS, nullable = false)
    private TransactionStatus status;

    @Column(name = TransactionVariable.COLUMN_TRX_TRANSACTIONS_CREATED_AT, nullable = false)
    private Instant createdAt;

    @Column(name = TransactionVariable.COLUMN_TRX_TRANSACTIONS_UPDATED_AT, nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
