package com.indivaragroup.jatistore.dto.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum RestApiSuccess {

    LOGIN_SUCCESS(HttpStatus.OK.value(), "Login successful."),
    LOGOUT_SUCCESS(HttpStatus.OK.value(), "Logged out successfully.");

    private final int code;
    private final String message;
}
