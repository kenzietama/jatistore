package com.indivaragroup.jatistore.controller.user;

import tools.jackson.databind.ObjectMapper;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderConfirmReceiptResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderHistoryItemResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderItemResponse;
import com.indivaragroup.jatistore.service.user.UserOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserOrderService userOrderService;

    @MockitoBean
    private com.indivaragroup.jatistore.service.utility.AuthJWTUtility authJWTUtility;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void getOrderHistory_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderItemResponse item = OrderItemResponse.builder()
                .productName("Test Product")
                .quantity(2)
                .pricePerItem(BigDecimal.valueOf(100))
                .isFlashSale(false)
                .build();

        OrderHistoryItemResponse orderHistory = OrderHistoryItemResponse.builder()
                .orderId(orderId)
                .orderDate(Instant.now())
                .totalAmount(BigDecimal.valueOf(200))
                .status(OrderStatus.SHIPPED)
                .items(List.of(item))
                .build();

        Page<OrderHistoryItemResponse> page = new PageImpl<>(
                List.of(orderHistory),
                PageRequest.of(0, 20),
                1
        );

        RestApiResponse<Page<OrderHistoryItemResponse>> apiResponse = RestApiResponse.<Page<OrderHistoryItemResponse>>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Order history retrieved successfully.")
                .restApiResponseData(page)
                .build();

        when(userOrderService.getOrderHistory(eq("user@example.com"), eq(null), any()))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/api/v1/user/checkout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].orderId").value(orderId.toString()));

        verify(userOrderService, times(1)).getOrderHistory(eq("user@example.com"), eq(null), any());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void getOrderHistory_WithStatusFilter_Success() throws Exception {
        Page<OrderHistoryItemResponse> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        RestApiResponse<Page<OrderHistoryItemResponse>> apiResponse = RestApiResponse.<Page<OrderHistoryItemResponse>>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Order history retrieved successfully.")
                .restApiResponseData(page)
                .build();

        when(userOrderService.getOrderHistory(eq("user@example.com"), eq(OrderStatus.SHIPPED), any()))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/api/v1/user/checkout")
                        .param("status", "SHIPPED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userOrderService, times(1)).getOrderHistory(eq("user@example.com"), eq(OrderStatus.SHIPPED), any());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void getOrderHistory_WithPagination_Success() throws Exception {
        Page<OrderHistoryItemResponse> page = new PageImpl<>(
                List.of(),
                PageRequest.of(1, 10),
                0
        );

        RestApiResponse<Page<OrderHistoryItemResponse>> apiResponse = RestApiResponse.<Page<OrderHistoryItemResponse>>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Order history retrieved successfully.")
                .restApiResponseData(page)
                .build();

        when(userOrderService.getOrderHistory(eq("user@example.com"), eq(null), any()))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/api/v1/user/checkout")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userOrderService, times(1)).getOrderHistory(eq("user@example.com"), eq(null), any());
    }

    @Test
    void getOrderHistory_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/user/checkout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userOrderService, never()).getOrderHistory(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"SELLER"})
    void getOrderHistory_WithWrongRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/user/checkout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userOrderService, never()).getOrderHistory(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void confirmReceipt_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderConfirmReceiptResponse response = OrderConfirmReceiptResponse.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.RECEIVED)
                .build();

        RestApiResponse<OrderConfirmReceiptResponse> apiResponse = RestApiResponse.<OrderConfirmReceiptResponse>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Order receipt confirmed. Funds released to seller.")
                .restApiResponseData(response)
                .build();

        when(userOrderService.confirmReceipt(eq("user@example.com"), eq(orderId)))
                .thenReturn(apiResponse);

        mockMvc.perform(post("/api/v1/user/checkout/" + orderId + "/confirm-receipt")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.orderStatus").value("RECEIVED"));

        verify(userOrderService, times(1)).confirmReceipt(eq("user@example.com"), eq(orderId));
    }

    @Test
    void confirmReceipt_WithoutAuthentication_ShouldReturn401() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/user/checkout/" + orderId + "/confirm-receipt")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userOrderService, never()).confirmReceipt(any(), any());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"SELLER"})
    void confirmReceipt_WithWrongRole_ShouldReturn403() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/user/checkout/" + orderId + "/confirm-receipt")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userOrderService, never()).confirmReceipt(any(), any());
    }
}
