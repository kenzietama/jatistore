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
    AUT_0009(HttpStatus.NOT_FOUND.value(), "Session not found"),
    AUT_0010(HttpStatus.FORBIDDEN.value(), "Account has been suspended or deactivated."),
    SLR_0002(HttpStatus.FORBIDDEN.value(), "User is not a registered seller"),
    SLR_0020(HttpStatus.BAD_REQUEST.value(), "Invalid order status transition"),
    SLR_0021(HttpStatus.FORBIDDEN.value(), "Order does not contain your products");

    private final int code;
    private final String message;

}
