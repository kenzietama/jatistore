package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.request.user.UserCheckoutRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCheckoutResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.user.UserCheckoutService;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class UserCheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserCheckoutService userCheckoutService;

    @MockitoBean
    private AuthJWTUtility authJWTUtility;

    @MockitoBean
    private AuthRepository authRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    private UserCheckoutRequest walletCheckoutRequest;
    private UserCheckoutRequest cardCheckoutRequest;
    private UserCheckoutResponse mockResponse;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        // Create proper UserDetails mock
        testUser = User.builder()
                .username("user@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Wallet checkout request
        walletCheckoutRequest = new UserCheckoutRequest();
        walletCheckoutRequest.setUserSelectedCartItemId(new UUID[]{UUID.randomUUID()});
        walletCheckoutRequest.setUserCheckoutRequestPaymentMethod(PaymentMethod.WALLET);

        // Card checkout request
        cardCheckoutRequest = new UserCheckoutRequest();
        cardCheckoutRequest.setUserSelectedCartItemId(new UUID[]{UUID.randomUUID()});
        cardCheckoutRequest.setUserCheckoutRequestPaymentMethod(PaymentMethod.CARD);
        cardCheckoutRequest.setUserCheckoutRequestCardNumber("4111111111111111");
        cardCheckoutRequest.setUserCheckoutRequestCardHolderName("John Doe");
        cardCheckoutRequest.setUserCheckoutRequestExpiryDate("12/25");
        cardCheckoutRequest.setUserCheckoutRequestCvc("123");

        // Mock response
        mockResponse = new UserCheckoutResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "gateway-ref-123",
                "PAID_ON_HOLD",
                BigDecimal.valueOf(200)
        );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void checkout_Success_WalletPayment() throws Exception {
        // Arrange
        RestApiResponse<UserCheckoutResponse> successResponse = RestApiResponse.success(mockResponse);
        when(userCheckoutService.checkout(any(UserCheckoutRequest.class), any(String.class)))
                .thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletCheckoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.order_id").value(mockResponse.getOrder_id().toString()))
                .andExpect(jsonPath("$.data.transaction_id").value(mockResponse.getTransaction_id().toString()))
                .andExpect(jsonPath("$.data.order_status").value("PAID_ON_HOLD"))
                .andExpect(jsonPath("$.data.total_amount").value(200));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void checkout_Success_CardPayment() throws Exception {
        // Arrange
        RestApiResponse<UserCheckoutResponse> successResponse = RestApiResponse.success(mockResponse);
        when(userCheckoutService.checkout(any(UserCheckoutRequest.class), any(String.class)))
                .thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCheckoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void checkout_Fail_EmptyCartItems() throws Exception {
        // Arrange
        UserCheckoutRequest invalidRequest = new UserCheckoutRequest();
        invalidRequest.setUserSelectedCartItemId(new UUID[]{});
        invalidRequest.setUserCheckoutRequestPaymentMethod(PaymentMethod.WALLET);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .principal(() -> "user@example.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_Fail_NullPaymentMethod() throws Exception {
        // Arrange
        UserCheckoutRequest invalidRequest = new UserCheckoutRequest();
        invalidRequest.setUserSelectedCartItemId(new UUID[]{UUID.randomUUID()});
        invalidRequest.setUserCheckoutRequestPaymentMethod(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .principal(() -> "user@example.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void checkout_Fail_InsufficientStock() throws Exception {
        // Arrange
        when(userCheckoutService.checkout(any(UserCheckoutRequest.class), any(String.class)))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0011));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletCheckoutRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void checkout_Fail_MultipleSellerInCart() throws Exception {
        // Arrange
        when(userCheckoutService.checkout(any(UserCheckoutRequest.class), any(String.class)))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0019));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletCheckoutRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void checkout_Fail_PaymentDeclined() throws Exception {
        // Arrange
        when(userCheckoutService.checkout(any(UserCheckoutRequest.class), any(String.class)))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0012));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletCheckoutRequest)))
                .andExpect(status().isPaymentRequired());
    }
}
