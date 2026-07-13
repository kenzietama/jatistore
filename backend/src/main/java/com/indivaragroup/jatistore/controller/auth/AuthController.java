package com.indivaragroup.jatistore.controller.module;

import com.indivaragroup.jatistore.dto.request.AuthLoginRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.AuthLoginResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.module.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestApiPath.BASE_PATH+RestApiPath.AUTH_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping(RestApiPath.AUTH_LOGIN_PATH)
    public RestApiResponse<AuthLoginResponse> getUserLogin(@Valid @RequestBody AuthLoginRequest request) throws CoreThrowHandler {
        return authService.login(request);
    }
}
