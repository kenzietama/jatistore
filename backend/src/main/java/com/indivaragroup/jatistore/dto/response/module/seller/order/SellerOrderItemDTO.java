package com.indivaragroup.jatistore.dto.response.module.seller.order;

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
public class SellerOrderItemDTO {
    private UUID productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal pricePerItem;
    private BigDecimal subtotal;
    private Boolean flashSale;
}
