package com.indivaragroup.jatistore.data.utility.table.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderVariable {
    public static final String TABLE_TRX_ORDERS = "trx_orders";

    public static final String COLUMN_TRX_ORDERS_ID = "id";
    public static final String COLUMN_TRX_ORDERS_USER_ID = "user_id";
    public static final String COLUMN_TRX_ORDERS_TOTAL_AMOUNT = "total_amount";
    public static final String COLUMN_TRX_ORDERS_STATUS = "status";
    public static final String COLUMN_TRX_ORDERS_CREATED_AT = "created_at";
    public static final String COLUMN_TRX_ORDERS_UPDATED_AT = "updated_at";

}
