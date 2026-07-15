package com.indivaragroup.jatistore.dto.response.module.seller.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerOrderListResponse {
    private UUID orderId;
    private ZonedDateTime orderDate;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private List<SellerOrderItemDTO> items;
}
