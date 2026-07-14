package com.indivaragroup.jatistore.service.utility;

import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class AuthJWTUtility {

    // Must be at least 256 bits (32 bytes) for HS256
    @Value("${jwt.secret:default-secret-key-that-must-be-very-long-and-secure-for-hmac-sha-512-at-least-64-bytes-long}")
    private String jwtSecret;

    public String generateToken(UUID userId, String email, String role, int TTL) {
        try {
            Instant ISSUED_AT = Instant.now();
            Instant EXPIRES_AT = ISSUED_AT.plusSeconds(TTL);

            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .claim("userId", userId)
                    .claim("email", email)
                    .claim("role", role)
                    .issueTime(Date.from(ISSUED_AT))
                    .expirationTime(Date.from(EXPIRES_AT))
                    .build();

            JWSSigner signer = new MACSigner(jwtSecret.getBytes());
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), jwtClaimsSet);
            signedJWT.sign(signer);
            String token = signedJWT.serialize();

            log.info("[AuthJWTUtility:GENERATE_ACCESS_TOKEN] Token CREATION completed for user: {}", userId);

            return token;
        } catch (JOSEException e) {
            log.error("[AuthJWTUtility:GENERATE_ACCESS_TOKEN] Token CREATION failed for user: {}", userId);
            log.error("[OauthJWTUtility:GENERATE_ACCESS_TOKEN] Exception:{}", e.getMessage());
            throw new CoreThrowHandler(RestApiError.GEN_0005);
        }
    }

    public String resolveSubjectFromEncryptedToken(String serializedJwt) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(serializedJwt);
            String email = signedJWT.getJWTClaimsSet().getStringClaim("email");
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email is missing or empty");
            }
            return email;
        } catch (Exception e) {
            log.warn("[AuthJWTUtility:RESOLVE_SUBJECT] Invalid or unreadable token: {}", e.getMessage());
            throw new CoreThrowHandler(RestApiError.AUT_0004);
        }
    }

    public void verifyToken(String serializedJwt) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(serializedJwt);
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());

            if (!signedJWT.verify(verifier)) {
                throw new CoreThrowHandler(RestApiError.AUT_0008);
            }

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime == null || expirationTime.before(Date.from(Instant.now()))) {
                throw new CoreThrowHandler(RestApiError.AUT_0007);
            }
        } catch (CoreThrowHandler e) {
            throw e;
        } catch (Exception e) {
            throw new CoreThrowHandler(RestApiError.AUT_0008);
        }
    }
}
