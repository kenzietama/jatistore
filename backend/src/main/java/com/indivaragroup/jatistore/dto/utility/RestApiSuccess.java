package com.indivaragroup.jatistore.dto.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum RestApiSuccess {

    LOGIN_SUCCESS(HttpStatus.OK.value(), "Login successful.");

    private final int code;
    private final String message;
}
