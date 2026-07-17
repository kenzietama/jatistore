package com.indivaragroup.jatistore.data.utility.table.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SellerLedgerVariable {

    public static final String TABLE_TRX_SELLER_LEDGER = "trx_seller_ledger";

    public static final String COLUMN_TRX_SELLER_LEDGER_ID = "id";
    public static final String COLUMN_TRX_SELLER_LEDGER_SELLER_ID = "seller_id";
    public static final String COLUMN_TRX_SELLER_LEDGER_ORDER_ID = "order_id";
    public static final String COLUMN_TRX_SELLER_LEDGER_AMOUNT = "amount";
    public static final String COLUMN_TRX_SELLER_LEDGER_BALANCE_TYPE = "balance_type";
    public static final String COLUMN_TRX_SELLER_LEDGER_CREATED_AT = "created_at";
}
