package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderConfirmReceiptResponse;
import com.indivaragroup.jatistore.dto.response.module.user.OrderHistoryItemResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.SellerLedgerRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserOrderServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SellerLedgerRepository sellerLedgerRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UserOrderService userOrderService;

    private User mockUser;
    private Order mockOrder;
    private Seller mockSeller;
    private UUID orderId;
    private UUID userId;
    private UUID sellerId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        sellerId = UUID.randomUUID();

        mockUser = User.builder()
                .id(userId)
                .email("user@example.com")
                .build();

        mockSeller = new Seller();
        mockSeller.setId(sellerId);

        mockOrder = Order.builder()
                .id(orderId)
                .user(mockUser)
                .totalAmount(BigDecimal.valueOf(200))
                .status(OrderStatus.SHIPPED)
                .build();
    }

    @Test
    void getOrderHistory_Success() throws CoreThrowHandler {
        Pageable pageable = PageRequest.of(0, 20);
        Instant now = Instant.now();

        Object[] orderRow = new Object[] {
                orderId.toString(),
                now,
                BigDecimal.valueOf(200),
                "SHIPPED"
        };

        Object[] itemRow = new Object[] {
                "Test Product",
                2,
                BigDecimal.valueOf(100),
                false
        };

        List<Object[]> orderList = new java.util.ArrayList<>();
        orderList.add(orderRow);
        Page<Object[]> orderPage = new PageImpl<>(orderList, pageable, 1);

        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findOrderHistoryByUserId(eq(userId), eq(null), eq(pageable)))
                .thenReturn(orderPage);
        List<Object[]> itemList = new java.util.ArrayList<>();
        itemList.add(itemRow);
        when(orderRepository.findOrderItemsByOrderId(eq(orderId)))
                .thenReturn(itemList);

        RestApiResponse<Page<OrderHistoryItemResponse>> response =
                userOrderService.getOrderHistory("user@example.com", null, pageable);

        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertEquals("SUCCESS", response.getRestApiResponseHttpStatus());
        assertNotNull(response.getRestApiResponseData());
        assertEquals(1, response.getRestApiResponseData().getTotalElements());

        OrderHistoryItemResponse item = response.getRestApiResponseData().getContent().get(0);
        assertEquals(orderId, item.getOrderId());
        assertEquals(BigDecimal.valueOf(200), item.getTotalAmount());
        assertEquals(OrderStatus.SHIPPED, item.getStatus());
        assertEquals(1, item.getItems().size());
        assertEquals("Test Product", item.getItems().get(0).getProductName());

        verify(authRepository).findByEmail("user@example.com");
        verify(orderRepository).findOrderHistoryByUserId(eq(userId), eq(null), eq(pageable));
        verify(orderRepository).findOrderItemsByOrderId(eq(orderId));
    }

    @Test
    void getOrderHistory_WithStatusFilter_Success() throws CoreThrowHandler {
        Pageable pageable = PageRequest.of(0, 20);
        List<Object[]> emptyList = List.of();
        Page<Object[]> emptyPage = new PageImpl<>(emptyList, pageable, 0);

        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findOrderHistoryByUserId(eq(userId), eq("RECEIVED"), eq(pageable)))
                .thenReturn(emptyPage);

        RestApiResponse<Page<OrderHistoryItemResponse>> response =
                userOrderService.getOrderHistory("user@example.com", OrderStatus.RECEIVED, pageable);

        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertEquals(0, response.getRestApiResponseData().getTotalElements());

        verify(orderRepository).findOrderHistoryByUserId(eq(userId), eq("RECEIVED"), eq(pageable));
    }

    @Test
    void getOrderHistory_UserNotFound_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 20);

        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userOrderService.getOrderHistory("user@example.com", null, pageable);
        });

        assertEquals(RestApiError.GEN_0005.getCode(), exception.getCode());
        verify(authRepository).findByEmail("user@example.com");
        verify(orderRepository, never()).findOrderHistoryByUserId(any(), any(), any());
    }

    @Test
    void confirmReceipt_Success() throws CoreThrowHandler {
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.findSellerIdByOrderId(orderId)).thenReturn(sellerId);
        when(entityManager.getReference(Seller.class, sellerId)).thenReturn(mockSeller);

        RestApiResponse<OrderConfirmReceiptResponse> response =
                userOrderService.confirmReceipt("user@example.com", orderId);

        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertEquals("SUCCESS", response.getRestApiResponseHttpStatus());
        assertEquals(orderId, response.getRestApiResponseData().getOrderId());
        assertEquals(OrderStatus.RECEIVED, response.getRestApiResponseData().getOrderStatus());

        verify(sellerLedgerRepository, times(2)).save(any());
        verify(orderRepository).save(mockOrder);
        assertEquals(OrderStatus.RECEIVED, mockOrder.getStatus());

        ArgumentCaptor<com.indivaragroup.jatistore.data.entity.SellerLedger> ledgerCaptor =
                ArgumentCaptor.forClass(com.indivaragroup.jatistore.data.entity.SellerLedger.class);
        verify(sellerLedgerRepository, times(2)).save(ledgerCaptor.capture());

        List<com.indivaragroup.jatistore.data.entity.SellerLedger> savedLedgers = ledgerCaptor.getAllValues();
        assertEquals(BalanceType.ON_HOLD, savedLedgers.get(0).getBalanceType());
        assertEquals(BigDecimal.valueOf(200).negate(), savedLedgers.get(0).getAmount());
        assertEquals(BalanceType.AVAILABLE, savedLedgers.get(1).getBalanceType());
        assertEquals(BigDecimal.valueOf(200), savedLedgers.get(1).getAmount());
    }

    @Test
    void confirmReceipt_OrderNotFound_ThrowsException() {
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userOrderService.confirmReceipt("user@example.com", orderId);
        });

        assertEquals(RestApiError.USR_0015.getCode(), exception.getCode());
        verify(orderRepository).findById(orderId);
        verify(sellerLedgerRepository, never()).save(any());
    }

    @Test
    void confirmReceipt_OrderNotBelongToUser_ThrowsException() {
        User anotherUser = User.builder()
                .id(UUID.randomUUID())
                .email("another@example.com")
                .build();

        mockOrder.setUser(anotherUser);

        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userOrderService.confirmReceipt("user@example.com", orderId);
        });

        assertEquals(RestApiError.USR_0015.getCode(), exception.getCode());
        verify(sellerLedgerRepository, never()).save(any());
    }

    @Test
    void confirmReceipt_OrderNotShipped_ThrowsException() {
        mockOrder.setStatus(OrderStatus.PENDING);

        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userOrderService.confirmReceipt("user@example.com", orderId);
        });

        assertEquals(RestApiError.USR_0016.getCode(), exception.getCode());
        verify(sellerLedgerRepository, never()).save(any());
    }

    @Test
    void confirmReceipt_ReleaseFundsToSeller() throws CoreThrowHandler {
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.findSellerIdByOrderId(orderId)).thenReturn(sellerId);
        when(entityManager.getReference(Seller.class, sellerId)).thenReturn(mockSeller);

        userOrderService.confirmReceipt("user@example.com", orderId);

        ArgumentCaptor<com.indivaragroup.jatistore.data.entity.SellerLedger> ledgerCaptor =
                ArgumentCaptor.forClass(com.indivaragroup.jatistore.data.entity.SellerLedger.class);
        verify(sellerLedgerRepository, times(2)).save(ledgerCaptor.capture());

        List<com.indivaragroup.jatistore.data.entity.SellerLedger> ledgers = ledgerCaptor.getAllValues();

        com.indivaragroup.jatistore.data.entity.SellerLedger availableLedger = ledgers.stream()
                .filter(l -> l.getBalanceType() == BalanceType.AVAILABLE)
                .findFirst()
                .orElseThrow();

        com.indivaragroup.jatistore.data.entity.SellerLedger onHoldLedger = ledgers.stream()
                .filter(l -> l.getBalanceType() == BalanceType.ON_HOLD)
                .findFirst()
                .orElseThrow();

        assertEquals(mockSeller, availableLedger.getSeller());
        assertEquals(mockOrder, availableLedger.getOrder());
        assertEquals(mockOrder.getTotalAmount(), availableLedger.getAmount());

        assertEquals(mockSeller, onHoldLedger.getSeller());
        assertEquals(mockOrder, onHoldLedger.getOrder());
        assertEquals(mockOrder.getTotalAmount().negate(), onHoldLedger.getAmount());
    }
}
