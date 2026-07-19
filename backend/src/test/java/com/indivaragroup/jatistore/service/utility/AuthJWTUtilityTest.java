package com.indivaragroup.jatistore.service.utility;

import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthJWTUtilityTest {

    private AuthJWTUtility authJWTUtility;
    private static final String SECRET_KEY = "9aDf784GjkLpQweRtyUioPasDfgHjKlZxcVbnM1234567890QWERTYUIOPASDFGHJKLZXCVBNM_HS512_VERY_SECRET_AND_LONG_KEY";

    @BeforeEach
    void setUp() {
        authJWTUtility = new AuthJWTUtility();
        ReflectionTestUtils.setField(authJWTUtility, "jwtSecret", SECRET_KEY);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        UUID userId = UUID.randomUUID();
        String email = "seller.tech@example.com";
        String role = "ROLE_SELLER";
        int ttl = 600;

        String token = authJWTUtility.generateToken(userId, email, role, ttl);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertDoesNotThrow(() -> authJWTUtility.verifyToken(token));
        assertEquals(email, authJWTUtility.resolveSubjectFromEncryptedToken(token));
    }

    @Test
    void generateToken_WithTooShortSecret_ShouldThrowException() {
        // A 32-byte key is 256 bits, which is too short for HMAC HS512 (requires at least 64 bytes/512 bits)
        String shortSecret = "12345678901234567890123456789012";
        ReflectionTestUtils.setField(authJWTUtility, "jwtSecret", shortSecret);

        UUID userId = UUID.randomUUID();
        assertThrows(CoreThrowHandler.class, () -> 
            authJWTUtility.generateToken(userId, "seller@example.com", "ROLE_SELLER", 600)
        );
    }

    @Test
    void resolveSubject_WithMissingEmailClaim_ShouldThrowException() {
        String token = authJWTUtility.generateToken(UUID.randomUUID(), "", "ROLE_SELLER", 600);

        assertThrows(CoreThrowHandler.class, () -> 
            authJWTUtility.resolveSubjectFromEncryptedToken(token)
        );
    }

    @Test
    void resolveSubject_WithNullEmailClaim_ShouldThrowException() {
        String token = authJWTUtility.generateToken(UUID.randomUUID(), null, "ROLE_SELLER", 600);

        assertThrows(CoreThrowHandler.class, () -> 
            authJWTUtility.resolveSubjectFromEncryptedToken(token)
        );
    }

    @Test
    void resolveSubject_WithMalformedToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";
        assertThrows(CoreThrowHandler.class, () -> 
            authJWTUtility.resolveSubjectFromEncryptedToken(invalidToken)
        );
    }

    @Test
    void verifyToken_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";
        CoreThrowHandler ex = assertThrows(CoreThrowHandler.class, () -> authJWTUtility.verifyToken(invalidToken));
        assertEquals(com.indivaragroup.jatistore.dto.utility.RestApiError.AUT_0008, ex.getRestApiError());
    }

    @Test
    void verifyToken_WithExpiredToken_ShouldThrowException() {
        String expiredToken = authJWTUtility.generateToken(UUID.randomUUID(), "seller@example.com", "ROLE_SELLER", -600);
        CoreThrowHandler ex = assertThrows(CoreThrowHandler.class, () -> authJWTUtility.verifyToken(expiredToken));
        assertEquals(com.indivaragroup.jatistore.dto.utility.RestApiError.AUT_0007, ex.getRestApiError());
    }

    @Test
    void verifyToken_WithInvalidSignature_ShouldThrowException() {
        String token = authJWTUtility.generateToken(UUID.randomUUID(), "seller@example.com", "ROLE_SELLER", 600);

        // Mismatched secret key
        ReflectionTestUtils.setField(authJWTUtility, "jwtSecret", "differentSecretKeyDifferentSecretKeyDifferentSecretKeyDifferentSecretKeyDifferentSecretKeyDifferentSecretKey");

        CoreThrowHandler ex = assertThrows(CoreThrowHandler.class, () -> authJWTUtility.verifyToken(token));
        assertEquals(com.indivaragroup.jatistore.dto.utility.RestApiError.AUT_0008, ex.getRestApiError());
    }

    @Test
    void verifyToken_WithNullExpirationTime_ShouldThrowException() throws Exception {
        // Arrange
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject("seller.tech@example.com")
                .claim("userId", UUID.randomUUID().toString())
                .claim("role", "ROLE_SELLER")
                .build(); // Missing expiration time

        JWSSigner signer = new MACSigner(SECRET_KEY.getBytes());
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), jwtClaimsSet);
        signedJWT.sign(signer);
        String token = signedJWT.serialize();

        // Act & Assert
        CoreThrowHandler ex = assertThrows(CoreThrowHandler.class, () -> authJWTUtility.verifyToken(token));
        assertEquals(com.indivaragroup.jatistore.dto.utility.RestApiError.AUT_0007, ex.getRestApiError());
    }
}
