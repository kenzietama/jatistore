package com.indivaragroup.jatistore.data.utility.table.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserVariable {

    public static final String TABLE_MST_USERS = "mst_users";

    public static final String COLUMN_MST_USERS_ID = "id";
    public static final String COLUMN_MST_USERS_USERNAME = "username";
    public static final String COLUMN_MST_USERS_EMAIL = "email";
    public static final String COLUMN_MST_USERS_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_MST_USERS_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_MST_USERS_FULL_NAME = "full_name";
    public static final String COLUMN_MST_USERS_CREATED_AT = "created_at";
    public static final String COLUMN_MST_USERS_UPDATED_AT = "updated_at";

}
