package com.indivaragroup.jatistore.dto.response.module.seller.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentOrderResponse {
    private UUID orderId;
    private String displayId; // e.g. #ORD-9921
    private String itemName;
    private String itemImage;
    private BigDecimal amount;
    private Integer quantity;
    private String status;
}
