package com.indivaragroup.jatistore.controller.handler;

import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestControllerAdviceHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Serializable> validationErrors = new HashMap<>();
        
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
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
            validationErrors.put(propertyName, resolvedMessage);
        }

        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(BAD_REQUEST.value());
        apiResponse.setRestApiResponseHttpStatus(BAD_REQUEST.name());
        apiResponse.setRestApiResponseMessage("Request validation failed");
        apiResponse.setRestApiResponseError(validationErrors);
        apiResponse.setRestApiResponseTimestamp(Instant.now());
        apiResponse.setRestApiResponseRequestId(MDC.get("requestId"));

        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
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
            if (end > start) {
                propertyName = msg.substring(start, end);
            }
        }

        String resolvedMessage = RestApiError.GEN_0002.getMessage().replace("{}", propertyName);

        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(BAD_REQUEST.value());
        apiResponse.setRestApiResponseHttpStatus(BAD_REQUEST.name());
        apiResponse.setRestApiResponseMessage(resolvedMessage);
        apiResponse.setRestApiResponseError(null);
        apiResponse.setRestApiResponseTimestamp(Instant.now());
        apiResponse.setRestApiResponseRequestId(MDC.get("requestId"));

        return ResponseEntity.status(BAD_REQUEST).body(apiResponse);
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

        String statusLabel;
        try {
            statusLabel = HttpStatus.valueOf(status).name();
        } catch (IllegalArgumentException e) {
            statusLabel = "ERROR";
        }

        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(status);
        apiResponse.setRestApiResponseHttpStatus(statusLabel);
        apiResponse.setRestApiResponseMessage(resolvedMessage);
        
        // Pass error details map if populated, otherwise set as null to keep payload clean
        if (!ex.getError().isEmpty()) {
            apiResponse.setRestApiResponseError(ex.getError());
        } else {
            apiResponse.setRestApiResponseError(null);
        }
        
        apiResponse.setRestApiResponseTimestamp(Instant.now());
        apiResponse.setRestApiResponseRequestId(MDC.get("requestId"));

        return ResponseEntity.status(status).body(apiResponse);
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<RestApiResponse<Void>> handleServletException(ServletException ex) {
        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(INTERNAL_SERVER_ERROR.value());
        apiResponse.setRestApiResponseHttpStatus(INTERNAL_SERVER_ERROR.name());
        apiResponse.setRestApiResponseMessage(ex.getMessage());
        apiResponse.setRestApiResponseError(null);
        apiResponse.setRestApiResponseTimestamp(Instant.now());
        apiResponse.setRestApiResponseRequestId(MDC.get("requestId"));

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(NOT_FOUND.value());
        apiResponse.setRestApiResponseHttpStatus(NOT_FOUND.name());
        apiResponse.setRestApiResponseMessage(ex.getMessage());
        apiResponse.setRestApiResponseError(null);
        apiResponse.setRestApiResponseTimestamp(Instant.now());
        apiResponse.setRestApiResponseRequestId(MDC.get("requestId"));

        return ResponseEntity.status(NOT_FOUND).body(apiResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return handleCoreThrowHandler(new CoreThrowHandler(RestApiError.AUT_0006));
        }

        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(FORBIDDEN.value());
        apiResponse.setRestApiResponseHttpStatus(FORBIDDEN.name());
        apiResponse.setRestApiResponseMessage("Access denied: You do not have permission to access this resource");
        apiResponse.setRestApiResponseError(null);
        apiResponse.setRestApiResponseTimestamp(Instant.now());
        apiResponse.setRestApiResponseRequestId(MDC.get("requestId"));

        return ResponseEntity.status(FORBIDDEN).body(apiResponse);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<RestApiResponse<Void>> handleAnyThrowable(Throwable ex) {
        String errorId = java.util.UUID.randomUUID().toString();
        log.error("[{}] Unhandled exception", errorId, ex);

        String resolvedMessage = RestApiError.GEN_0005.getMessage();

        RestApiResponse<Void> apiResponse = new RestApiResponse<>();
        apiResponse.setRestApiResponseHttpCode(INTERNAL_SERVER_ERROR.value());
        apiResponse.setRestApiResponseHttpStatus(INTERNAL_SERVER_ERROR.name());
        apiResponse.setRestApiResponseMessage(resolvedMessage);

        Map<String, Serializable> err = new HashMap<>();
        err.put("errorId", errorId);
        apiResponse.setRestApiResponseError(err);
        
        apiResponse.setRestApiResponseTimestamp(Instant.now());
        apiResponse.setRestApiResponseRequestId(MDC.get("requestId"));

        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(apiResponse);
    }
}
