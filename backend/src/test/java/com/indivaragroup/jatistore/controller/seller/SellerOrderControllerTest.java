package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderDetailResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderListResponse;
import com.indivaragroup.jatistore.service.seller.SellerOrderService;
import com.indivaragroup.jatistore.service.seller.SellerSecurityHelper;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SellerOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SellerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerOrderService sellerOrderService;

    @MockitoBean
    private SellerSecurityHelper securityHelper;

    @MockitoBean
    private AuthJWTUtility authJWTUtility;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void getOrders_shouldReturnOk() throws Exception {
        UUID sellerId = UUID.randomUUID();
        when(securityHelper.getSellerIdFromPrincipal(any(Principal.class))).thenReturn(sellerId);

        SellerOrderListResponse response = SellerOrderListResponse.builder()
                .orderId(UUID.randomUUID())
                .status("PROCESSED")
                .build();

        when(sellerOrderService.getSellerOrders(eq(sellerId), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/seller/orders")
                        .principal(() -> "seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].status").value("PROCESSED"));
    }

    @Test
    void getOrderDetail_shouldReturnOk() throws Exception {
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(securityHelper.getSellerIdFromPrincipal(any(Principal.class))).thenReturn(sellerId);

        SellerOrderDetailResponse response = SellerOrderDetailResponse.builder()
                .orderId(orderId)
                .status("PROCESSED")
                .build();

        when(sellerOrderService.getSellerOrderDetail(sellerId, orderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/seller/orders/{orderId}", orderId)
                        .principal(() -> "seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()));
    }

    @Test
    void markAsShipped_shouldReturnOk() throws Exception {
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(securityHelper.getSellerIdFromPrincipal(any(Principal.class))).thenReturn(sellerId);

        doNothing().when(sellerOrderService).markOrderAsShipped(sellerId, orderId);

        mockMvc.perform(patch("/api/v1/seller/orders/{orderId}/ship", orderId)
                        .principal(() -> "seller"))
                .andExpect(status().isOk());
    }
}
