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
import com.indivaragroup.jatistore.data.entity.FlashSaleItem;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.CartRepository;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.OrderDetailRepository;
import com.indivaragroup.jatistore.repository.CartItemRepository;
import com.indivaragroup.jatistore.repository.PaymentCardRepository;
import com.indivaragroup.jatistore.repository.TransactionRepository;
import com.indivaragroup.jatistore.repository.FlashSaleItemRepository;
import com.indivaragroup.jatistore.repository.projection.CheckoutPriceProjection;
import com.indivaragroup.jatistore.service.payment.PaymentGatewayClient;
import com.indivaragroup.jatistore.dto.response.payment.CardChargeResponse;
import com.indivaragroup.jatistore.dto.response.payment.WalletChargeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private FlashSaleItemRepository flashSaleItemRepository;

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

    private CheckoutPriceProjection createMockProjection(UUID cartItemId, UUID productId, Integer quantity, BigDecimal price, Boolean isFlashSale) {
        return new CheckoutPriceProjection() {
            @Override public UUID getCartItemId() { return cartItemId; }
            @Override public UUID getProductId() { return productId; }
            @Override public Integer getQuantity() { return quantity; }
            @Override public BigDecimal getEffectivePrice() { return price; }
            @Override public Boolean getFlashSale() { return isFlashSale; }
        };
    }

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
        CheckoutPriceProjection proj = createMockProjection(mockCartItem.getId(), mockProduct.getId(), 2, BigDecimal.valueOf(100), false);
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(mockCartItem));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
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
    }

    @Test
    void createOrder_WithActiveFlashSale_StoresFlashPriceAndFlag() {
        // Setup
        UUID cartItemId = UUID.randomUUID();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .selectedCartItemIds(List.of(cartItemId))
                .build();

        User user = User.builder().id(UUID.randomUUID()).email("buyer@test.com").build();

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);

        Store store = new Store();
        Seller seller = new Seller();
        seller.setActive(true);
        store.setSeller(seller);
        product.setStore(store);

        Cart cart = new Cart();
        cart.setUserId(user.getId());

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        CheckoutPriceProjection projection = createMockProjection(cartItemId, product.getId(), 2, new BigDecimal("80.00"), true);
        FlashSaleItem flashSaleItem = new FlashSaleItem();
        flashSaleItem.setRemainingQuota(100);

        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(cartItem));
        when(authRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(projection));
        when(flashSaleItemRepository.findByProductAndActiveFlashSale(product.getId())).thenReturn(Optional.of(flashSaleItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderDetailRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // Execute
        RestApiResponse<CreateOrderResponse> response = userCheckoutService.createOrder(request, "buyer@test.com");

        // Verify
        ArgumentCaptor<List> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderDetailRepository).saveAll((List<OrderDetail>) detailsCaptor.capture());

        List<OrderDetail> savedDetails = detailsCaptor.getValue();
        assertThat(savedDetails).hasSize(1);
        OrderDetail detail = savedDetails.get(0);
        assertThat(detail.getPricePerItem()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(detail.getFlashSale()).isTrue();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("160.00"));
    }

    @Test
    void createOrder_QuotaExhausted_ThrowsUSR_0025() {
        UUID cartItemId = UUID.randomUUID();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .selectedCartItemIds(List.of(cartItemId))
                .build();

        User user = User.builder().id(UUID.randomUUID()).email("buyer@test.com").build();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        Store store = new Store();
        Seller seller = new Seller();
        seller.setActive(true);
        store.setSeller(seller);
        product.setStore(store);

        Cart cart = new Cart();
        cart.setUserId(user.getId());

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(5);

        CheckoutPriceProjection projection = createMockProjection(cartItemId, product.getId(), 5, new BigDecimal("80.00"), true);
        FlashSaleItem flashSaleItem = new FlashSaleItem();
        flashSaleItem.setRemainingQuota(2); // Only 2 remaining, but requested 5

        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(cartItem));
        when(authRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(projection));
        when(flashSaleItemRepository.findByProductAndActiveFlashSale(product.getId())).thenReturn(Optional.of(flashSaleItem));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.createOrder(request, "buyer@test.com");
        });

        assertThat(exception.getRestApiError()).isEqualTo(RestApiError.USR_0025);
    }

    @Test
    void createOrder_SoftDeletedProduct_ThrowsUSR_0001() {
        UUID cartItemId = UUID.randomUUID();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .selectedCartItemIds(List.of(cartItemId))
                .build();

        User user = User.builder().id(UUID.randomUUID()).email("buyer@test.com").build();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setDeletedAt(java.time.Instant.now()); // Soft-deleted

        Cart cart = new Cart();
        cart.setUserId(user.getId());

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(cartItem));
        when(authRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.createOrder(request, "buyer@test.com");
        });

        assertThat(exception.getRestApiError()).isEqualTo(RestApiError.USR_0001);
    }

    @Test
    void createOrder_MultipleStores_ThrowsUSR_0019() {
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(2);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.createOrder(createOrderRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0019.getCode(), exception.getCode());
    }

    @Test
    void createOrder_InsufficientStock_ThrowsUSR_0011() {
        mockCartItem.setQuantity(20); // Exceeds stock of 10
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(mockCartItem));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.createOrder(createOrderRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0011.getCode(), exception.getCode());
    }

    @Test
    void payOrder_Success_WalletPayment() throws CoreThrowHandler {
        CheckoutPriceProjection proj = createMockProjection(mockCartItem.getId(), mockProduct.getId(), 2, BigDecimal.valueOf(100), false);
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
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
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
        CheckoutPriceProjection proj = createMockProjection(mockCartItem.getId(), mockProduct.getId(), 2, BigDecimal.valueOf(100), false);
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
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
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
        assertNotNull(response.getRestApiResponseData());

        verify(paymentGatewayClient).chargeCard(any());
        verify(checkoutFinalizer).finalizeOrder(any(), anyList());
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
    void payOrder_Fail_NotOrderOwner() {
        User otherUser = User.builder().id(UUID.randomUUID()).email("other@example.com").build();
        mockOrder.setUser(otherUser);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0015.getCode(), exception.getCode());
    }

    @Test
    void payOrder_Fail_NotPendingStatus() {
        mockOrder.setStatus(OrderStatus.PAID_ON_HOLD);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0022.getCode(), exception.getCode());
    }

    @Test
    void payOrder_Fail_InsufficientStock() {
        mockProduct.setStock(1); // Less than order quantity of 2
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
        CheckoutPriceProjection proj = createMockProjection(mockCartItem.getId(), mockProduct.getId(), 2, BigDecimal.valueOf(125), false);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0023.getCode(), exception.getCode());
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void payOrder_FlashSaleExpiresBetweenCreateAndPay_CancelsWithUSR_0023() {
        // Setup
        UUID orderId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        PayOrderRequest payRequest = new PayOrderRequest();
        payRequest.setPaymentMethod(PaymentMethod.WALLET);

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("160.00")); // 2 * 80 flash price
        order.setStatus(OrderStatus.PENDING);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("buyer@test.com");
        order.setUser(user);

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);

        Store store = new Store();
        Seller seller = new Seller();
        seller.setActive(true);
        store.setSeller(seller);
        product.setStore(store);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setProduct(product);
        orderDetail.setQuantity(2);
        orderDetail.setPricePerItem(new BigDecimal("80.00"));
        orderDetail.setFlashSale(true);
        order.setOrderDetails(List.of(orderDetail));

        Cart cart = new Cart();
        cart.setUserId(user.getId());

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        // Flash sale expired - projection returns base price
        CheckoutPriceProjection expiredProjection = createMockProjection(cartItemId, product.getId(), 2, new BigDecimal("100.00"), false);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(authRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(cartItem));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(expiredProjection));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Execute & Verify
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(orderId, payRequest, "buyer@test.com");
        });

        assertThat(exception.getRestApiError()).isEqualTo(RestApiError.USR_0023);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void payOrder_QuotaExhaustedBeforePayment_CancelsWithUSR_0025() {
        UUID orderId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        PayOrderRequest payRequest = new PayOrderRequest();
        payRequest.setPaymentMethod(PaymentMethod.WALLET);

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("160.00"));
        order.setStatus(OrderStatus.PENDING);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("buyer@test.com");
        order.setUser(user);

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);

        Store store = new Store();
        Seller seller = new Seller();
        seller.setActive(true);
        store.setSeller(seller);
        product.setStore(store);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setProduct(product);
        orderDetail.setQuantity(2);
        orderDetail.setPricePerItem(new BigDecimal("80.00"));
        orderDetail.setFlashSale(true);
        order.setOrderDetails(List.of(orderDetail));

        Cart cart = new Cart();
        cart.setUserId(user.getId());

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        CheckoutPriceProjection proj = createMockProjection(cartItemId, product.getId(), 2, new BigDecimal("80.00"), true);
        FlashSaleItem flashSaleItem = new FlashSaleItem();
        flashSaleItem.setRemainingQuota(1); // Only 1 remaining, but order needs 2

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(authRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(cartItem));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
        when(flashSaleItemRepository.findByProductAndActiveFlashSale(product.getId())).thenReturn(Optional.of(flashSaleItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(orderId, payRequest, "buyer@test.com");
        });

        assertThat(exception.getRestApiError()).isEqualTo(RestApiError.USR_0025);
        verify(paymentGatewayClient, never()).chargeWallet(any());
        verify(paymentGatewayClient, never()).chargeCard(any());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void payOrder_SoftDeletedProductBeforePayment_CancelsWithUSR_0001() {
        UUID orderId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        PayOrderRequest payRequest = new PayOrderRequest();
        payRequest.setPaymentMethod(PaymentMethod.WALLET);

        Order order = new Order();
        order.setId(orderId);
        order.setTotalAmount(new BigDecimal("160.00"));
        order.setStatus(OrderStatus.PENDING);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("buyer@test.com");
        order.setUser(user);

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setDeletedAt(java.time.Instant.now()); // Soft-deleted

        Store store = new Store();
        Seller seller = new Seller();
        seller.setActive(true);
        store.setSeller(seller);
        product.setStore(store);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setProduct(product);
        orderDetail.setQuantity(2);
        orderDetail.setPricePerItem(new BigDecimal("80.00"));
        order.setOrderDetails(List.of(orderDetail));

        Cart cart = new Cart();
        cart.setUserId(user.getId());

        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(authRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(orderId, payRequest, "buyer@test.com");
        });

        assertThat(exception.getRestApiError()).isEqualTo(RestApiError.USR_0001);
        verify(paymentGatewayClient, never()).chargeWallet(any());
        verify(paymentGatewayClient, never()).chargeCard(any());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void payOrder_Fail_PaymentDeclined_Wallet() {
        CheckoutPriceProjection proj = createMockProjection(mockCartItem.getId(), mockProduct.getId(), 2, BigDecimal.valueOf(100), false);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(paymentGatewayClient.chargeWallet(any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.PAYMENT_REQUIRED));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0013.getCode(), exception.getCode());
        verify(transactionRepository, atLeastOnce()).save(argThat(tx -> tx.getStatus() == TransactionStatus.DECLINED));
        verify(orderRepository, atLeastOnce()).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void payOrder_Fail_PaymentDeclined_Card() {
        CheckoutPriceProjection proj = createMockProjection(mockCartItem.getId(), mockProduct.getId(), 2, BigDecimal.valueOf(100), false);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
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
        verify(transactionRepository, atLeastOnce()).save(argThat(tx -> tx.getStatus() == TransactionStatus.DECLINED));
        verify(orderRepository, atLeastOnce()).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void payOrder_Fail_PaymentTimeout() {
        CheckoutPriceProjection proj = createMockProjection(mockCartItem.getId(), mockProduct.getId(), 2, BigDecimal.valueOf(100), false);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(paymentGatewayClient.chargeWallet(any())).thenThrow(new ResourceAccessException("Timeout"));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0017.getCode(), exception.getCode());
        verify(transactionRepository, atLeastOnce()).save(argThat(tx -> tx.getStatus() == TransactionStatus.FAILED));
        verify(orderRepository, atLeastOnce()).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void payOrder_Fail_PaymentGatewayError() {
        CheckoutPriceProjection proj = createMockProjection(mockCartItem.getId(), mockProduct.getId(), 2, BigDecimal.valueOf(100), false);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(paymentGatewayClient.chargeWallet(any())).thenThrow(new RestClientException("Gateway error"));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(
                    mockOrder.getId(), walletPayRequest, "user@example.com");
        });

        assertEquals(RestApiError.USR_0014.getCode(), exception.getCode());
        verify(transactionRepository, atLeastOnce()).save(argThat(tx -> tx.getStatus() == TransactionStatus.FAILED));
        verify(orderRepository, atLeastOnce()).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void createOrder_EmptyCartItems_ThrowsUSR_0009() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .selectedCartItemIds(List.of(UUID.randomUUID()))
                .build();
        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of());
        
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.createOrder(request, "user@example.com");
        });
        assertEquals(RestApiError.USR_0009.getCode(), exception.getCode());
    }

    @Test
    void createOrder_InactiveSeller_ThrowsUSR_0001() {
        UUID cartItemId = UUID.randomUUID();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .selectedCartItemIds(List.of(cartItemId))
                .build();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("100.00"));
        Store store = new Store();
        Seller seller = new Seller();
        seller.setActive(false);
        store.setSeller(seller);
        product.setStore(store);
        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setProduct(product);

        when(cartItemRepository.selectDistinctStore(any())).thenReturn(1);
        when(cartItemRepository.findAllById(anyList())).thenReturn(List.of(cartItem));
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.createOrder(request, "user@example.com");
        });
        assertEquals(RestApiError.USR_0001.getCode(), exception.getCode());
    }

    @Test
    void payOrder_EmptyCartItems_ThrowsUSR_0009() {
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(mockOrder.getId(), walletPayRequest, "user@example.com");
        });
        assertEquals(RestApiError.USR_0009.getCode(), exception.getCode());
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void payOrder_InactiveSeller_ThrowsUSR_0001() {
        mockSeller.setActive(false);
        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            userCheckoutService.payOrder(mockOrder.getId(), walletPayRequest, "user@example.com");
        });
        assertEquals(RestApiError.USR_0001.getCode(), exception.getCode());
        verify(orderRepository).save(argThat(order -> order.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void payOrder_CardPayment_NewCardCreated() throws CoreThrowHandler {
        CheckoutPriceProjection proj = createMockProjection(mockCartItem.getId(), mockProduct.getId(), 2, BigDecimal.valueOf(100), false);
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

        when(orderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder)).thenReturn(Optional.of(paidOrder));
        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartAndProductIdIn(any(), anyList())).thenReturn(List.of(mockCartItem));
        when(cartItemRepository.findCheckoutPrices(any())).thenReturn(List.of(proj));
        when(paymentCardRepository.findByCardNumber(any())).thenReturn(Optional.empty());
        when(paymentCardRepository.save(any())).thenReturn(mockPaymentCard);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(transactionRepository.findById(mockTransaction.getId())).thenReturn(Optional.of(successTransaction));
        when(paymentGatewayClient.chargeCard(any())).thenReturn(
                new CardChargeResponse("success", BigDecimal.valueOf(200), "Payment successful", "gateway-ref-456", "1111"));
        when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);
        doNothing().when(checkoutFinalizer).finalizeOrder(any(), anyList());

        RestApiResponse<UserCheckoutResponse> response = userCheckoutService.payOrder(
                mockOrder.getId(), cardPayRequest, "user@example.com");
        
        verify(paymentCardRepository).save(any());
        assertNotNull(response);
    }
}
