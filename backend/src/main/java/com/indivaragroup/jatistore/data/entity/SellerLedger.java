package com.indivaragroup.jatistore.data.entity;

import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import com.indivaragroup.jatistore.data.utility.table.schema.SellerLedgerVariable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = SellerLedgerVariable.TABLE_TRX_SELLER_LEDGER)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerLedger {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = SellerLedgerVariable.COLUMN_TRX_SELLER_LEDGER_ID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SellerLedgerVariable.COLUMN_TRX_SELLER_LEDGER_SELLER_ID, nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SellerLedgerVariable.COLUMN_TRX_SELLER_LEDGER_ORDER_ID)
    private Order order;

    @Column(name = SellerLedgerVariable.COLUMN_TRX_SELLER_LEDGER_AMOUNT, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = SellerLedgerVariable.COLUMN_TRX_SELLER_LEDGER_BALANCE_TYPE, nullable = false)
    private BalanceType balanceType;

    @Column(name = SellerLedgerVariable.COLUMN_TRX_SELLER_LEDGER_CREATED_AT, nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
