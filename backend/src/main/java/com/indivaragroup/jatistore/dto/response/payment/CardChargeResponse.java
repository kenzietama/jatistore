package com.indivaragroup.jatistore.dto.response.payment;

import java.math.BigDecimal;

public record CardChargeResponse(
    String status,
    BigDecimal amount,
    String message,
    String transactionId,
    String cardLast4
) {}
