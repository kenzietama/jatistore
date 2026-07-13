package com.indivaragroup.jatistore.service.module;

import com.indivaragroup.jatistore.data.entity.Token;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.AuthLoginRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.auth.AuthLoginResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.dto.utility.RestApiSuccess;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.TokenRepository;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import com.indivaragroup.jatistore.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthJWTUtility authJWTUtility;
    private final AuthRepository authRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration-seconds:600}")
    private int jwtExpirationSeconds;

    @Value("${jwt.expiration-seconds.admin:3600}")
    private int jwtAdminExpirationSeconds;

    @Transactional
    public RestApiResponse<AuthLoginResponse> login(AuthLoginRequest authLoginRequest) throws CoreThrowHandler {
        Optional<User> user = authRepository.findByEmail(authLoginRequest.getAuthLoginRequestEmail());
        if (user.isEmpty()) {
            throw new CoreThrowHandler(RestApiError.AUT_0004);
        }
        boolean isPasswordCorrect = passwordEncoder.matches(
                authLoginRequest.getAuthLoginRequestPassword(),
                user.get().getPasswordHash()
        );
        if (!isPasswordCorrect) {
            throw new CoreThrowHandler(RestApiError.AUT_0004);
        }

        String role = authRepository.findUserRole(user.get().getId());

        if (role.equals("ROLE_SELLER")) {
            if (!authRepository.isSellerActive(user.get().getEmail())) {
                throw new CoreThrowHandler(RestApiError.AUT_0006);
            }
        }

        int TTL = role.equals("ROLE_ADMIN") ? jwtAdminExpirationSeconds : jwtExpirationSeconds;

        String jwt = authJWTUtility.generateToken(user.get().getId(), user.get().getEmail(), role, TTL);

        Token tokenEntity = tokenRepository.findByUserId(user.get().getId())
                .orElseGet(() -> Token.builder()
                        .user(user.get())
                        .build());

        tokenEntity.setToken(jwt);
        tokenEntity.setExpiresAt(Instant.now().plusSeconds(TTL));

        tokenRepository.save(tokenEntity);

        AuthLoginResponse authLoginResponse = new AuthLoginResponse();
        authLoginResponse.setAccessToken(jwt);
        authLoginResponse.setExpiresIn(TTL);
        authLoginResponse.setRole(role);

        return RestApiResponse.<AuthLoginResponse>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage(RestApiSuccess.LOGIN_SUCCESS.getMessage())
                .restApiResponseData(authLoginResponse)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }
}
