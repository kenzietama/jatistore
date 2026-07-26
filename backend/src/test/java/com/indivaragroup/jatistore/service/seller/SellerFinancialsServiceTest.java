package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.SellerLedger;
import com.indivaragroup.jatistore.dto.request.seller.SellerWithdrawalRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerBalanceSummaryResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerFinancialDashboardResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerLedgerTransactionResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.financials.SellerWithdrawalResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.SellerLedgerRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import com.indivaragroup.jatistore.service.payment.PaymentGatewayClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerFinancialsServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private SellerLedgerRepository sellerLedgerRepository;

    @Mock
    private PaymentGatewayClient paymentGatewayClient;

    @InjectMocks
    private SellerFinancialsService sellerFinancialsService;

    private UUID sellerId;
    private Seller mockSeller;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        mockSeller = new Seller();
        mockSeller.setId(sellerId);
        mockSeller.setCachedAvailableBalance(new BigDecimal("1000"));
        mockSeller.setCachedOnHoldBalance(new BigDecimal("500"));
    }

    @Test
    void getDashboard_shouldReturnDashboard() throws CoreThrowHandler {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        
        SellerLedger ledger = new SellerLedger();
        ledger.setId(UUID.randomUUID());
        ledger.setAmount(new BigDecimal("100"));
        ledger.setBalanceType(BalanceType.AVAILABLE);
        Page<SellerLedger> page = new PageImpl<>(List.of(ledger));
        when(sellerLedgerRepository.findBySellerIdOrderByCreatedAtDescAmountDesc(eq(sellerId), any(Pageable.class)))
                .thenReturn(page);

        SellerFinancialDashboardResponse response = sellerFinancialsService.getDashboard(sellerId);

        assertNotNull(response);
        assertEquals(new BigDecimal("1000"), response.getAvailableBalance());
        assertEquals(new BigDecimal("500"), response.getOnHoldBalance());
        assertEquals(new BigDecimal("1500"), response.getTotalEarnings());
        assertEquals(1, response.getRecentTransactions().size());
    }

    @Test
    void getDashboard_sellerNotFound_shouldThrow() {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> sellerFinancialsService.getDashboard(sellerId));
    }

    @Test
    void getBalanceSummary_shouldReturnSummary() throws CoreThrowHandler {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));

        SellerBalanceSummaryResponse response = sellerFinancialsService.getBalanceSummary(sellerId);

        assertNotNull(response);
        assertEquals(new BigDecimal("1000"), response.getAvailableBalance());
        assertEquals(new BigDecimal("500"), response.getOnHoldBalance());
        assertEquals(new BigDecimal("1500"), response.getTotalEarnings());
    }

    @Test
    void getTransactionHistory_shouldReturnPage() throws CoreThrowHandler {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));

        SellerLedger ledger = new SellerLedger();
        ledger.setId(UUID.randomUUID());
        ledger.setAmount(new BigDecimal("100"));
        ledger.setBalanceType(BalanceType.AVAILABLE);
        Page<SellerLedger> page = new PageImpl<>(List.of(ledger));
        when(sellerLedgerRepository.findTransactionsBySellerId(eq(sellerId), eq("ALL"), eq(""), any(Pageable.class)))
                .thenReturn(page);

        Page<SellerLedgerTransactionResponse> response = sellerFinancialsService.getTransactionHistory(sellerId, "ALL", null, 0, 10, "amount_desc");

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }
    
    @Test
    void getTransactionHistory_sortAmountAsc_shouldReturnPage() throws CoreThrowHandler {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        Page<SellerLedger> page = new PageImpl<>(List.of());
        when(sellerLedgerRepository.findTransactionsBySellerId(eq(sellerId), eq("ALL"), eq("test"), any(Pageable.class)))
                .thenReturn(page);

        Page<SellerLedgerTransactionResponse> response = sellerFinancialsService.getTransactionHistory(sellerId, "ALL", "test", 0, 10, "amount_asc");
        assertNotNull(response);
    }
    
    @Test
    void getTransactionHistory_sortDateAsc_shouldReturnPage() throws CoreThrowHandler {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        Page<SellerLedger> page = new PageImpl<>(List.of());
        when(sellerLedgerRepository.findTransactionsBySellerId(eq(sellerId), eq("ALL"), eq("test"), any(Pageable.class)))
                .thenReturn(page);

        Page<SellerLedgerTransactionResponse> response = sellerFinancialsService.getTransactionHistory(sellerId, "ALL", "test", 0, 10, "date_asc");
        assertNotNull(response);
    }
    
    @Test
    void getTransactionHistory_sortDefault_shouldReturnPage() throws CoreThrowHandler {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        Page<SellerLedger> page = new PageImpl<>(List.of());
        when(sellerLedgerRepository.findTransactionsBySellerId(eq(sellerId), eq("ALL"), eq("test"), any(Pageable.class)))
                .thenReturn(page);

        Page<SellerLedgerTransactionResponse> response = sellerFinancialsService.getTransactionHistory(sellerId, "ALL", "test", 0, 10, "default");
        assertNotNull(response);
    }

    @Test
    void simulateWithdrawal_shouldReturnSuccess() throws Exception {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(sellerLedgerRepository.save(any(SellerLedger.class))).thenAnswer(i -> {
            SellerLedger sl = i.getArgument(0);
            sl.setId(UUID.randomUUID());
            return sl;
        });
        when(paymentGatewayClient.simulatePayout(any())).thenReturn("GATEWAY_REF_123");

        SellerWithdrawalRequest request = new SellerWithdrawalRequest();
        request.setAmount(new BigDecimal("500"));

        SellerWithdrawalResponse response = sellerFinancialsService.simulateWithdrawal(sellerId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("500"), response.getAmount());
        assertEquals(new BigDecimal("500"), response.getNewAvailableBalance()); // 1000 - 500
        assertEquals("GATEWAY_REF_123", response.getMockGatewayRef());
        assertNotNull(response.getWithdrawalId());
    }

    @Test
    void simulateWithdrawal_nullRequest_shouldThrow() {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        assertThrows(CoreThrowHandler.class, () -> sellerFinancialsService.simulateWithdrawal(sellerId, null));
    }

    @Test
    void simulateWithdrawal_zeroAmount_shouldThrow() {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        SellerWithdrawalRequest request = new SellerWithdrawalRequest();
        request.setAmount(BigDecimal.ZERO);
        assertThrows(CoreThrowHandler.class, () -> sellerFinancialsService.simulateWithdrawal(sellerId, request));
    }

    @Test
    void simulateWithdrawal_insufficientBalance_shouldThrow() {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        SellerWithdrawalRequest request = new SellerWithdrawalRequest();
        request.setAmount(new BigDecimal("2000")); // available is 1000
        assertThrows(CoreThrowHandler.class, () -> sellerFinancialsService.simulateWithdrawal(sellerId, request));
    }

    @Test
    void simulateWithdrawal_exceedsMaxLimit_shouldThrow() {
        mockSeller.setCachedAvailableBalance(new BigDecimal("2000000000"));
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        SellerWithdrawalRequest request = new SellerWithdrawalRequest();
        request.setAmount(new BigDecimal("1500000000")); // max is 1000000000
        assertThrows(CoreThrowHandler.class, () -> sellerFinancialsService.simulateWithdrawal(sellerId, request));
    }

    @Test
    void simulateWithdrawal_gatewayFails_shouldThrow() throws Exception {
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(sellerLedgerRepository.save(any(SellerLedger.class))).thenAnswer(i -> {
            SellerLedger sl = i.getArgument(0);
            sl.setId(UUID.randomUUID());
            return sl;
        });
        when(paymentGatewayClient.simulatePayout(any())).thenThrow(new RuntimeException("Gateway error"));

        SellerWithdrawalRequest request = new SellerWithdrawalRequest();
        request.setAmount(new BigDecimal("500"));

        assertThrows(CoreThrowHandler.class, () -> sellerFinancialsService.simulateWithdrawal(sellerId, request));
    }
}
