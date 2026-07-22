package com.indivaragroup.jatistore.dto.response.module.seller.financials;


import com.indivaragroup.jatistore.data.entity.SellerLedger;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SellerLedgerTransactionResponse {
    private UUID id;
    private String type;
    private BigDecimal amount;
    private String balanceType;
    private UUID orderId;
    private String description;
    private Instant createdAt;

    public static SellerLedgerTransactionResponse from(SellerLedger ledger) {
        boolean isPositive = ledger.getAmount().compareTo(BigDecimal.ZERO) >= 0;
        String type;
        String description;

        if (ledger.getOrder() != null) {
            if (ledger.getBalanceType().name().equals("ON_HOLD")) {
                if (isPositive) {
                    type = "credit_on_hold";
                    description = "Order #" + ledger.getOrder().getId() + " payment (On Hold)";
                } else {
                    type = "transfer_out";
                    description = "Order #" + ledger.getOrder().getId() + " funds released";
                }
            } else { // AVAILABLE
                if (isPositive) {
                    type = "credit";
                    description = "Order #" + ledger.getOrder().getId() + " funds available";
                } else {
                    type = "debit"; // misal refund, walau belum diimplementasi
                    description = "Order #" + ledger.getOrder().getId() + " refund";
                }
            }
        } else {
            type = isPositive ? "credit" : "debit";
            description = isPositive ? "Adjustment" : "Withdrawal to external account";
        }

        return SellerLedgerTransactionResponse.builder()
                .id(ledger.getId())
                .type(type)
                .amount(ledger.getAmount())
                .balanceType(ledger.getBalanceType().name())
                .orderId(ledger.getOrder() !=null ? ledger.getOrder().getId() : null)
                .description(description)
                .createdAt(ledger.getCreatedAt())
                .build();
    }
}
