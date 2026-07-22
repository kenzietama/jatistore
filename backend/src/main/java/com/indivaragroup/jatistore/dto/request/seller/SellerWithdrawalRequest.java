package com.indivaragroup.jatistore.dto.request.seller;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SellerWithdrawalRequest {
    private BigDecimal amount;
}
