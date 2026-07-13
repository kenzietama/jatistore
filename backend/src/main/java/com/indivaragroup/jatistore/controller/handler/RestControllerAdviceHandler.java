package com.indivaragroup.jatistore.controller.handler;

import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestControllerAdviceHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().getFirst();
        RestApiError apiError = mapToRestApiError(fieldError);
        String propertyName = normalizeValidationField(fieldError);

        String resolvedMessage;
        if (apiError == RestApiError.GEN_0003) {
            Object limit = "unknown";
            Object[] valArgs = fieldError.getArguments();
            if (valArgs != null && valArgs.length >= 3) {
                Integer max = (Integer) valArgs[1];
                Integer min = (Integer) valArgs[2];
                limit = (max != null && max != Integer.MAX_VALUE) ? max : min;
            }
            resolvedMessage = apiError.getMessage()
                    .replaceFirst("\\{\\}", propertyName)
                    .replaceFirst("\\{\\}", String.valueOf(limit));
        } else {
            resolvedMessage = apiError.getMessage().replace("{}", propertyName);
        }

        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(apiError.getCode());
        apiResponse.setRestApiResponseMessage(resolvedMessage);

        Map<String, Serializable> errorDetails = new HashMap<>();
        errorDetails.put("code", apiError.getCode());
        errorDetails.put("message", resolvedMessage);
        apiResponse.setRestApiResponseError(errorDetails);

        return ResponseEntity.status(apiError.getCode()).body(apiResponse);
    }

    private RestApiError mapToRestApiError(FieldError fieldError) {
        String code = fieldError.getCode();
        if ("NotBlank".equals(code) || "NotNull".equals(code) || "NotEmpty".equals(code)) {
            return RestApiError.GEN_0001;
        }
        if ("Size".equals(code) || "Max".equals(code) || "Length".equals(code)) {
            return RestApiError.GEN_0003;
        }
        return RestApiError.GEN_0002;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String propertyName = "payload";
        String msg = ex.getMessage();
        if (msg != null && msg.contains("[\"")) {
            int start = msg.indexOf("[\"") + 2;
            int end = msg.indexOf("\"]");
            if (start > 1 && end > start) {
                propertyName = msg.substring(start, end);
            }
        }

        String resolvedMessage = RestApiError.GEN_0002.getMessage().replace("{}", propertyName);

        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(HttpStatus.BAD_REQUEST.value());
        apiResponse.setRestApiResponseMessage(resolvedMessage);

        Map<String, Serializable> errorDetails = new HashMap<>();
        errorDetails.put("code", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("message", resolvedMessage);
        apiResponse.setRestApiResponseError(errorDetails);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    private String normalizeValidationField(FieldError fieldError) {
        String raw = fieldError.getField();
        if (raw == null || raw.isBlank()) {
            return raw;
        }

        String fieldName = raw;
        String[] parts = raw.split("(?i)Request");
        if (parts.length > 1) {
            fieldName = parts[parts.length - 1];
        }

        return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    @ExceptionHandler(CoreThrowHandler.class)
    public ResponseEntity<RestApiResponse<Void>> handleCoreThrowHandler(CoreThrowHandler ex) {
        String resolvedMessage = ex.getCustomMessage();
        int status = ex.getCode();

        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(status);
        apiResponse.setRestApiResponseMessage(resolvedMessage);

        Map<String, Serializable> errorDetails = new HashMap<>();
        if (ex.getError() != null && !ex.getError().isEmpty()) {
            errorDetails.putAll(ex.getError());
        }
        errorDetails.put("code", status);
        errorDetails.put("message", resolvedMessage);
        apiResponse.setRestApiResponseError(errorDetails);

        return ResponseEntity.status(status).body(apiResponse);
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<RestApiResponse<Void>> handleServletException(ServletException ex) {
        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(INTERNAL_SERVER_ERROR.value());
        apiResponse.setRestApiResponseMessage(ex.getMessage());

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestApiResponse<Void>> handleNoResourceFoundException(ServletException ex) {
        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(NOT_FOUND.value());
        apiResponse.setRestApiResponseMessage(ex.getMessage());

        return ResponseEntity.status(NOT_FOUND).body(apiResponse);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<RestApiResponse<Void>> handleAnyThrowable(Throwable ex) {
        String errorId = java.util.UUID.randomUUID().toString();
        log.error("[{}] Unhandled exception", errorId, ex);

        String resolvedMessage = RestApiError.GEN_0005.getMessage();

        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        apiResponse.setRestApiResponseMessage(resolvedMessage);

        Map<String, Serializable> err = new HashMap<>();
        err.put("errorId", errorId);
        err.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        err.put("message", resolvedMessage);
        apiResponse.setRestApiResponseError(err);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }
}
