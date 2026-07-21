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
public class SellerFinancialsService {

    private final SellerRepository sellerRepository;
    private final SellerLedgerRepository sellerLedgerRepository;
    private final PaymentGatewayClient paymentGatewayClient;

    private static final BigDecimal MAX_WITHDRAWAL_LIMIT = new BigDecimal("1000000000");

    private Seller getSellerById(UUID sellerId) throws CoreThrowHandler {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0002));
    }

    public SellerFinancialDashboardResponse getDashboard(UUID sellerId) throws CoreThrowHandler {
        Seller seller = getSellerById(sellerId);

        List<SellerLedger> top50 = sellerLedgerRepository.findBySellerIdOrderByCreatedAtDescAmountDesc(sellerId, PageRequest.of(0, 50)).getContent();

        return SellerFinancialDashboardResponse.builder()
                .availableBalance(seller.getCachedAvailableBalance())
                .onHoldBalance(seller.getCachedOnHoldBalance())
                .totalEarnings(seller.getCachedAvailableBalance().add(seller.getCachedOnHoldBalance()))
                .recentTransactions(top50.stream().map(SellerLedgerTransactionResponse::from).collect(Collectors.toList()))
                .build();
    }

    public SellerBalanceSummaryResponse getBalanceSummary(UUID sellerId) throws CoreThrowHandler {
        Seller seller = getSellerById(sellerId);
        return SellerBalanceSummaryResponse.builder()
                .availableBalance(seller.getCachedAvailableBalance())
                .onHoldBalance(seller.getCachedOnHoldBalance())
                .totalEarnings(seller.getCachedAvailableBalance().add(seller.getCachedOnHoldBalance()))
                .build();
    }

    public Page<SellerLedgerTransactionResponse> getTransactionHistory(UUID sellerId, String type, int page, int size) throws CoreThrowHandler {
        Seller seller = getSellerById(sellerId);
        Pageable pageable = PageRequest.of(page, size);
        Page<SellerLedger> ledgerPage;

        if (type != null && !type.isBlank()) {
            try {
                BalanceType balanceType = BalanceType.valueOf(type.toUpperCase());
                ledgerPage = sellerLedgerRepository.findBySellerIdAndBalanceTypeOrderByCreatedAtDescAmountDesc(sellerId, balanceType, pageable);
            } catch (IllegalArgumentException e) {
                throw new CoreThrowHandler(RestApiError.SLR_0040);
            }
        } else {
            ledgerPage = sellerLedgerRepository.findBySellerIdOrderByCreatedAtDescAmountDesc(sellerId, pageable);
        }

        return ledgerPage.map(SellerLedgerTransactionResponse::from);
    }

    @Audit(action = "WITHDRAWAL", affectedModule = "FINANCIALS", description = "Seller withdraws available balance")
    @Transactional
    public SellerWithdrawalResponse simulateWithdrawal(UUID sellerId, SellerWithdrawalRequest request) throws CoreThrowHandler {
        Seller seller = getSellerById(sellerId);

        if (request == null || request.getAmount() == null) {
            throw new CoreThrowHandler(RestApiError.GEN_0001, "amount");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CoreThrowHandler(RestApiError.SLR_0041);
        }

        if (request.getAmount().compareTo(seller.getCachedAvailableBalance()) > 0) {
            throw new CoreThrowHandler(RestApiError.SLR_0042);
        }

        if (request.getAmount().compareTo(MAX_WITHDRAWAL_LIMIT) > 0) {
            throw new CoreThrowHandler(RestApiError.SLR_0043);
        }

        SellerLedger debitEntry = SellerLedger.builder()
                .seller(seller)
                .amount(request.getAmount().negate())
                .balanceType(BalanceType.AVAILABLE)
                .order(null)
                .build();

        debitEntry = sellerLedgerRepository.save(debitEntry);
        BigDecimal newBalance = seller.getCachedAvailableBalance().subtract(request.getAmount());

        String gatewayRef;
        try {
            gatewayRef = paymentGatewayClient.simulatePayout(request.getAmount());
        } catch (Exception e) {
            throw new CoreThrowHandler(RestApiError.USR_0014);
        }

        return SellerWithdrawalResponse.builder()
                .amount(request.getAmount())
                .newAvailableBalance(newBalance)
                .withdrawalId(debitEntry.getId())
                .mockGatewayRef(gatewayRef)
                .build();
    }
}
