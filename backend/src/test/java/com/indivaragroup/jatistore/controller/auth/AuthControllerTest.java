package com.indivaragroup.jatistore.controller.auth;

import tools.jackson.databind.ObjectMapper;
import com.indivaragroup.jatistore.dto.request.AuthLoginRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.auth.AuthLoginResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.auth.AuthService;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import com.indivaragroup.jatistore.repository.AuthRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters to test validation behavior directly
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthJWTUtility authJWTUtility;

    @MockitoBean
    private AuthRepository authRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void login_WithValidData_ShouldReturnOk() throws Exception {
        // Arrange
        AuthLoginRequest request = new AuthLoginRequest();
        request.setAuthLoginRequestEmail("seller.tech@example.com");
        request.setAuthLoginRequestPassword("password123");

        AuthLoginResponse loginResponse = new AuthLoginResponse();
        loginResponse.setAccessToken("dummy_token");
        loginResponse.setExpiresIn(600);
        loginResponse.setRole("SELLER");

        RestApiResponse<AuthLoginResponse> apiResponse = RestApiResponse.<AuthLoginResponse>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Login successful")
                .restApiResponseData(loginResponse)
                .build();

        when(authService.login(any(AuthLoginRequest.class))).thenReturn(apiResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("dummy_token"));

        verify(authService, times(1)).login(any(AuthLoginRequest.class));
    }

    @Test
    void login_WithEmptyEmail_ShouldReturn400BadRequest() throws Exception {
        // Arrange: Email is blank
        AuthLoginRequest request = new AuthLoginRequest();
        request.setAuthLoginRequestEmail("");
        request.setAuthLoginRequestPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.error.email").value("Missing mandatory property email"));

        verify(authService, never()).login(any());
    }

    @Test
    void login_WithPasswordTooShort_ShouldReturn400BadRequest() throws Exception {
        // Arrange: Password is too short (< 4 chars)
        AuthLoginRequest request = new AuthLoginRequest();
        request.setAuthLoginRequestEmail("seller.tech@example.com");
        request.setAuthLoginRequestPassword("123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.password").value("Maximum length for property password is 4"));

        verify(authService, never()).login(any());
    }
}
