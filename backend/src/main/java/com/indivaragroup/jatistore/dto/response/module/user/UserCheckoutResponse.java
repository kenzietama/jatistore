package com.indivaragroup.jatistore.dto.response.module.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.Transaction;
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

    public static UserCheckoutResponse from(Order order, Transaction transaction) {
        return new UserCheckoutResponse(
                order.getId(),
                transaction.getId(),
                transaction.getPaymentGatewayRef(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getTotalAmount()
        );
    }

}
