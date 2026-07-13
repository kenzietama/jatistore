package com.indivaragroup.jatistore.exception;

import com.indivaragroup.jatistore.dto.utility.RestApiError;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

@Getter
public class CoreThrowHandler extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer code;
    private final RestApiError restApiError;
    private final String customMessage;
    private final Map<String, Serializable> error;

    public CoreThrowHandler(RestApiError restApiError) {
        super(restApiError.name());
        this.code = restApiError.getCode();
        this.restApiError = restApiError;
        this.customMessage = restApiError.getMessage();
        this.error = Collections.emptyMap();
    }

    public CoreThrowHandler(RestApiError restApiError, Map<String, Serializable> error) {
        super(restApiError.name());
        this.code = restApiError.getCode();
        this.restApiError = restApiError;
        this.customMessage = restApiError.getMessage();
        this.error = error;
    }

    public CoreThrowHandler(Integer status, String message, Map<String, Serializable> error) {
        super(message);
        this.code = status;
        this.restApiError = null;
        this.customMessage = message;
        this.error = error != null ? error : Collections.emptyMap();
    }
}
