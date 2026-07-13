package com.indivaragroup.jatistore.data.utility.table.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenVariable {

    public static final String TABLE_TRX_TOKEN = "trx_tokens";

    public static final String COLUMN_TRX_TOKENS_ID = "id";
    public static final String COLUMN_TRX_TOKENS_USER_ID = "user_id";
    public static final String COLUMN_TRX_TOKENS_TOKEN = "token";
    public static final String COLUMN_TRX_TOKENS_EXPIRES_AT = "expires_at";
    public static final String COLUMN_TRX_TOKENS_LAST_UPDATED_AT = "last_updated_at";

}
