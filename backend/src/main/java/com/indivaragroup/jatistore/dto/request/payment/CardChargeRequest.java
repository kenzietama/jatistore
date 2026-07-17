package com.indivaragroup.jatistore.dto.request.payment;

import java.math.BigDecimal;

public record CardChargeRequest(
    String cardNumber,
    String expiry,
    String cvc,
    BigDecimal amount,
    String cardHolderName
) {}
