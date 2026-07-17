package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.checkout.CartItem;
import com.indivaragroup.jatistore.data.entity.checkout.PaymentCard;
import com.indivaragroup.jatistore.data.entity.checkout.Transaction;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import com.indivaragroup.jatistore.data.utility.constant.TransactionStatus;
import com.indivaragroup.jatistore.dto.request.user.UserCheckoutRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCheckoutResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.checkout.CartItemRepository;
import com.indivaragroup.jatistore.repository.checkout.PaymentCardRepository;
import com.indivaragroup.jatistore.repository.checkout.TransactionRepository;
import com.indivaragroup.jatistore.service.payment.PaymentGatewayClient;
import com.indivaragroup.jatistore.dto.response.payment.CardChargeResponse;
import com.indivaragroup.jatistore.dto.response.payment.WalletChargeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
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
    private OrderRepository orderRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

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
    private CartItem mockCartItem;
    private Order mockOrder;
    private Transaction mockTransaction;
    private PaymentCard mockPaymentCard;
    private UserCheckoutRequest walletCheckoutRequest;
    private UserCheckoutRequest cardCheckoutRequest;

    @BeforeEach
    void setUp() {
        // Mock User
        mockUser = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .build();

        // Mock Seller
        mockSeller = new Seller();
        mockSeller.setId(UUID.randomUUID());
        mockSeller.setUser(mockUser);
        mockSeller.setActive(true);

        // Mock Store
        mockStore = new Store();
        mockStore.setId(UUID.randomUUID());
        mockStore.setStoreName("Test Store");
        mockStore.setSeller(mockSeller);

        // Mock Product
        mockProduct = new Product();
        mockProduct.setId(UUID.randomUUID());
        mockProduct.setName("Test Product");
        mockProduct.setPrice(BigDecimal.valueOf(100));
        mockProduct.setStock(10);
        mockProduct.setStore(mockStore);

        // Mock CartItem
        mockCartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .product(mockProduct)
                .quantity(2)
                .build();

        // Mock Order
        mockOrder = Order.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .totalAmount(BigDecimal.valueOf(200))
                .status(OrderStatus.PENDING)
                .build();

        // Mock Transaction
        mockTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .order(mockOrder)
                .paymentMethod(PaymentMethod.WALLET)
                .status(TransactionStatus.PENDING)
                .build();

        // Mock PaymentCard
        mockPaymentCard = PaymentCard.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .build();

        // Wallet checkout request
        walletCheckoutRequest = new UserCheckoutRequest();
        walletCheckoutRequest.setUserSelectedCartItemId(new UUID[]{mockCartItem.getId()});
        walletCheckoutRequest.setUserCheckoutRequestPaymentMethod(PaymentMethod.WALLET);

        // Card checkout request
        cardCheckoutRequest = new UserCheckoutRequest();
        cardCheckoutRequest.setUserSelectedCartItemId(new UUID[]{mockCartItem.getId()});
        cardCheckoutRequest.setUserCheckoutRequestPaymentMethod(PaymentMethod.CARD);
        cardCheckoutRequest.setUserCheckoutRequestCardNumber("4111111111111111");
        cardCheckoutRequest.setUserCheckoutRequestCardHolderName("John Doe");
        cardCheckoutRequest.setUserCheckoutRequestExpiryDate("12/25");
        cardCheckoutRequest.setUserCheckoutRequestCvc("123");
    }

    @Test
    void checkout_Success_WalletPayment() throws CoreThrowHandler {
        // Arrange
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(mockCartItem));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(paymentGatewayClient.chargeWallet(any())).thenReturn(new WalletChargeResponse("success", BigDecimal.valueOf(200), "Payment successful", "gateway-ref-123"));
        when(orderRepository.findById(any())).thenReturn(Optional.of(mockOrder));
        when(transactionRepository.findById(any())).thenReturn(Optional.of(mockTransaction));
        doNothing().when(checkoutFinalizer).finalizeOrder(any(), anyList());

        // Act
        RestApiResponse<UserCheckoutResponse> response = userCheckoutService.checkout(walletCheckoutRequest, "user@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertNotNull(response.getRestApiResponseData());
        assertEquals(mockOrder.getId(), response.getRestApiResponseData().getOrder_id());
        assertEquals(mockTransaction.getId(), response.getRestApiResponseData().getTransaction_id());

        verify(cartItemRepository).selectDistinctStore(any());
        verify(cartItemRepository).findAllById(anyList());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
        verify(paymentGatewayClient).chargeWallet(any());
        verify(checkoutFinalizer).finalizeOrder(any(), anyList());
    }

    @Test
    void checkout_Success_CardPayment() throws CoreThrowHandler {
        // Arrange
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(mockCartItem));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(paymentCardRepository.findByCardNumber(any())).thenReturn(Optional.of(mockPaymentCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(paymentGatewayClient.chargeCard(any())).thenReturn(new CardChargeResponse("success", BigDecimal.valueOf(200), "Payment successful", "gateway-ref-456", "1111"));
        when(orderRepository.findById(any())).thenReturn(Optional.of(mockOrder));
        when(transactionRepository.findById(any())).thenReturn(Optional.of(mockTransaction));
        doNothing().when(checkoutFinalizer).finalizeOrder(any(), anyList());

        // Act
        RestApiResponse<UserCheckoutResponse> response = userCheckoutService.checkout(cardCheckoutRequest, "user@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertNotNull(response.getRestApiResponseData());

        verify(paymentGatewayClient).chargeCard(any());
        verify(paymentCardRepository).findByCardNumber(any());
    }

    @Test
    void checkout_Fail_MultipleSellerInCart() {
        // Arrange
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(2);

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.checkout(walletCheckoutRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0019.getCode(), exception.getCode());
        verify(cartItemRepository).selectDistinctStore(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkout_Fail_EmptyCart() {
        // Arrange
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of());

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.checkout(walletCheckoutRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0009.getCode(), exception.getCode());
        verify(cartItemRepository).findAllById(anyList());
    }

    @Test
    void checkout_Fail_InsufficientStock() {
        // Arrange
        mockProduct.setStock(1); // Cart has quantity 2, but stock is 1
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(mockCartItem));

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.checkout(walletCheckoutRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0011.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("Test Product"));
    }

    @Test
    void checkout_Fail_PaymentDeclined_Wallet() {
        // Arrange
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(mockCartItem));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(paymentGatewayClient.chargeWallet(any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.PAYMENT_REQUIRED));
        when(orderRepository.findById(any())).thenReturn(Optional.of(mockOrder));
        when(transactionRepository.findById(any())).thenReturn(Optional.of(mockTransaction));

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.checkout(walletCheckoutRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0012.getCode(), exception.getCode());
        verify(paymentGatewayClient).chargeWallet(any());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    @Test
    void checkout_Fail_PaymentDeclined_Card() {
        // Arrange
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(mockCartItem));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartItemRepository.calculateTotalAmount(any())).thenReturn(BigDecimal.valueOf(200));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(paymentCardRepository.findByCardNumber(any())).thenReturn(Optional.of(mockPaymentCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(paymentGatewayClient.chargeCard(any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.PAYMENT_REQUIRED));
        when(orderRepository.findById(any())).thenReturn(Optional.of(mockOrder));
        when(transactionRepository.findById(any())).thenReturn(Optional.of(mockTransaction));

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.checkout(cardCheckoutRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0013.getCode(), exception.getCode());
        verify(paymentGatewayClient).chargeCard(any());
    }
}
