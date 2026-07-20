package com.indivaragroup.jatistore.dto.request.module.seller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class FlashSaleItemRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Flash price is required")
    @Min(value = 0, message = "Flash price cannot be negative")
    private BigDecimal flashPrice;

    @NotNull(message = "Remaining quota is required")
    @Min(value = 1, message = "Remaining quota must be at least 1")
    private Integer remainingQuota;
}
