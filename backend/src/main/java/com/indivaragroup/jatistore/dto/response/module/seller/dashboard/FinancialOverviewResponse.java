package com.indivaragroup.jatistore.dto.response.module.seller.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancialOverviewResponse {
    private BigDecimal availableBalance;
    private BigDecimal onHoldBalance;
}
