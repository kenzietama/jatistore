package com.indivaragroup.jatistore.dto.response.module.user;

import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private UUID orderId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderDetailItemResponse> orderDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailItemResponse {
        private UUID productId;
        private String productName;
        private BigDecimal pricePerItem;
        private Integer quantity;
        private Boolean flashSale;
    }
}
