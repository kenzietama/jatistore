package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.request.seller.SellerWithdrawalRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerBalanceSummaryResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerFinancialDashboardResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerLedgerTransactionResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerWithdrawalResponse;
import com.indivaragroup.jatistore.service.seller.SellerFinancialsService;
import com.indivaragroup.jatistore.service.seller.SellerSecurityHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SellerFinancialsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SellerFinancialsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerFinancialsService financialsService;

    @MockitoBean
    private SellerSecurityHelper securityHelper;
    
    // We need these beans because WebMvcTest loads security config which might require them
    @MockitoBean
    private com.indivaragroup.jatistore.service.utility.AuthJWTUtility authJWTUtility;
    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;
    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    private UUID mockSellerId;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockSellerId = UUID.randomUUID();
        mockPrincipal = () -> "seller@test.com";
        when(securityHelper.getSellerIdFromPrincipal(any())).thenReturn(mockSellerId);
    }

    @Test
    void getDashboard_shouldReturnOk() throws Exception {
        SellerFinancialDashboardResponse mockResponse = SellerFinancialDashboardResponse.builder()
                .availableBalance(new BigDecimal("1000"))
                .build();
        when(financialsService.getDashboard(mockSellerId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/seller/financials")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableBalance").value(1000));
    }

    @Test
    void getBalanceSummary_shouldReturnOk() throws Exception {
        SellerBalanceSummaryResponse mockResponse = SellerBalanceSummaryResponse.builder()
                .availableBalance(new BigDecimal("1000"))
                .build();
        when(financialsService.getBalanceSummary(mockSellerId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/seller/financials/balance")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableBalance").value(1000));
    }

    @Test
    void getTransactionHistory_shouldReturnOk() throws Exception {
        SellerLedgerTransactionResponse transaction = SellerLedgerTransactionResponse.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("100"))
                .build();
        when(financialsService.getTransactionHistory(eq(mockSellerId), eq("ALL"), eq(""), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(transaction)));

        mockMvc.perform(get("/api/v1/seller/financials/transactions")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].amount").value(100));
    }

    @Test
    void simulateWithdrawal_shouldReturnOk() throws Exception {
        SellerWithdrawalRequest request = new SellerWithdrawalRequest();
        request.setAmount(new BigDecimal("500"));
        
        SellerWithdrawalResponse mockResponse = SellerWithdrawalResponse.builder()
                .amount(new BigDecimal("500"))
                .newAvailableBalance(new BigDecimal("500"))
                .build();
                
        when(financialsService.simulateWithdrawal(eq(mockSellerId), any(SellerWithdrawalRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/seller/financials/withdraw")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(500))
                .andExpect(jsonPath("$.data.newAvailableBalance").value(500));
    }
}
