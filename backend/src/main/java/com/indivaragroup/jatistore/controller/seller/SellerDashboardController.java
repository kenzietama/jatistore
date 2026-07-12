package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.DashboardStatsResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.FinancialOverviewResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.RecentOrderResponse;
import com.indivaragroup.jatistore.service.seller.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seller/dashboard")
@RequiredArgsConstructor
// @CrossOrigin(origins = "*") // Usually handled by global config, but adding if needed
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;

    // In a real app, sellerId is extracted from JWT token. 
    // For now, we accept it as a request param or header for testing/mock purposes, 
    // or we can hardcode for the first seller in the seed data: bb000000-0000-0000-0000-000000000001
    
    private UUID getMockSellerId() {
        return UUID.fromString("bb000000-0000-0000-0000-000000000001");
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats(getMockSellerId()));
    }

    @GetMapping("/financial")
    public ResponseEntity<FinancialOverviewResponse> getFinancials() {
        return ResponseEntity.ok(dashboardService.getFinancialOverview(getMockSellerId()));
    }

    @GetMapping("/orders/recent")
    public ResponseEntity<Page<RecentOrderResponse>> getRecentOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(getMockSellerId(), search, status, sortBy, sortDir, page, limit));
    }
}
