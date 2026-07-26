package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserBalanceResponse;
import com.indivaragroup.jatistore.service.user.UserBalanceService;
import com.indivaragroup.jatistore.audit.Audit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.USER_BASE_PATH + RestApiPath.USER_BALANCE_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserBalanceController {

    private final UserBalanceService userBalanceService;

    @Audit(action = "USER_BALANCE_FETCH", affectedModule = "FINANCIALS", description = "User fetched their balance")
    @GetMapping()
    public RestApiResponse<UserBalanceResponse> getBalance() {
        return userBalanceService.getBalance();
    }
}
