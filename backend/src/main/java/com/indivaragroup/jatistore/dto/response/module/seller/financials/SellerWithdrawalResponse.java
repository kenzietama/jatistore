package com.indivaragroup.jatistore.dto.response.module.seller.financials;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SellerWithdrawalResponse {
   private BigDecimal amount;
   private BigDecimal newAvailableBalance;
   private UUID withdrawalId;
   private String mockGatewayRef;
}
