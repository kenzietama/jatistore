package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.request.module.seller.FlashSaleItemRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.SellerFlashSaleResponse;
import com.indivaragroup.jatistore.service.seller.SellerFlashSaleService;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SellerFlashSaleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SellerFlashSaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SellerFlashSaleService sellerFlashSaleService;

    @MockitoBean
    private AuthJWTUtility authJWTUtility;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void getAvailableFlashSales_shouldReturnOk() throws Exception {
        SellerFlashSaleResponse.Available response = SellerFlashSaleResponse.Available.builder()
                .name("Event")
                .status("upcoming")
                .build();
        when(sellerFlashSaleService.getAvailableFlashSales(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/seller/flash-sales/available")
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user@example.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Event"));
    }

    @Test
    void getFlashSaleItems_shouldReturnOk() throws Exception {
        UUID fsId = UUID.randomUUID();
        SellerFlashSaleResponse.Wrapper wrapper = SellerFlashSaleResponse.Wrapper.builder()
                .eventId(fsId)
                .build();
        when(sellerFlashSaleService.getFlashSaleItems(any(), eq(fsId), anyInt(), anyInt())).thenReturn(wrapper);

        mockMvc.perform(get("/api/v1/seller/flash-sales/{flashSaleId}/items", fsId)
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user@example.com", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").value(fsId.toString()));
    }

    @Test
    void addFlashSaleItem_shouldReturnCreated() throws Exception {
        UUID fsId = UUID.randomUUID();
        FlashSaleItemRequest request = new FlashSaleItemRequest();
        request.setProductId(UUID.randomUUID());
        request.setFlashPrice(new BigDecimal("100"));
        request.setRemainingQuota(10);

        doNothing().when(sellerFlashSaleService).addFlashSaleItem(any(), eq(fsId), any(FlashSaleItemRequest.class));

        mockMvc.perform(post("/api/v1/seller/flash-sales/{flashSaleId}/items", fsId)
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user@example.com", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.flashSaleId").value(fsId.toString()));
    }

    @Test
    void removeFlashSaleItem_shouldReturnOk() throws Exception {
        UUID fsId = UUID.randomUUID();
        UUID pId = UUID.randomUUID();
        doNothing().when(sellerFlashSaleService).removeFlashSaleItem(any(), eq(fsId), eq(pId));

        mockMvc.perform(delete("/api/v1/seller/flash-sales/{flashSaleId}/items/{productId}", fsId, pId)
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user@example.com", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void updateFlashSaleItem_shouldReturnOk() throws Exception {
        UUID fsId = UUID.randomUUID();
        UUID pId = UUID.randomUUID();
        FlashSaleItemRequest request = new FlashSaleItemRequest();
        request.setProductId(pId);
        request.setFlashPrice(new BigDecimal("100"));
        request.setRemainingQuota(10);

        doNothing().when(sellerFlashSaleService).updateFlashSaleItem(any(), eq(fsId), eq(pId), any(FlashSaleItemRequest.class));

        mockMvc.perform(patch("/api/v1/seller/flash-sales/{flashSaleId}/items/{productId}", fsId, pId)
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user@example.com", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Flash sale item updated successfully."));
    }
}
