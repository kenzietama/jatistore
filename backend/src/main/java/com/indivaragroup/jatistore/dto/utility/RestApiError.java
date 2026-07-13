package com.indivaragroup.jatistore.dto.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;

import java.util.Locale;

@AllArgsConstructor
@Getter
public enum RestApiErrorMessage {

    GEN_0001(HttpStatus.BAD_REQUEST.value(), "Missing mandatory property {}" ),
    GEN_0002(HttpStatus.BAD_REQUEST.value(), "Invalid data type for property {}" ),
    GEN_0003(HttpStatus.BAD_REQUEST.value(), "Maximum length for property {} is {}" ),
    AUT_0004(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password"),
    AUT_0005(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");

    private final int code;
    private final String message;

}
