package com.indivaragroup.jatistore.dto.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestApiPath {
    public static final String BASE_PATH = "/api/v1";
    public static final String AUTH_BASE_PATH = "/auth";

    public static final String AUTH_LOGIN_PATH = "/login";
    public static final String AUTH_LOGOUT_PATH = "/logout";

}
