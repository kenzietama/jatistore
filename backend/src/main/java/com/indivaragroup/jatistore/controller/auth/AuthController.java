package com.indivaragroup.jatistore.controller.auth;

import com.indivaragroup.jatistore.dto.request.auth.AuthLoginRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.auth.AuthLoginResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(RestApiPath.BASE_PATH+RestApiPath.AUTH_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping(RestApiPath.AUTH_LOGIN_PATH)
    @Audit(action = "LOGIN", affectedModule = "AUTH", description = "User attempted to login")
    public RestApiResponse<AuthLoginResponse> getUserLogin(@Valid @RequestBody AuthLoginRequest request) throws CoreThrowHandler {
        return authService.login(request);
    }

    @PostMapping(RestApiPath.AUTH_LOGOUT_PATH)
    @Audit(action = "LOGOUT", affectedModule = "AUTH", description = "User logged out")
    public  RestApiResponse<Void> logout(@RequestHeader("Authorization") String authorizationHeader) throws CoreThrowHandler {
        return authService.logout(authorizationHeader);
    }
}
