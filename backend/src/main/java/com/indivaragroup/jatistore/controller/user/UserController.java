package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.request.user.UpdateUserProfileRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserProfileResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.USER_BASE_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping(RestApiPath.USER_PROFILE_PATH)
    public RestApiResponse<UserProfileResponse> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) throws CoreThrowHandler {
        log.info("Receiving profile request for user: {}", userDetails.getUsername());
        
        UserProfileResponse response = userService.getUserProfile(userDetails.getUsername());

        return RestApiResponse.<UserProfileResponse>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage("User profile retrieved successfully.")
                .restApiResponseData(response)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }

    @PutMapping(RestApiPath.USER_PROFILE_PATH)
    public RestApiResponse<Void> updateUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) throws CoreThrowHandler {
        log.info("Receiving profile update request for user: {}", userDetails.getUsername());
        
        userService.updateUserProfile(userDetails.getUsername(), request);

        return RestApiResponse.<Void>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("User profile updated successfully.")
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }
}