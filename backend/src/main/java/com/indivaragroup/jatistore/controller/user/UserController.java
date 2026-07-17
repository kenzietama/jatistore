package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.USER_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @GetMapping(RestApiPath.USER_PROFILE_PATH)
    public RestApiResponse<String> getUserProfile(@PathVariable String id) throws CoreThrowHandler {
        log.info("Menerima permintaan data profil untuk User ID: {}", id);

        return RestApiResponse.<String>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage("Data user berhasil diambil!")
                .restApiResponseData("Profil User dengan ID " + id + " (Demo)")
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId("REQ-" + System.currentTimeMillis())
                .build();
    }
}