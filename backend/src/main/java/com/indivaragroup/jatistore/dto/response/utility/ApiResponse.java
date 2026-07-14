package com.indivaragroup.jatistore.dto.response.utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String status;
    private String message;
    private T data;
    private ZonedDateTime timestamp;
    private String requestId;

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .status("SUCCESS")
                .message(message)
                .data(data)
                .timestamp(ZonedDateTime.now())
                .requestId(UUID.randomUUID().toString()) // Mock request ID for now
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return success("200", message, data);
    }
}
