package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.SellerLedger;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import com.indivaragroup.jatistore.dto.request.seller.SellerWithdrawalRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerBalanceSummaryResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerFinancialDashboardResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerLedgerTransactionResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerWithdrawalResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.SellerLedgerRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import com.indivaragroup.jatistore.service.payment.PaymentGatewayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerFinancialsService {

    private final SellerRepository sellerRepository;
    private final SellerLedgerRepository sellerLedgerRepository;
    private final PaymentGatewayClient paymentGatewayClient;

    private static final BigDecimal MAX_WITHDRAWAL_LIMIT = new BigDecimal("1000000000");

    private Seller getSellerById(UUID sellerId) throws CoreThrowHandler {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> {
                    log.error("Seller not found for ID: {}", sellerId);
                    return new CoreThrowHandler(RestApiError.SLR_0002);
                });
    }

    public SellerFinancialDashboardResponse getDashboard(UUID sellerId) throws CoreThrowHandler {
        log.info("Fetching financial dashboard for seller ID: {}", sellerId);
        Seller seller = getSellerById(sellerId);

        List<SellerLedger> top50 = sellerLedgerRepository.findBySellerIdOrderByCreatedAtDescAmountDesc(sellerId, PageRequest.of(0, 50)).getContent();

        return SellerFinancialDashboardResponse.builder()
                .availableBalance(seller.getCachedAvailableBalance())
                .onHoldBalance(seller.getCachedOnHoldBalance())
                .totalEarnings(seller.getCachedAvailableBalance().add(seller.getCachedOnHoldBalance()))
                .recentTransactions(top50.stream().map(SellerLedgerTransactionResponse::from).toList())
                .build();
    }

    public SellerBalanceSummaryResponse getBalanceSummary(UUID sellerId) throws CoreThrowHandler {
        log.info("Fetching balance summary for seller ID: {}", sellerId);
        Seller seller = getSellerById(sellerId);
        return SellerBalanceSummaryResponse.builder()
                .availableBalance(seller.getCachedAvailableBalance())
                .onHoldBalance(seller.getCachedOnHoldBalance())
                .totalEarnings(seller.getCachedAvailableBalance().add(seller.getCachedOnHoldBalance()))
                .build();
    }

    public Page<SellerLedgerTransactionResponse> getTransactionHistory(UUID sellerId, String type, String search, int page, int size, String sort) throws CoreThrowHandler {
        log.info("Fetching transaction history for seller ID: {}, type: {}, search: {}", sellerId, type, search);
        Seller seller = getSellerById(sellerId);
        
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("createdAt"),
                org.springframework.data.domain.Sort.Order.desc("amount")
        );
        
        if ("amount_desc".equalsIgnoreCase(sort)) {
            sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "amount");
        } else if ("amount_asc".equalsIgnoreCase(sort)) {
            sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "amount");
        } else if ("date_asc".equalsIgnoreCase(sort)) {
            sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "createdAt");
        }
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<SellerLedger> ledgerPage = sellerLedgerRepository.findTransactionsBySellerId(sellerId, type, search == null ? "" : search, pageable);

        return ledgerPage.map(SellerLedgerTransactionResponse::from);
    }

    @Audit(action = "WITHDRAWAL", affectedModule = "FINANCIALS", description = "Seller withdraws available balance")
    @Transactional
    public SellerWithdrawalResponse simulateWithdrawal(UUID sellerId, SellerWithdrawalRequest request) throws CoreThrowHandler {
        log.info("Simulating withdrawal for seller ID: {}", sellerId);
        Seller seller = getSellerById(sellerId);

        if (request == null || request.getAmount() == null) {
            log.warn("Withdrawal request or amount is null for seller ID: {}", sellerId);
            throw new CoreThrowHandler(RestApiError.GEN_0001, "amount");
        }

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Withdrawal amount must be greater than zero. Seller ID: {}, amount: {}", sellerId, amount);
            throw new CoreThrowHandler(RestApiError.SLR_0041);
        }

        if (amount.compareTo(seller.getCachedAvailableBalance()) > 0) {
            log.warn("Insufficient available balance. Seller ID: {}, requested: {}, available: {}", sellerId, amount, seller.getCachedAvailableBalance());
            throw new CoreThrowHandler(RestApiError.SLR_0042);
        }

        if (amount.compareTo(MAX_WITHDRAWAL_LIMIT) > 0) {
            log.warn("Withdrawal amount exceeds maximum limit. Seller ID: {}, requested: {}, limit: {}", sellerId, amount, MAX_WITHDRAWAL_LIMIT);
            throw new CoreThrowHandler(RestApiError.SLR_0043);
        }

        SellerLedger debitEntry = SellerLedger.builder()
                .seller(seller)
                .amount(amount.negate())
                .balanceType(BalanceType.AVAILABLE)
                .order(null)
                .build();

        debitEntry = sellerLedgerRepository.save(debitEntry);
        BigDecimal newBalance = seller.getCachedAvailableBalance().subtract(amount);

        String gatewayRef;
        try {
            log.info("Calling payment gateway to simulate payout of {}", amount);
            gatewayRef = paymentGatewayClient.simulatePayout(amount);
        } catch (Exception e) {
            log.error("Payment gateway payout simulation failed: {}", e.getMessage(), e);
            throw new CoreThrowHandler(RestApiError.USR_0014); // Or a specific SLR payout error if one existed, but reusing USR_0014 is okay.
        }

        log.info("Withdrawal simulation successful for seller ID: {}", sellerId);
        return SellerWithdrawalResponse.builder()
                .amount(amount)
                .newAvailableBalance(newBalance)
                .withdrawalId(debitEntry.getId())
                .mockGatewayRef(gatewayRef)
                .build();
    }
}
