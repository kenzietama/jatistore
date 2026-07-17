package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.SellerProfileResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.DashboardStatsResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.FinancialOverviewResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.RecentOrderResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.seller.SellerDashboardService;
import com.indivaragroup.jatistore.service.seller.SellerSecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.SELLER_DASHBOARD_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;
    private final SellerSecurityHelper securityHelper;

    @GetMapping("/profile")
    public RestApiResponse<SellerProfileResponse> getProfile(Principal principal) throws CoreThrowHandler {
        return RestApiResponse.success(dashboardService.getProfile(securityHelper.getSellerIdFromPrincipal(principal)));
    }

    @GetMapping("/stats")
    public RestApiResponse<DashboardStatsResponse> getStats(Principal principal) throws CoreThrowHandler {
        return RestApiResponse.success(dashboardService.getDashboardStats(securityHelper.getSellerIdFromPrincipal(principal)));
    }

    @GetMapping("/financial")
    public RestApiResponse<FinancialOverviewResponse> getFinancials(Principal principal) throws CoreThrowHandler {
        return RestApiResponse.success(dashboardService.getFinancialOverview(securityHelper.getSellerIdFromPrincipal(principal)));
    }

    @GetMapping("/orders/recent")
    public RestApiResponse<Page<RecentOrderResponse>> getRecentOrders(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit) throws CoreThrowHandler {
        return RestApiResponse.success(dashboardService.getRecentOrders(securityHelper.getSellerIdFromPrincipal(principal), search, status, sortBy, sortDir, page, limit));
    }
}
