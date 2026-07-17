package com.indivaragroup.jatistore.data.utility.table.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionVariable {

    public static final String TABLE_TRX_TRANSACTIONS = "trx_transactions";

    public static final String COLUMN_TRX_TRANSACTIONS_ID = "id";
    public static final String COLUMN_TRX_TRANSACTIONS_ORDER_ID = "order_id";
    public static final String COLUMN_TRX_TRANSACTIONS_PAYMENT_METHOD = "payment_method";
    public static final String COLUMN_TRX_TRANSACTIONS_PAYMENT_CARD_ID = "payment_card_id";
    public static final String COLUMN_TRX_TRANSACTIONS_PAYMENT_GATEWAY_REF = "payment_gateway_ref";
    public static final String COLUMN_TRX_TRANSACTIONS_STATUS = "status";
    public static final String COLUMN_TRX_TRANSACTIONS_CREATED_AT = "created_at";
    public static final String COLUMN_TRX_TRANSACTIONS_UPDATED_AT = "updated_at";
}
