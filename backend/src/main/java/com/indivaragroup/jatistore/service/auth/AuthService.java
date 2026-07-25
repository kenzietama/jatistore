package com.indivaragroup.jatistore.service.auth;

import com.indivaragroup.jatistore.data.entity.Token;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.auth.AuthLoginRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.auth.AuthLoginResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.dto.utility.RestApiSuccess;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.TokenRepository;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.data.entity.AuditTrail;
import com.indivaragroup.jatistore.repository.AuditTrailRepository;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import com.indivaragroup.jatistore.data.entity.Seller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthJWTUtility authJWTUtility;
    private final AuthRepository authRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditTrailRepository auditTrailRepository;
    private final SellerRepository sellerRepository;

    @Value("${jwt.expiration-seconds.user:600}")
    private int jwtUserExpirationSeconds;

    @Value("${jwt.expiration-seconds.seller:600}")
    private int jwtSellerExpirationSeconds;

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

        if (role.equals("SELLER")) {
            if (!authRepository.isSellerActive(user.get().getEmail())) {
                String reason = RestApiError.AUT_0010.getMessage();
                Optional<Seller> seller = sellerRepository.findByUserId(user.get().getId());
                log.info("Checking seller deactivation reason for userId: {}, sellerId: {}", user.get().getId(), seller.isPresent() ? seller.get().getId() : "null");
                if (seller.isPresent()) {
                    Optional<AuditTrail> audit = auditTrailRepository.findFirstByEntityIdAndActionOrderByCreatedAtDesc(seller.get().getId(), "SELLER_UPDATE_STATUS");
                    log.info("Found audit trail: {}", audit.isPresent());
                    if (audit.isPresent() && audit.get().getPayload() != null) {
                        String payload = audit.get().getPayload();
                        log.info("Audit payload: {}", payload);
                        String key = "\"deactivationReason\"";
                        int keyIdx = payload.indexOf(key);
                        if (keyIdx != -1) {
                            int colonIdx = payload.indexOf(":", keyIdx);
                            if (colonIdx != -1) {
                                int quoteStart = payload.indexOf("\"", colonIdx);
                                if (quoteStart != -1) {
                                    int quoteEnd = payload.indexOf("\"", quoteStart + 1);
                                    if (quoteEnd != -1) {
                                        reason = payload.substring(quoteStart + 1, quoteEnd);
                                        log.info("Extracted reason: {}", reason);
                                    }
                                }
                            }
                        }
                    }
                }
                throw new CoreThrowHandler(HttpStatus.FORBIDDEN.value(), reason, Collections.emptyMap());
            }
        }

        int TTL = role.equals("ADMIN") ? jwtAdminExpirationSeconds
                : role.equals("SELLER") ? jwtSellerExpirationSeconds
                : jwtUserExpirationSeconds;

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


    @Transactional
    public RestApiResponse<Void> logout(String authorizationHeader) throws CoreThrowHandler {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CoreThrowHandler(RestApiError.AUT_0006);
        }

        String jwt = authorizationHeader.substring(7);
        Optional<Token> token = tokenRepository.findByToken(jwt);
        if (token.isEmpty()) {
            throw new CoreThrowHandler(RestApiError.AUT_0009);
        }

        token.ifPresent(tokenRepository::delete);
        SecurityContextHolder.clearContext();
        return RestApiResponse.<Void>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage(RestApiSuccess.LOGOUT_SUCCESS.getMessage())
                .restApiResponseData(null)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }
}
