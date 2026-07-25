package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserBalanceResponse;
import com.indivaragroup.jatistore.service.user.UserBalanceService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(UserBalanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserBalanceService userBalanceService;

    @MockitoBean
    private com.indivaragroup.jatistore.service.utility.AuthJWTUtility authJWTUtility;
    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;
    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;
    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    
    @BeforeEach
    void setUp() {
    }

    @Test
    void getBalance_ShouldReturnOk() throws Exception {
        UserBalanceResponse data = new UserBalanceResponse(new BigDecimal("150000"));
        RestApiResponse<UserBalanceResponse> response = RestApiResponse.<UserBalanceResponse>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage("Wallet balance retrieved successfully.")
                .restApiResponseData(data)
                .restApiResponseTimestamp(Instant.now())
                .build();

        when(userBalanceService.getBalance()).thenReturn(response);

        mockMvc.perform(get("/api/v1/user/balance")
                        .with(user("test").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(150000));
    }
}
