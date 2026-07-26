package com.indivaragroup.jatistore.dto.request.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRegisterRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidRequest() {
        AuthRegisterRequest request = new AuthRegisterRequest(
            "test@example.com",
            "Password123",
            "testuser",
            "08123456789",
            "Test User",
            LocalDate.of(1990, 1, 1)
        );

        Set<ConstraintViolation<AuthRegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testMissingEmail() {
        AuthRegisterRequest request = new AuthRegisterRequest(
            null,
            "Password123",
            "testuser",
            "08123456789",
            "Test User",
            null
        );

        Set<ConstraintViolation<AuthRegisterRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("required");
    }

    @Test
    void testInvalidEmailFormat() {
        AuthRegisterRequest request = new AuthRegisterRequest(
            "invalid-email",
            "Password123",
            "testuser",
            "08123456789",
            "Test User",
            null
        );

        Set<ConstraintViolation<AuthRegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testWeakPassword() {
        AuthRegisterRequest request = new AuthRegisterRequest(
            "test@example.com",
            "weak",
            "testuser",
            "08123456789",
            "Test User",
            null
        );

        Set<ConstraintViolation<AuthRegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testInvalidUsername() {
        AuthRegisterRequest request = new AuthRegisterRequest(
            "test@example.com",
            "Password123",
            "ab",
            "08123456789",
            "Test User",
            null
        );

        Set<ConstraintViolation<AuthRegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testInvalidPhoneNumber() {
        AuthRegisterRequest request = new AuthRegisterRequest(
            "test@example.com",
            "Password123",
            "testuser",
            "123",
            "Test User",
            null
        );

        Set<ConstraintViolation<AuthRegisterRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}
