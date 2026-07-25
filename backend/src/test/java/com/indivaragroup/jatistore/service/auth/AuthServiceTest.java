package com.indivaragroup.jatistore.service.auth;

import com.indivaragroup.jatistore.data.entity.Token;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.auth.AuthLoginRequest;
import com.indivaragroup.jatistore.dto.request.auth.AuthRegisterRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.auth.AuthLoginResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.TokenRepository;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthJWTUtility authJWTUtility;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private AuthRegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtUserExpirationSeconds", 600);
        ReflectionTestUtils.setField(authService, "jwtSellerExpirationSeconds", 600);
        ReflectionTestUtils.setField(authService, "jwtAdminExpirationSeconds", 3600);

        validRequest = new AuthRegisterRequest(
            "test@example.com",
            "Password123",
            "testuser",
            "08123456789",
            "Test User",
            LocalDate.of(1990, 1, 1)
        );
    }

    @Test
    void login_Success_ForSeller() {
        // Arrange
        AuthLoginRequest request = new AuthLoginRequest();
        request.setAuthLoginRequestEmail("seller.tech@example.com");
        request.setAuthLoginRequestPassword("password123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("seller.tech@example.com")
                .passwordHash("hashed_password")
                .build();

        when(authRepository.findByEmail(request.getAuthLoginRequestEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getAuthLoginRequestPassword(), user.getPasswordHash())).thenReturn(true);
        when(authRepository.findUserRole(user.getId())).thenReturn("SELLER");
        when(authRepository.isSellerActive(user.getEmail())).thenReturn(true);
        when(authJWTUtility.generateToken(any(), any(), any(), anyInt())).thenReturn("mocked_jwt_token");
        when(tokenRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        // Act
        RestApiResponse<AuthLoginResponse> response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertEquals("mocked_jwt_token", response.getRestApiResponseData().getAccessToken());
        assertEquals("SELLER", response.getRestApiResponseData().getRole());
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    void login_Success_ForAdmin() {
        // Arrange
        AuthLoginRequest request = new AuthLoginRequest();
        request.setAuthLoginRequestEmail("admin@example.com");
        request.setAuthLoginRequestPassword("password123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("admin@example.com")
                .passwordHash("hashed_password")
                .build();

        when(authRepository.findByEmail(request.getAuthLoginRequestEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getAuthLoginRequestPassword(), user.getPasswordHash())).thenReturn(true);
        when(authRepository.findUserRole(user.getId())).thenReturn("ADMIN");
        when(authJWTUtility.generateToken(any(), any(), any(), anyInt())).thenReturn("mocked_jwt_token");
        when(tokenRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        // Act
        RestApiResponse<AuthLoginResponse> response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertEquals("ADMIN", response.getRestApiResponseData().getRole());
        assertEquals(3600, response.getRestApiResponseData().getExpiresIn());
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    void login_UserNotFound_ShouldThrowException() {
        // Arrange
        AuthLoginRequest request = new AuthLoginRequest();
        request.setAuthLoginRequestEmail("unknown@example.com");

        when(authRepository.findByEmail(request.getAuthLoginRequestEmail())).thenReturn(Optional.empty());

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> authService.login(request));
        assertEquals(RestApiError.AUT_0004.getCode(), exception.getCode());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void login_WrongPassword_ShouldThrowException() {
        // Arrange
        AuthLoginRequest request = new AuthLoginRequest();
        request.setAuthLoginRequestEmail("seller.tech@example.com");
        request.setAuthLoginRequestPassword("wrong_password");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("seller.tech@example.com")
                .passwordHash("hashed_password")
                .build();

        when(authRepository.findByEmail(request.getAuthLoginRequestEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getAuthLoginRequestPassword(), user.getPasswordHash())).thenReturn(false);

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> authService.login(request));
        assertEquals(RestApiError.AUT_0004.getCode(), exception.getCode());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void login_SuspendedSeller_ShouldThrowException() {
        // Arrange
        AuthLoginRequest request = new AuthLoginRequest();
        request.setAuthLoginRequestEmail("seller.tech@example.com");
        request.setAuthLoginRequestPassword("password123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("seller.tech@example.com")
                .passwordHash("hashed_password")
                .build();

        when(authRepository.findByEmail(request.getAuthLoginRequestEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getAuthLoginRequestPassword(), user.getPasswordHash())).thenReturn(true);
        when(authRepository.findUserRole(user.getId())).thenReturn("SELLER");
        when(authRepository.isSellerActive(user.getEmail())).thenReturn(false);

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> authService.login(request));
        assertEquals(RestApiError.AUT_0010.getCode(), exception.getCode());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void login_Success_ForOtherRole() {
        // Arrange
        AuthLoginRequest request = new AuthLoginRequest();
        request.setAuthLoginRequestEmail("user@example.com");
        request.setAuthLoginRequestPassword("password123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .passwordHash("hashed_password")
                .build();

        when(authRepository.findByEmail(request.getAuthLoginRequestEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getAuthLoginRequestPassword(), user.getPasswordHash())).thenReturn(true);
        when(authRepository.findUserRole(user.getId())).thenReturn("CUSTOMER");
        when(authJWTUtility.generateToken(any(), any(), any(), eq(600))).thenReturn("mocked_jwt_token");
        when(tokenRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        // Act
        RestApiResponse<AuthLoginResponse> response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertEquals("CUSTOMER", response.getRestApiResponseData().getRole());
        assertEquals(600, response.getRestApiResponseData().getExpiresIn());
    }

    @Test
    void logout_Success() throws CoreThrowHandler {
        String tokenStr = "mocked_jwt_token";
        Token token = new Token();
        token.setToken(tokenStr);

        when(tokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

        RestApiResponse<Void> response = authService.logout("Bearer " + tokenStr);
        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        verify(tokenRepository).delete(token);
    }

    @Test
    void logout_NullHeader_ShouldThrow() {
        assertThrows(CoreThrowHandler.class, () -> authService.logout(null));
        assertThrows(CoreThrowHandler.class, () -> authService.logout("InvalidHeader"));
    }

    @Test
    void logout_TokenNotFound_ShouldThrow() {
        String tokenStr = "mocked_jwt_token";
        when(tokenRepository.findByToken(tokenStr)).thenReturn(Optional.empty());

        assertThrows(CoreThrowHandler.class, () -> authService.logout("Bearer " + tokenStr));
    }

    @Test
    void testRegisterSuccess() throws CoreThrowHandler {
        when(authRepository.existsByEmail(anyString())).thenReturn(false);
        when(authRepository.existsByUsername(anyString())).thenReturn(false);
        when(authRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(authRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RestApiResponse<Void> response = authService.register(validRequest);

        assertNotNull(response);
        assertEquals("Registration successful. Please login.", response.getRestApiResponseMessage());
        verify(authRepository).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmail() {
        when(authRepository.existsByEmail("test@example.com")).thenReturn(true);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
            () -> authService.register(validRequest));
        assertEquals(RestApiError.AUT_0017.getCode(), exception.getCode());

        verify(authRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateUsername() {
        when(authRepository.existsByEmail(anyString())).thenReturn(false);
        when(authRepository.existsByUsername("testuser")).thenReturn(true);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
            () -> authService.register(validRequest));
        assertEquals(RestApiError.AUT_0017.getCode(), exception.getCode());

        verify(authRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterDuplicatePhone() {
        when(authRepository.existsByEmail(anyString())).thenReturn(false);
        when(authRepository.existsByUsername(anyString())).thenReturn(false);
        when(authRepository.existsByPhoneNumber("08123456789")).thenReturn(true);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
            () -> authService.register(validRequest));
        assertEquals(RestApiError.AUT_0017.getCode(), exception.getCode());

        verify(authRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterPasswordHashing() throws CoreThrowHandler {
        when(authRepository.existsByEmail(anyString())).thenReturn(false);
        when(authRepository.existsByUsername(anyString())).thenReturn(false);
        when(authRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$10$hashedpassword");
        when(authRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(validRequest);

        verify(passwordEncoder).encode("Password123");
        verify(authRepository).save(argThat(user ->
            user.getPasswordHash().equals("$2a$10$hashedpassword")
        ));
    }
}
