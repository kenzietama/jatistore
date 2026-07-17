package com.indivaragroup.jatistore.dto.response.module.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCheckoutResponse {

    private UUID order_id;
    private UUID transaction_id;
    private String payment_gateway_ref;
    private String order_status;
    private BigDecimal total_amount;

}
