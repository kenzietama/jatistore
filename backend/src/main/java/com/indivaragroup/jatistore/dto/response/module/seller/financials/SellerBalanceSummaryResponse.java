package com.indivaragroup.jatistore.dto.response.module.seller.financials;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SellerBalanceSummaryResponse {
    private BigDecimal availableBalance;
    private BigDecimal onHoldBalance;
    private BigDecimal totalEarnings;
}
