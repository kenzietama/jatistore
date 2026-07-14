package com.indivaragroup.jatistore.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Legacy bad-request shape for bean validation errors (e.g. empty email).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorApiResponse {
    private Map<String, String> field;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String message;

    private String code;
}
