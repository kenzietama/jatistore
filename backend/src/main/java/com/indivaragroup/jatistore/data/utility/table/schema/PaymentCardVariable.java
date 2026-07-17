package com.indivaragroup.jatistore.data.utility.table.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCardVariable {

    public static final String TABLE_MST_PAYMENT_CARDS = "mst_payment_cards";

    public static final String COLUMN_MST_PAYMENT_CARDS_ID = "id";
    public static final String COLUMN_MST_PAYMENT_CARDS_USER_ID = "user_id";
    public static final String COLUMN_MST_PAYMENT_CARDS_CARD_NUMBER = "card_number";
    public static final String COLUMN_MST_PAYMENT_CARDS_CARD_HOLDER_NAME = "card_holder_name";
    public static final String COLUMN_MST_PAYMENT_CARDS_EXPIRY_DATE = "expiry_date";
}
