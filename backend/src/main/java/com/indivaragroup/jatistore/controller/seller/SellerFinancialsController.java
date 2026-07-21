package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.request.seller.SellerWithdrawalRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerBalanceSummaryResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerFinancialDashboardResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerLedgerTransactionResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerWithdrawalResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.seller.SellerFinancialsService;
import com.indivaragroup.jatistore.service.seller.SellerSecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.SELLER_FINANCIALS_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerFinancialsController {

    private final SellerFinancialsService financialsService;
    private final SellerSecurityHelper securityHelper;

    @GetMapping
    public RestApiResponse<SellerFinancialDashboardResponse> getDashboard(Principal principal) throws CoreThrowHandler {
        return RestApiResponse.success(financialsService.getDashboard(securityHelper.getSellerIdFromPrincipal(principal)));
    }

    @GetMapping("/balance")
    public RestApiResponse<SellerBalanceSummaryResponse> getBalanceSummary(Principal principal) throws CoreThrowHandler {
        return RestApiResponse.success(financialsService.getBalanceSummary(securityHelper.getSellerIdFromPrincipal(principal)));
    }

    @GetMapping("/transactions")
    public RestApiResponse<Page<SellerLedgerTransactionResponse>> getTransactionHistory(
            Principal principal,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) throws CoreThrowHandler {
        return RestApiResponse.success(financialsService.getTransactionHistory(securityHelper.getSellerIdFromPrincipal(principal), type, search, page, size, sort));
    }

    @PostMapping("/withdraw")
    public RestApiResponse<SellerWithdrawalResponse> simulateWithdrawal(
            Principal principal,
            @RequestBody SellerWithdrawalRequest request
    ) throws CoreThrowHandler {
        return RestApiResponse.success(financialsService.simulateWithdrawal(securityHelper.getSellerIdFromPrincipal(principal), request));
    }
}
