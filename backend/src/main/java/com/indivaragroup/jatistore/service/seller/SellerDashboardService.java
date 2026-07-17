package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.DashboardStatsResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.FinancialOverviewResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.RecentOrderResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.SellerProfileResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;

import java.util.UUID;

import org.springframework.data.domain.Page;

public interface SellerDashboardService {
    DashboardStatsResponse getDashboardStats(UUID sellerId) throws CoreThrowHandler;
    FinancialOverviewResponse getFinancialOverview(UUID sellerId) throws CoreThrowHandler;
    Page<RecentOrderResponse> getRecentOrders(UUID sellerId, String search, String status, String sortBy, String sortDir, int page, int limit) throws CoreThrowHandler;
    SellerProfileResponse getProfile(UUID sellerId) throws CoreThrowHandler;
}
