package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.request.user.PayOrderRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCheckoutResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.user.UserCheckoutService;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.TokenRepository;
import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserCheckoutController.class)
public class UserCheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserCheckoutService userCheckoutService;

    @MockitoBean
    private com.indivaragroup.jatistore.service.utility.AuthJWTUtility authJWTUtility;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private PayOrderRequest walletPayRequest;
    private PayOrderRequest cardPayRequest;
    private UserCheckoutResponse mockResponse;
    private UserDetails testUser;
    private Principal mockPrincipal;
    private UUID orderId;
    private List<UUID> cartItemIds;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "user@example.com";

        testUser = User.builder()
                .username("user@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        orderId = UUID.randomUUID();
        cartItemIds = List.of(UUID.randomUUID());

        walletPayRequest = PayOrderRequest.builder()
                .paymentMethod(PaymentMethod.WALLET)
                .build();

        cardPayRequest = PayOrderRequest.builder()
                .paymentMethod(PaymentMethod.CARD)
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvc("123")
                .build();

        mockResponse = new UserCheckoutResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "gateway-ref-123",
                "PAID_ON_HOLD",
                BigDecimal.valueOf(200)
        );
    }

    @Test
    void payOrder_Success_WalletPayment() throws Exception {
        RestApiResponse<UserCheckoutResponse> successResponse = RestApiResponse.success(mockResponse);
        when(userCheckoutService.payOrder(any(UUID.class), any(PayOrderRequest.class), anyList(), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletPayRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.order_id").value(mockResponse.getOrder_id().toString()))
                .andExpect(jsonPath("$.data.transaction_id").value(mockResponse.getTransaction_id().toString()))
                .andExpect(jsonPath("$.data.order_status").value("PAID_ON_HOLD"))
                .andExpect(jsonPath("$.data.total_amount").value(200));
    }

    @Test
    void payOrder_Success_CardPayment() throws Exception {
        RestApiResponse<UserCheckoutResponse> successResponse = RestApiResponse.success(mockResponse);
        when(userCheckoutService.payOrder(any(UUID.class), any(PayOrderRequest.class), anyList(), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardPayRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void payOrder_Fail_NullPaymentMethod() throws Exception {
        PayOrderRequest invalidRequest = PayOrderRequest.builder()
                .paymentMethod(null)
                .build();

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payOrder_Fail_OrderNotFound() throws Exception {
        when(userCheckoutService.payOrder(any(UUID.class), any(PayOrderRequest.class), anyList(), anyString()))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0015));

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletPayRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void payOrder_Fail_OrderNotPayable() throws Exception {
        when(userCheckoutService.payOrder(any(UUID.class), any(PayOrderRequest.class), anyList(), anyString()))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0022));

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletPayRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void payOrder_Fail_AmountChanged() throws Exception {
        when(userCheckoutService.payOrder(any(UUID.class), any(PayOrderRequest.class), anyList(), anyString()))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0023));

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletPayRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void payOrder_Fail_InsufficientStock() throws Exception {
        when(userCheckoutService.payOrder(any(UUID.class), any(PayOrderRequest.class), anyList(), anyString()))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0011));

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletPayRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payOrder_Fail_PaymentDeclined_Wallet() throws Exception {
        when(userCheckoutService.payOrder(any(UUID.class), any(PayOrderRequest.class), anyList(), anyString()))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0013));

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletPayRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isPaymentRequired());
    }

    @Test
    void payOrder_Fail_PaymentDeclined_Card() throws Exception {
        when(userCheckoutService.payOrder(any(UUID.class), any(PayOrderRequest.class), anyList(), anyString()))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0013));

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardPayRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isPaymentRequired());
    }

    @Test
    void payOrder_Fail_PaymentTimeout() throws Exception {
        when(userCheckoutService.payOrder(any(UUID.class), any(PayOrderRequest.class), anyList(), anyString()))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0017));

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/pay")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletPayRequest))
                        .param("cartItemIds", cartItemIds.get(0).toString()))
                .andExpect(status().isRequestTimeout());
    }
}
