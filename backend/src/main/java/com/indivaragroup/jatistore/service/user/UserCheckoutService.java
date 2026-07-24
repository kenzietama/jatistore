package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.*;
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
import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.repository.*;
import com.indivaragroup.jatistore.repository.projection.CheckoutPriceProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import com.indivaragroup.jatistore.dto.request.payment.CardChargeRequest;
import com.indivaragroup.jatistore.dto.request.payment.WalletChargeRequest;
import com.indivaragroup.jatistore.service.payment.PaymentGatewayClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCheckoutService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final AuthRepository authRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final PaymentGatewayClient paymentGatewayClient;
    private final CheckoutFinalizer checkoutFinalizer;
    private final FlashSaleItemRepository flashSaleItemRepository;

    @Audit(action = "ORDER_CREATE", affectedModule = "ORDERS", description = "User creates pending order")
    @Transactional
    public RestApiResponse<CreateOrderResponse> createOrder(
            CreateOrderRequest request,
            String email
    ) throws CoreThrowHandler {
        UUID[] cartItemIdsArray = request.getSelectedCartItemIds().toArray(new UUID[0]);

        // 1. Validate seller consistency
        int sellerCount = cartItemRepository.selectDistinctStore(cartItemIdsArray);
        if (sellerCount != 1) {
            throw new CoreThrowHandler(RestApiError.USR_0019);
        }

        // 2. Load and validate cart items/stock
        List<CartItem> cartItems = cartItemRepository.findAllById(request.getSelectedCartItemIds());
        if (cartItems.isEmpty()) {
            throw new CoreThrowHandler(RestApiError.USR_0009);
        }

        // 3. Validate stock & active product status
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.GEN_0005));

        for (CartItem item : cartItems) {
            if (item.getProduct().getDeletedAt() != null) {
                throw new CoreThrowHandler(RestApiError.USR_0001);
            }
            if (!item.getProduct().getStore().getSeller().getActive()) {
                throw new CoreThrowHandler(RestApiError.USR_0001);
            }
            if (item.getProduct().getStock() < item.getQuantity()) {
                String customMessage = RestApiError.USR_0011.getMessage()
                        .replace("{productName}", item.getProduct().getName());
                throw new CoreThrowHandler(RestApiError.USR_0011.getCode(), customMessage, null);
            }
        }

        // 4. Calculate total amount (backend security) & validate active flash sale quota
        List<CheckoutPriceProjection> priceProjections = cartItemRepository.findCheckoutPrices(cartItemIdsArray);

        for (CartItem cartItem : cartItems) {
            CheckoutPriceProjection projection = priceProjections.stream()
                    .filter(p -> p.getCartItemId().equals(cartItem.getId()))
                    .findFirst()
                    .orElseThrow();

            if (Boolean.TRUE.equals(projection.getFlashSale())) {
                FlashSaleItem flashSaleItem = flashSaleItemRepository
                        .findByProductAndActiveFlashSale(projection.getProductId())
                        .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0024));

                if (flashSaleItem.getRemainingQuota() < cartItem.getQuantity()) {
                    throw new CoreThrowHandler(RestApiError.USR_0025);
                }
            }
        }

        BigDecimal totalAmount = priceProjections.stream()
                .map(p -> p.getEffectivePrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Create order (PENDING)
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        // 6. Create order details
        Order finalOrder = order;
        List<OrderDetail> orderDetails = cartItems.stream().map(cartItem -> {
            CheckoutPriceProjection projection = priceProjections.stream()
                    .filter(p -> p.getCartItemId().equals(cartItem.getId()))
                    .findFirst()
                    .orElseThrow();

            OrderDetail detail = new OrderDetail();
            detail.setOrder(finalOrder);
            detail.setProduct(cartItem.getProduct());
            detail.setQuantity(cartItem.getQuantity());
            detail.setPricePerItem(projection.getEffectivePrice());
            detail.setFlashSale(projection.getFlashSale());
            return detail;
        }).toList();
        orderDetailRepository.saveAll(orderDetails);

        return RestApiResponse.success(CreateOrderResponse.builder()
                .orderId(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build());
    }

    @Audit(action = "ORDER_PAY", affectedModule = "ORDERS", description = "User pays for order")
    @Transactional(noRollbackFor = CoreThrowHandler.class)
    public RestApiResponse<UserCheckoutResponse> payOrder(
            UUID orderId,
            PayOrderRequest request,
            String email
    ) throws CoreThrowHandler {
        // Guard 1 & 2: Ownership and status check (in one transaction)
        Order order = validateOrderForPayment(orderId, email);

        // Re-validate stock and amount
        List<CartItem> cartItems = revalidateStockAndAmount(order, email);

        // Create transaction and call payment gateway
        Transaction transaction = createPendingTransaction(order, request, email);

        // Call payment gateway outside transaction boundary
        return executePayment(order, transaction, cartItems, request);
    }

    @Transactional
    protected Order validateOrderForPayment(UUID orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0015));

        if (!order.getUser().getEmail().equals(email)) {
            throw new CoreThrowHandler(RestApiError.USR_0015);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CoreThrowHandler(RestApiError.USR_0022);
        }

        return order;
    }

    @Transactional
    protected List<CartItem> revalidateStockAndAmount(Order order, String email) {
        // Get product IDs from order details
        List<UUID> productIds = order.getOrderDetails().stream()
                .map(detail -> detail.getProduct().getId())
                .toList();

        // Find user's cart and get matching cart items
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0006));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0009));

        List<CartItem> cartItems = cartItemRepository.findByCartAndProductIdIn(cart, productIds);

        if (cartItems.isEmpty()) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            throw new CoreThrowHandler(RestApiError.USR_0009);
        }

        // Re-validate stock & active product status
        for (CartItem item : cartItems) {
            if (item.getProduct().getDeletedAt() != null) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                throw new CoreThrowHandler(RestApiError.USR_0001);
            }

            if (!item.getProduct().getStore().getSeller().getActive()) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                throw new CoreThrowHandler(RestApiError.USR_0001);
            }

            if (item.getProduct().getStock() < item.getQuantity()) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                String customMessage = RestApiError.USR_0011.getMessage()
                        .replace("{productName}", item.getProduct().getName());
                throw new CoreThrowHandler(RestApiError.USR_0011.getCode(), customMessage, null);
            }
        }

        // Recalculate and validate amount with fresh flash-sale prices
        UUID[] cartItemIds = cartItems.stream().map(CartItem::getId).toArray(UUID[]::new);
        List<CheckoutPriceProjection> freshProjections = cartItemRepository.findCheckoutPrices(cartItemIds);

        BigDecimal recalculated = freshProjections.stream()
                .map(p -> p.getEffectivePrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (recalculated.compareTo(order.getTotalAmount()) != 0) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            throw new CoreThrowHandler(RestApiError.USR_0023);
        }

        // Validate flash sale quota before payment gateway is invoked
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                if (Boolean.TRUE.equals(detail.getFlashSale())) {
                    Optional<FlashSaleItem> flashSaleItemOpt = flashSaleItemRepository
                            .findByProductAndActiveFlashSale(detail.getProduct().getId());

                    if (flashSaleItemOpt.isPresent()) {
                        FlashSaleItem flashSaleItem = flashSaleItemOpt.get();
                        if (flashSaleItem.getRemainingQuota() < detail.getQuantity()) {
                            order.setStatus(OrderStatus.CANCELLED);
                            orderRepository.save(order);
                            throw new CoreThrowHandler(RestApiError.USR_0025);
                        }
                    }
                }
            }
        }

        if (recalculated.compareTo(order.getTotalAmount()) != 0) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            throw new CoreThrowHandler(RestApiError.USR_0023);
        }

        return cartItems;
    }

    @Transactional
    protected Transaction createPendingTransaction(Order order, PayOrderRequest request, String email) {
        PaymentMethod method = request.getPaymentMethod();
        PaymentCard paymentCard = null;

        if (method == PaymentMethod.CARD) {
            paymentCard = getOrCreatePaymentCard(request, email);
        }

        Transaction transaction = Transaction.builder()
                .order(order)
                .paymentMethod(method)
                .paymentCard(paymentCard)
                .status(TransactionStatus.PENDING)
                .build();

        return transactionRepository.save(transaction);
    }

    private PaymentCard getOrCreatePaymentCard(PayOrderRequest request, String email) {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.GEN_0005));

        return paymentCardRepository.findByCardNumber(request.getCardNumber())
                .orElseGet(() -> paymentCardRepository.save(PaymentCard.builder()
                        .user(user)
                        .cardNumber(request.getCardNumber())
                        .cardHolderName(request.getCardHolderName())
                        .expiryDate(request.getExpiryDate())
                        .build()));
    }

    @Transactional(noRollbackFor = CoreThrowHandler.class)
    protected RestApiResponse<UserCheckoutResponse> executePayment(
            Order order,
            Transaction transaction,
            List<CartItem> cartItems,
            PayOrderRequest request
    ) throws CoreThrowHandler {
        try {
            String gatewayRef = callPaymentGateway(request, order.getTotalAmount());
            return handlePaymentSuccess(order, transaction, cartItems, gatewayRef);
        } catch (HttpClientErrorException ex) {
            return handlePaymentError(order, transaction, ex);
        } catch (ResourceAccessException ex) {
            return handlePaymentTimeout(order, transaction);
        } catch (RestClientException ex) {
            return handlePaymentFailure(order, transaction);
        }
    }

    private String callPaymentGateway(PayOrderRequest request, BigDecimal amount) {
        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            CardChargeRequest chargeRequest = new CardChargeRequest(
                    request.getCardNumber(),
                    request.getExpiryDate(),
                    request.getCvc(),
                    amount,
                    request.getCardHolderName()
            );
            return paymentGatewayClient.chargeCard(chargeRequest).transactionId();
        } else {
            WalletChargeRequest chargeRequest = new WalletChargeRequest(amount);
            return paymentGatewayClient.chargeWallet(chargeRequest).transactionId();
        }
    }

    @Transactional(noRollbackFor = CoreThrowHandler.class)
    public RestApiResponse<UserCheckoutResponse> handlePaymentSuccess(
            Order order,
            Transaction transaction,
            List<CartItem> cartItems,
            String gatewayRef
    ) {
        // Commit SUCCESS status + gatewayRef first
        order.setStatus(OrderStatus.PAID_ON_HOLD);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setPaymentGatewayRef(gatewayRef);

        orderRepository.save(order);
        transactionRepository.save(transaction);

        try {
            // Finalize order in separate transaction
            checkoutFinalizer.finalizeOrder(order, cartItems);
        } catch (CoreThrowHandler ex) {
            order.setStatus(OrderStatus.CANCELLED);
            transaction.setStatus(TransactionStatus.FAILED);
            orderRepository.save(order);
            transactionRepository.save(transaction);
            throw ex;
        }

        Order committedOrder = orderRepository.findById(order.getId()).orElseThrow();
        Transaction committedTxn = transactionRepository.findById(transaction.getId()).orElseThrow();

        return RestApiResponse.success(UserCheckoutResponse.from(committedOrder, committedTxn));
    }

    @Transactional(noRollbackFor = CoreThrowHandler.class)
    public RestApiResponse<UserCheckoutResponse> handlePaymentError(
            Order order,
            Transaction transaction,
            HttpClientErrorException ex
    ) throws CoreThrowHandler {
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        if (ex.getStatusCode().value() == 402) {
            transaction.setStatus(TransactionStatus.DECLINED);
            transactionRepository.save(transaction);
            throw new CoreThrowHandler(RestApiError.USR_0013);
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new CoreThrowHandler(RestApiError.USR_0014);
        }
    }

    @Transactional(noRollbackFor = CoreThrowHandler.class)
    public RestApiResponse<UserCheckoutResponse> handlePaymentTimeout(
            Order order,
            Transaction transaction
    ) throws CoreThrowHandler {
        order.setStatus(OrderStatus.CANCELLED);
        transaction.setStatus(TransactionStatus.FAILED);

        orderRepository.save(order);
        transactionRepository.save(transaction);

        throw new CoreThrowHandler(RestApiError.USR_0017);
    }

    @Transactional(noRollbackFor = CoreThrowHandler.class)
    public RestApiResponse<UserCheckoutResponse> handlePaymentFailure(
            Order order,
            Transaction transaction
    ) throws CoreThrowHandler {
        order.setStatus(OrderStatus.CANCELLED);
        transaction.setStatus(TransactionStatus.FAILED);

        orderRepository.save(order);
        transactionRepository.save(transaction);

        throw new CoreThrowHandler(RestApiError.USR_0014);
    }
}
