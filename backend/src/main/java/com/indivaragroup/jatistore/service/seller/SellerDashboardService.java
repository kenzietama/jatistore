package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.DashboardStatsResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.FinancialOverviewResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.RecentOrderResponse;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

public interface SellerDashboardService {
    DashboardStatsResponse getDashboardStats(UUID sellerId);
    FinancialOverviewResponse getFinancialOverview(UUID sellerId);
    Page<RecentOrderResponse> getRecentOrders(UUID sellerId, String search, String status, String sortBy, String sortDir, int page, int limit);
}
