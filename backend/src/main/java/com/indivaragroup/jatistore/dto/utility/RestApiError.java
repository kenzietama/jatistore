package com.indivaragroup.jatistore.dto.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum RestApiError {

    GEN_0001(HttpStatus.BAD_REQUEST.value(), "Missing mandatory property {}" ),
    GEN_0002(HttpStatus.BAD_REQUEST.value(), "Invalid data type for property {}" ),
    GEN_0003(HttpStatus.BAD_REQUEST.value(), "Maximum length for property {} is {}" ),
    AUT_0004(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password"),
    GEN_0005(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error."),
    AUT_0006(HttpStatus.UNAUTHORIZED.value(), "Unauthorized access"),
    AUT_0007(HttpStatus.UNAUTHORIZED.value(), "Token has expired"),
    AUT_0008(HttpStatus.UNAUTHORIZED.value(), "Invalid Token"),
    AUT_0009(HttpStatus.UNAUTHORIZED.value(), "Session not found"),
    AUT_0010(HttpStatus.FORBIDDEN.value(), "Account has been suspended or deactivated."),

    USR_0001(HttpStatus.NOT_FOUND.value(), "Product not found"),
    USR_0003(HttpStatus.BAD_REQUEST.value(), "Insufficient stock"),
    USR_0004(HttpStatus.NOT_FOUND.value(), "Cart item not found"),
    USR_0005(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"),
    USR_0006(HttpStatus.UNAUTHORIZED.value(), "Unauthorized access"),
    USR_0007(HttpStatus.UNAUTHORIZED.value(), "Token expired"),
    USR_0008(HttpStatus.UNAUTHORIZED.value(), "Invalid token"),
    USR_0009(HttpStatus.BAD_REQUEST.value(), "Cart is empty"),
    USR_0010(HttpStatus.BAD_REQUEST.value(), "Missing mandatory payment field: {field}"),
    USR_0011(HttpStatus.BAD_REQUEST.value(), "Insufficient stock for product: {productName}"),
    USR_0012(HttpStatus.PAYMENT_REQUIRED.value(), "Insufficient wallet balance"),
    USR_0013(HttpStatus.PAYMENT_REQUIRED.value(), "Payment gateway declined transaction"),
    USR_0014(HttpStatus.BAD_GATEWAY.value(), "Payment gateway error"),
    USR_0017(HttpStatus.GATEWAY_TIMEOUT.value(), "Payment gateway timeout"),
    USR_0018(HttpStatus.BAD_REQUEST.value(), "Invalid card holder name format"),
    USR_0019(HttpStatus.BAD_REQUEST.value(), "Items from different sellers"),
    USR_0021(HttpStatus.BAD_REQUEST.value(),"Invalid cart item ID"),
    USR_0015(HttpStatus.NOT_FOUND.value(), "Order not found"),
    USR_0016(HttpStatus.BAD_REQUEST.value(), "Order receipt confirmation not allowed"),

    SLR_0002(HttpStatus.FORBIDDEN.value(), "User is not a registered seller"),
    SLR_0020(HttpStatus.BAD_REQUEST.value(), "Invalid order status transition"),
    SLR_0021(HttpStatus.FORBIDDEN.value(), "Order does not contain your products"),

    ADM_0006(HttpStatus.NOT_FOUND.value(), "Seller not found"),
    ADM_0010(HttpStatus.CONFLICT.value(), "Category name already exists"),
    ADM_0011(HttpStatus.NOT_FOUND.value(), "Category not found"),
    ADM_0012(HttpStatus.CONFLICT.value(), "Cannot delete category with active products"),
    ADM_0004(HttpStatus.BAD_REQUEST.value(), "Invalid page or size parameter"),
    ADM_0013(HttpStatus.BAD_REQUEST.value(), "Invalid status parameter"),
    ADM_0014(HttpStatus.BAD_REQUEST.value(), "End time must be after start time"),
    ADM_0016(HttpStatus.NOT_FOUND.value(), "Flash sale not found"),
    ADM_0017(HttpStatus.CONFLICT.value(), "Cannot delete flash sale with attached products"),
    ADM_0018(HttpStatus.BAD_REQUEST.value(), "Start time cannot be in the past"),
    SLR_0001(HttpStatus.UNAUTHORIZED.value(), "Unauthorized access"),
    SLR_0016(HttpStatus.NOT_FOUND.value(), "Product not found"),
    SLR_0017(HttpStatus.FORBIDDEN.value(), "Forbidden: Product belongs to another seller"),
    SLR_0030(HttpStatus.BAD_REQUEST.value(), "Flash price must be less than retail price"),
    SLR_0033(HttpStatus.BAD_REQUEST.value(), "Remaining quota exceeds available stock"),
    SLR_0034(HttpStatus.BAD_REQUEST.value(), "Remaining quota must be greater than zero"),
    SLR_0035(HttpStatus.CONFLICT.value(), "Product time conflict"),
    SLR_0044(HttpStatus.NOT_FOUND.value(), "Flash sale event not found"),
    SLR_0045(HttpStatus.BAD_REQUEST.value(), "Flash sale event has already ended"),
    SLR_0046(HttpStatus.CONFLICT.value(), "Product already in this flash sale event"),
    SLR_0040(HttpStatus.BAD_REQUEST.value(), "Invalid balance type filter"),
    SLR_0041(HttpStatus.BAD_REQUEST.value(), "Amount must be greater than zero"),
    SLR_0042(HttpStatus.BAD_REQUEST.value(), "Insufficient available balance"),
    SLR_0043(HttpStatus.BAD_REQUEST.value(), "Withdrawal amount exceeds maximum limit");

    private final int code;
    private final String message;

}
