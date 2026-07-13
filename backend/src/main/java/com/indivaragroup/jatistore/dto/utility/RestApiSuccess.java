package com.indivaragroup.jatistore.dto.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum RestApiSuccessMessage {

    LOGIN_SUCCESS(HttpStatus.OK.value(), "Login successful."),
    REGISTER_SUCCESS(HttpStatus.CREATED.value(), "success.register"),
    PROFILE_SUCCESS(HttpStatus.OK.value(), "success.profile");

    private final int code;
    private final String message;
}
