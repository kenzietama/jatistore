package com.indivaragroup.jatistore.dto.response.module.seller.financials;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SellerFinancialDashboardResponse {
    private BigDecimal availableBalance;
    private BigDecimal onHoldBalance;
    private BigDecimal totalEarnings;
    private List<SellerLedgerTransactionResponse> recentTransactions;
}
