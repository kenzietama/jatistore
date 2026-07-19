package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.request.user.UserCheckoutRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCheckoutResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.user.UserCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestApiPath.BASE_PATH+RestApiPath.USER_CHECKOUT_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserCheckoutController {

    private final UserCheckoutService userCheckoutService;

    @PostMapping
    public RestApiResponse<UserCheckoutResponse> checkout(
            @Valid  @RequestBody UserCheckoutRequest userCheckoutRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws CoreThrowHandler {
        return userCheckoutService.checkout(userCheckoutRequest, userDetails.getUsername());
    }
}
