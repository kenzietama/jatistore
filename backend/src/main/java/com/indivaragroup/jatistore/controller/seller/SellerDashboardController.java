package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.SellerProfileResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.DashboardStatsResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.FinancialOverviewResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.RecentOrderResponse;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import com.indivaragroup.jatistore.service.seller.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/seller/dashboard")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;
    private final AuthRepository authRepository;
    private final SellerRepository sellerRepository;

    private UUID getSellerIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = authRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a registered seller"));
        return seller.getId();
    }

    @GetMapping("/profile")
    public ResponseEntity<SellerProfileResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(dashboardService.getProfile(getSellerIdFromPrincipal(principal)));
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(Principal principal) {
        return ResponseEntity.ok(dashboardService.getDashboardStats(getSellerIdFromPrincipal(principal)));
    }

    @GetMapping("/financial")
    public ResponseEntity<FinancialOverviewResponse> getFinancials(Principal principal) {
        return ResponseEntity.ok(dashboardService.getFinancialOverview(getSellerIdFromPrincipal(principal)));
    }

    @GetMapping("/orders/recent")
    public ResponseEntity<Page<RecentOrderResponse>> getRecentOrders(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentOrders(getSellerIdFromPrincipal(principal), search, status, sortBy, sortDir, page, limit));
    }
}
