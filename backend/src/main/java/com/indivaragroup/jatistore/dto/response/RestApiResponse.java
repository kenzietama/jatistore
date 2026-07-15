package com.indivaragroup.jatistore.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestApiResponse<T> {
    @JsonProperty("code")
    private int restApiResponseHttpCode;

    @JsonProperty("status")
    private String restApiResponseHttpStatus;

    @JsonProperty("message")
    private String restApiResponseMessage;

    @JsonProperty("data")
    private T restApiResponseData;

    @JsonProperty("error")
    private Map<String, Serializable> restApiResponseError;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private Instant restApiResponseTimestamp;

    @JsonProperty("requestId")
    private String restApiResponseRequestId;

    public static <T> RestApiResponse<T> success(T data) {
        return RestApiResponse.<T>builder()
                .restApiResponseHttpCode(org.springframework.http.HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Success")
                .restApiResponseData(data)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(org.slf4j.MDC.get("requestId"))
                .build();
    }
}
