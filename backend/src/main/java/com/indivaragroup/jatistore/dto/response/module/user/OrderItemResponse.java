package com.indivaragroup.jatistore.dto.response.module.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private String productName;
    private Integer quantity;
    private BigDecimal pricePerItem;
    private Boolean isFlashSale;
}
