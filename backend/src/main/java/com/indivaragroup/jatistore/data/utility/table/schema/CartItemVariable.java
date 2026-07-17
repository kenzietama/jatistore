package com.indivaragroup.jatistore.data.utility.table.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartItemVariable {
    public static final String TABLE_TRX_CART_ITEMS = "trx_cart_items";

    public static final String COLUMN_TRX_CART_ITEMS_ID = "id";
    public static final String COLUMN_TRX_CART_ITEMS_CART_ID = "cart_id";
    public static final String COLUMN_TRX_CART_ITEMS_PRODUCT_ID = "product_id";
    public static final String COLUMN_TRX_CART_ITEMS_QUANTITY = "quantity";

}
