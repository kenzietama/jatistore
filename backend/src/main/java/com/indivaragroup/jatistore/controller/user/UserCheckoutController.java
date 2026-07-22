package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.request.user.PayOrderRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCheckoutResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.user.UserCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.USER_ORDER_BASE_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserCheckoutController {

    private final UserCheckoutService userCheckoutService;

    @PostMapping(RestApiPath.USER_ORDER_PAY_PATH)
    public RestApiResponse<UserCheckoutResponse> payOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody PayOrderRequest payOrderRequest,
            @RequestParam List<UUID> cartItemIds,
            Principal principal
    ) throws CoreThrowHandler {
        return userCheckoutService.payOrder(orderId, payOrderRequest, cartItemIds, principal.getName());
    }
}
