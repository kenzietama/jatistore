package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.DashboardStatsResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.FinancialOverviewResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.RecentOrderResponse;
import com.indivaragroup.jatistore.service.seller.SellerDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.data.domain.PageImpl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SellerDashboardController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for this unit test
public class SellerDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerDashboardService dashboardService;

    private UUID mockSellerId;

    @BeforeEach
    void setUp() {
        mockSellerId = UUID.fromString("bb000000-0000-0000-0000-000000000001");
    }

    @Test
    void getStats_shouldReturnOk() throws Exception {
        DashboardStatsResponse mockResponse = new DashboardStatsResponse("Alex Mercer", 25L, 15L);
        when(dashboardService.getDashboardStats(mockSellerId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/seller/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellerName").value("Alex Mercer"))
                .andExpect(jsonPath("$.totalOrders").value(25))
                .andExpect(jsonPath("$.totalProducts").value(15));
    }

    @Test
    void getFinancials_shouldReturnOk() throws Exception {
        FinancialOverviewResponse mockResponse = new FinancialOverviewResponse(new BigDecimal("1500.00"), new BigDecimal("300.00"));
        when(dashboardService.getFinancialOverview(mockSellerId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/seller/dashboard/financial")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(1500.00))
                .andExpect(jsonPath("$.onHoldBalance").value(300.00));
    }

    @Test
    void getRecentOrders_shouldReturnOk() throws Exception {
        RecentOrderResponse recentOrder = new RecentOrderResponse(
                UUID.randomUUID(),
                "#ORD-1234",
                "Product",
                "image.jpg",
                BigDecimal.valueOf(100.0),
                1,
                "RECEIVED"
        );
        when(dashboardService.getRecentOrders(eq(mockSellerId), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Arrays.asList(recentOrder)));

        mockMvc.perform(get("/api/seller/dashboard/orders/recent")
                .param("limit", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].displayId").value("#ORD-1234"))
                .andExpect(jsonPath("$.content[0].itemName").value("Product"))
                .andExpect(jsonPath("$.content[0].amount").value(100.0))
                .andExpect(jsonPath("$.content[0].status").value("RECEIVED"));
    }
}
