package com.indivaragroup.jatistore.dto.response.module.seller.financials;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.SellerLedger;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SellerLedgerTransactionResponseTest {

    @Test
    void from_onHoldCredit() {
        Order order = new Order();
        order.setId(UUID.randomUUID());

        SellerLedger ledger = SellerLedger.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100"))
                .balanceType(BalanceType.ON_HOLD)
                .order(order)
                .createdAt(Instant.now())
                .build();

        SellerLedgerTransactionResponse response = SellerLedgerTransactionResponse.from(ledger);

        assertEquals("credit_on_hold", response.getType());
        assertEquals("Order #" + order.getId() + " payment (On Hold)", response.getDescription());
    }

    @Test
    void from_onHoldDebit() {
        Order order = new Order();
        order.setId(UUID.randomUUID());

        SellerLedger ledger = SellerLedger.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("-100"))
                .balanceType(BalanceType.ON_HOLD)
                .order(order)
                .createdAt(Instant.now())
                .build();

        SellerLedgerTransactionResponse response = SellerLedgerTransactionResponse.from(ledger);

        assertEquals("transfer_out", response.getType());
        assertEquals("Order #" + order.getId() + " funds released", response.getDescription());
    }

    @Test
    void from_availableCredit() {
        Order order = new Order();
        order.setId(UUID.randomUUID());

        SellerLedger ledger = SellerLedger.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100"))
                .balanceType(BalanceType.AVAILABLE)
                .order(order)
                .createdAt(Instant.now())
                .build();

        SellerLedgerTransactionResponse response = SellerLedgerTransactionResponse.from(ledger);

        assertEquals("credit", response.getType());
        assertEquals("Order #" + order.getId() + " funds available", response.getDescription());
    }

    @Test
    void from_availableDebit() {
        Order order = new Order();
        order.setId(UUID.randomUUID());

        SellerLedger ledger = SellerLedger.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("-100"))
                .balanceType(BalanceType.AVAILABLE)
                .order(order)
                .createdAt(Instant.now())
                .build();

        SellerLedgerTransactionResponse response = SellerLedgerTransactionResponse.from(ledger);

        assertEquals("debit", response.getType());
        assertEquals("Order #" + order.getId() + " refund", response.getDescription());
    }

    @Test
    void from_noOrderCredit() {
        SellerLedger ledger = SellerLedger.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100"))
                .balanceType(BalanceType.AVAILABLE)
                .createdAt(Instant.now())
                .build();

        SellerLedgerTransactionResponse response = SellerLedgerTransactionResponse.from(ledger);

        assertEquals("credit", response.getType());
        assertEquals("Adjustment", response.getDescription());
    }

    @Test
    void from_noOrderDebit() {
        SellerLedger ledger = SellerLedger.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("-100"))
                .balanceType(BalanceType.AVAILABLE)
                .createdAt(Instant.now())
                .build();

        SellerLedgerTransactionResponse response = SellerLedgerTransactionResponse.from(ledger);

        assertEquals("debit", response.getType());
        assertEquals("Withdrawal to external account", response.getDescription());
    }
}
