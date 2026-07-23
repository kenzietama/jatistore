package com.indivaragroup.jatistore.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface CheckoutPriceProjection {
    UUID getCartItemId();
    UUID getProductId();
    Integer getQuantity();
    BigDecimal getEffectivePrice();
    Boolean getFlashSale();
}
