package com.indivaragroup.jatistore.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productImage;
    private BigDecimal unitPrice;
    private BigDecimal originalPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer maxStock;
    private UUID storeId;
    private String storeName;
    private Boolean sellerActive;
}
