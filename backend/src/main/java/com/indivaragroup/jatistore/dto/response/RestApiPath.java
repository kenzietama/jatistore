package com.indivaragroup.jatistore.dto.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestApiPath {
    public static final String BASE_PATH = "/api/v1";

    public static final String AUTH_BASE_PATH = "/auth";
    public static final String AUTH_LOGIN_PATH = "/login";
    public static final String AUTH_LOGOUT_PATH = "/logout";
    public static final String AUTH_REGISTER_PATH = "/register";

    public static final String USER_ORDER_BASE_PATH = "/orders";
    public static final String USER_ORDER_PAY_PATH = "/{orderId}/pay";
    public static final String USER_ORDER_CONFIRM_RECEIPT_PATH = "/{orderId}/confirm-receipt";
    public static final String USER_BASE_PATH = "/user";
    public static final String USER_PROFILE_PATH = "/profile";
    public static final String USER_BALANCE_PATH = "/balance";

    public static final String PRODUCT_BASE_PATH = "/products";
    public static final String PRODUCT_LIST_PATH = "";
    public static final String PRODUCT_DETAIL_PATH = "/{id}";
    public static final String CART_BASE_PATH = "/cart";
    public static final String CART_ADD_ITEM_PATH = "/items";
    public static final String CART_UPDATE_ITEM_PATH = "/items/{cartItemId}";
    public static final String CART_DELETE_ITEM_PATH = "/items/{cartItemId}";

    // Seller Paths
    public static final String SELLER_BASE_PATH = "/seller";
    public static final String SELLER_DASHBOARD_PATH = SELLER_BASE_PATH + "/dashboard";
    public static final String SELLER_PRODUCTS_PATH = SELLER_BASE_PATH + "/products";
    public static final String SELLER_ORDERS_PATH = SELLER_BASE_PATH + "/orders";
    public static final String SELLER_FLASH_SALES_PATH = SELLER_BASE_PATH + "/flash-sales";
    public static final String SELLER_FINANCIALS_PATH = SELLER_BASE_PATH + "/financials";

    // Admin Paths
    public static final String ADMIN_BASE_PATH = "/admin";
    public static final String ADMIN_DASHBOARD_PATH = ADMIN_BASE_PATH + "/dashboard";
    public static final String ADMIN_SELLERS_PATH = ADMIN_BASE_PATH + "/sellers";
    public static final String ADMIN_CATEGORIES_PATH = ADMIN_BASE_PATH + "/categories";
}
