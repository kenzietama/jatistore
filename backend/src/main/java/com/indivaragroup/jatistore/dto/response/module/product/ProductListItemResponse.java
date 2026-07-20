package com.indivaragroup.jatistore.dto.response.module.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListItemResponse {
    private UUID id;
    private String name;
    private String storeName;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Boolean isFlashSale;
    private Instant flashSaleEndTime;
    private String image;
}
