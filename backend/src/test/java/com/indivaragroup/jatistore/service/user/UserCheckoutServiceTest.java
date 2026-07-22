package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.Cart;
import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.data.entity.PaymentCard;
import com.indivaragroup.jatistore.data.entity.Transaction;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import com.indivaragroup.jatistore.data.utility.constant.TransactionStatus;
import com.indivaragroup.jatistore.dto.request.user.CreateOrderRequest;
import com.indivaragroup.jatistore.dto.request.user.PayOrderRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.CreateOrderResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCheckoutResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.CartRepository;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.OrderDetailRepository;
import com.indivaragroup.jatistore.repository.CartItemRepository;
import com.indivaragroup.jatistore.repository.PaymentCardRepository;
import com.indivaragroup.jatistore.repository.TransactionRepository;
import com.indivaragroup.jatistore.service.payment.PaymentGatewayClient;
import com.indivaragroup.jatistore.dto.response.payment.CardChargeResponse;
import com.indivaragroup.jatistore.dto.response.payment.WalletChargeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCheckoutServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private PaymentGatewayClient paymentGatewayClient;

    @Mock
    private CheckoutFinalizer checkoutFinalizer;

    @InjectMocks
    private UserCheckoutService userCheckoutService;

    private User mockUser;
    private Product mockProduct;
    private Store mockStore;
    private Seller mockSeller;
    private Cart mockCart;
    private CartItem mockCartItem;
    private Order mockOrder;
    private OrderDetail mockOrderDetail;
    private Transaction mockTransaction;
    private PaymentCard mockPaymentCard;
    private CreateOrderRequest createOrderRequest;
    private PayOrderRequest walletPayRequest;
    private PayOrderRequest cardPayRequest;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .build();

        mockSeller = new Seller();
        mockSeller.setId(UUID.randomUUID());
        mockSeller.setUser(mockUser);
        mockSeller.setActive(true);

        mockStore = new Store();
        mockStore.setId(UUID.randomUUID());
        mockStore.setStoreName("Test Store");
        mockStore.setSeller(mockSeller);

        mockProduct = new Product();
        mockProduct.setId(UUID.randomUUID());
        mockProduct.setName("Test Product");
        mockProduct.setPrice(BigDecimal.valueOf(100));
        mockProduct.setStock(10);
        mockProduct.setStore(mockStore);

        mockCartItem = new CartItem();
        mockCartItem.setId(UUID.randomUUID());
        mockCartItem.setProduct(mockProduct);
        mockCartItem.setQuantity(2);

        mockCart = new Cart();
        mockCart.setId(UUID.randomUUID());
        mockCart.setUserId(mockUser.getId());

        mockOrderDetail = new OrderDetail();
        mockOrderDetail.setId(UUID.randomUUID());
        mockOrderDetail.setProduct(mockProduct);
        mockOrderDetail.setQuantity(2);
        mockOrderDetail.setPricePerItem(BigDecimal.valueOf(100));
        mockOrderDetail.setFlashSale(false);

        mockOrder = Order.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .totalAmount(BigDecimal.valueOf(200))
                .status(OrderStatus.PENDING)
                .build();
        mockOrder.setOrderDetails(List.of(mockOrderDetail));
        mockOrderDetail.setOrder(mockOrder);

        mockTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .order(mockOrder)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.PENDING)
                .build();

        mockPaymentCard = PaymentCard.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .selectedCartItemIds(List.of(mockCartItem.getId()))
                .build();

        walletPayRequest = PayOrderRequest.builder()
                .paymentMethod(PaymentMethod.WALLET)
                .build();

        cardPayRequest = PayOrderRequest.builder()
                .paymentMethod(PaymentMethod.CARD)
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvc("123")
                .build();
    }

    @Test
    void createOrder_Success() throws CoreThrowHandler {
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(mockCartItem));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(orderDetailRepository.saveAll(anyList())).thenReturn(List.of());

        RestApiResponse<CreateOrderResponse> response = userCheckoutService.createOrder(
                createOrderRequest, "user@example.com");

        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertNotNull(response.getRestApiResponseData());
        assertEquals(mockOrder.getId(), response.getRestApiResponseData().getOrderId());
        assertEquals(BigDecimal.valueOf(200), response.getRestApiResponseData().getTotalAmount());
        assertEquals(OrderStatus.PENDING, response.getRestApiResponseData().getStatus());

        verify(cartItemRepository).selectDistinctStore(any());
        verify(cartItemRepository).findAllById(anyList());
        verify(orderRepository).save(any(Order.class));
        verify(orderDetailRepository).saveAll(anyList());
    }

    @Test
    void createOrder_Fail_MultipleSellerInCart() {
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(2);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.createOrder(createOrderRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0019.getCode(), exception.getCode());
        verify(cartItemRepository).selectDistinctStore(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_Fail_EmptyCart() {
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of());

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.createOrder(createOrderRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0009.getCode(), exception.getCode());
        verify(cartItemRepository).findAllById(anyList());
    }

    @Test
    void createOrder_Fail_InsufficientStock() {
        mockProduct.setStock(1);
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(mockCartItem));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.createOrder(createOrderRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0011.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("Test Product"));
    }

    @Test
    void payOrder_Success_WalletPayment() throws CoreThrowHandler {
        // Mock order with PAID_ON_HOLD status for final read
        Order paidOrder = Order.builder()
                .id(mockOrder.getId())
                .user(mockUser)
                .totalAmount(BigDecimal.valueOf(200))
                .status(OrderStatus.PAID_ON_HOLD)
                .build();

        Transaction successTransaction = Transaction.builder()
                .id(mockTransaction.getId())
                .order(paidOrder)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.SUCCESS)
                .paymentGatewayRef("gateway-ref-123")
                .build();

        when(orderRepository.findById(mockOrder.getId()))
                .thenReturn(Optional.of(mockOrder))
                .thenReturn(Optional.of(paidOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(transactionRepository.findById(mockTransaction.getId())).thenReturn(Optional.of(successTransaction));
        when(paymentGatewayClient.chargeWallet(any())).thenReturn(
                new WalletChargeResponse("success", BigDecimal.valueOf(200), "Payment successful", "gateway-ref-123"));
        when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);
        doNothing().when(checkoutFinalizer).finalizeOrder(any(), anyList());

        RestApiResponse<UserCheckoutResponse> response = userCheckoutService.payOrder(
                mockOrder.getId(), walletPayRequest, "user@example.com");

        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertNotNull(response.getRestApiResponseData());

        verify(paymentGatewayClient).chargeWallet(any());
        verify(checkoutFinalizer).finalizeOrder(any(), anyList());
    }

    @Test
    void payOrder_Success_CardPayment() throws CoreThrowHandler {
        Order paidOrder = Order.builder()
                .id(mockOrder.getId())
                .user(mockUser)
                .totalAmount(BigDecimal.valueOf(200))
                .status(OrderStatus.PAID_ON_HOLD)
                .build();

        Transaction successTransaction = Transaction.builder()
                .id(mockTransaction.getId())
                .order(paidOrder)
                .paymentMethod(PaymentMethod.CARD)
                .status(TransactionStatus.SUCCESS)
                .paymentGatewayRef("gateway-ref-456")
                .paymentCard(mockPaymentCard)
                .build();

        when(orderRepository.findById(mockOrder.getId()))
                .thenReturn(Optional.of(mockOrder))
                .thenReturn(Optional.of(paidOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(paymentCardRepository.findByCardNumber(any())).thenReturn(Optional.of(mockPaymentCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(transactionRepository.findById(mockTransaction.getId())).thenReturn(Optional.of(successTransaction));
        when(paymentGatewayClient.chargeCard(any())).thenReturn(
                new CardChargeResponse("success", BigDecimal.valueOf(200), "Payment successful", "gateway-ref-456", "1111"));
        when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);
        doNothing().when(checkoutFinalizer).finalizeOrder(any(), anyList());

        RestApiResponse<UserCheckoutResponse> response = userCheckoutService.payOrder(
                mockOrder.getId(), cardPayRequest, "user@example.com");

        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());

        verify(paymentGatewayClient).chargeCard(any());
        verify(paymentCardRepository).findByCardNumber(any());
    }

    @Test
    void payOrder_Fail_OrderNotFound() {
        when(orderRepository.findById(any())).thenReturn(Optional.empty());

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    UUID.randomUUID(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0015.getCode(), exception.getCode());
    }

    @Test
    void payOrder_Fail_OrderNotOwnedByUser() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .email("different@example.com")
                .build();
        mockOrder.setUser(differentUser);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0015.getCode(), exception.getCode());
    }

    @Test
    void payOrder_Fail_OrderNotPending() {
        mockOrder.setStatus(OrderStatus.PAID_ON_HOLD);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0022.getCode(), exception.getCode());
    }

    @Test
    void payOrder_Fail_StockChangedAfterOrderCreation() {
        mockProduct.setStock(1);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0011.getCode(), exception.getCode());
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void payOrder_Fail_AmountChangedAfterOrderCreation() {
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(250));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0023.getCode(), exception.getCode());
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void payOrder_Fail_PaymentDeclined_Wallet() {
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(paymentGatewayClient.chargeWallet(any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.PAYMENT_REQUIRED));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0013.getCode(), exception.getCode());
    }

    @Test
    void payOrder_Fail_PaymentDeclined_Card() {
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(paymentCardRepository.findByCardNumber(any())).thenReturn(Optional.of(mockPaymentCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(paymentGatewayClient.chargeCard(any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.PAYMENT_REQUIRED));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), cardPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0013.getCode(), exception.getCode());
    }

    @Test
    void payOrder_Fail_PaymentTimeout() {
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(paymentGatewayClient.chargeWallet(any())).thenThrow(new ResourceAccessException("Timeout"));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0017.getCode(), exception.getCode());
    }

    @Test
    void payOrder_Fail_PaymentGatewayError() {
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(paymentGatewayClient.chargeWallet(any())).thenThrow(new RestClientException("Gateway error"));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0014.getCode(), exception.getCode());
    }
}
