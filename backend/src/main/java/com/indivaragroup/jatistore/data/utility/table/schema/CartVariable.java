package com.indivaragroup.jatistore.data.utility.table.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartVariable {
    public static final String TABLE_TRX_CARTS = "trx_carts";

    public static final String COLUMN_TRX_CARTS_ID = "id";
    public static final String COLUMN_TRX_CARTS_USER_ID = "user_id";
    public static final String COLUMN_TRX_CARTS_CREATED_AT = "created_at";
    public static final String COLUMN_TRX_CARTS_UPDATED_AT = "updated_at";

}
