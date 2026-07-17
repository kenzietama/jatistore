package com.indivaragroup.jatistore.dto.request.payment;

import java.math.BigDecimal;

public record WalletChargeRequest(
    BigDecimal amount
) {}
