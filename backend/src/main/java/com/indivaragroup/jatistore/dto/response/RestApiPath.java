package com.indivaragroup.jatistore.dto.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestApiPath {
    public static final String BASE_PATH = "/api/v1";
    public static final String AUTH_BASE_PATH = "/auth";

    public static final String AUTH_LOGIN_PATH = "/login";

    public static final String USER_BASE_PATH = "/user";
    public static final String USER_PROFILE_PATH = "/profile/{id}";

    public static final String PRODUCT_BASE_PATH = "/products";
    public static final String PRODUCT_LIST_PATH = "/list";
    public static final String PRODUCT_DETAIL_PATH = "/{id}";
    public static final String CART_BASE_PATH = "/carts";
    public static final String CART_ADD_ITEM_PATH = "/items";

}
