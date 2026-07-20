package com.indivaragroup.jatistore.dto.response.module.user;

import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmReceiptResponse {
    private UUID orderId;
    private OrderStatus orderStatus;
}
