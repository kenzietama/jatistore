package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.data.entity.PaymentCard;
import com.indivaragroup.jatistore.data.entity.Transaction;
import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import com.indivaragroup.jatistore.data.utility.constant.TransactionStatus;
import com.indivaragroup.jatistore.dto.request.user.UserCheckoutRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCheckoutResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.CartItemRepository;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.PaymentCardRepository;
import com.indivaragroup.jatistore.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import com.indivaragroup.jatistore.dto.request.payment.CardChargeRequest;
import com.indivaragroup.jatistore.dto.request.payment.WalletChargeRequest;
import com.indivaragroup.jatistore.service.payment.PaymentGatewayClient;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCheckoutService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final AuthRepository authRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final PlatformTransactionManager transactionManager;
    private final PaymentGatewayClient paymentGatewayClient;
    private final CheckoutFinalizer checkoutFinalizer;

    private static class OrderContext {
        Order order;
        Transaction transaction;
        List<CartItem> cartItems;
        BigDecimal amount;
        PaymentMethod method;
    }

    @Audit(action = "ORDER_CREATE", affectedModule = "ORDERS", description = "User places new order")
    public RestApiResponse<UserCheckoutResponse> checkout(
            UserCheckoutRequest userCheckoutRequest,
            String email
    ) throws CoreThrowHandler {
        TransactionTemplate template = new TransactionTemplate(transactionManager);

        // 1. Create order and transaction in PENDING state (committed in Transaction 1)
        OrderContext ctx;
        try {
            ctx = template.execute(status -> {
                // 1. Validate seller consistency
                int sellerCount =
                        cartItemRepository.selectDistinctStore(userCheckoutRequest.getUserSelectedCartItemId());
                if (sellerCount != 1) {
                    throw new CoreThrowHandler(RestApiError.USR_0019);
                }

                // 2. Load and validate cart items/stock
                List<CartItem> cartItems = cartItemRepository.findAllById(Arrays.asList(userCheckoutRequest.getUserSelectedCartItemId()));
                if (cartItems.isEmpty()) {
                    throw new CoreThrowHandler(RestApiError.USR_0009);
                }
                for (CartItem item : cartItems) {
                    if (item.getProduct().getStock() < item.getQuantity()) {
                        String customMessage = RestApiError.USR_0011.getMessage().replace("{productName}", item.getProduct().getName());
                        throw new CoreThrowHandler(RestApiError.USR_0011.getCode(), customMessage, null);
                    }
                }

                User user = authRepository.findByEmail(email).orElseThrow(() -> new CoreThrowHandler(RestApiError.GEN_0005));
                BigDecimal totalAmount = cartItemRepository.calculateTotalAmount(userCheckoutRequest.getUserSelectedCartItemId());

                // 3. Create order (PENDING)
                Order order = Order.builder()
                        .user(user)
                        .totalAmount(totalAmount)
                        .status(OrderStatus.PENDING)
                        .build();
                order = orderRepository.save(order);

                // 4. Create transaction (PENDING)
                PaymentMethod method = userCheckoutRequest.getUserCheckoutRequestPaymentMethod();
                PaymentCard paymentCard = null;

                if (method == PaymentMethod.CARD) {
                    String cardNum = userCheckoutRequest.getUserCheckoutRequestCardNumber();
                    paymentCard = paymentCardRepository.findByCardNumber(cardNum).orElse(null);
                    if (paymentCard == null) {
                        paymentCard = PaymentCard.builder()
                                .user(user)
                                .cardNumber(cardNum)
                                .cardHolderName(userCheckoutRequest.getUserCheckoutRequestCardHolderName())
                                .expiryDate(userCheckoutRequest.getUserCheckoutRequestExpiryDate())
                                .build();
                        paymentCard = paymentCardRepository.save(paymentCard);
                    }
                }

                Transaction transaction = Transaction.builder()
                        .order(order)
                        .paymentMethod(method)
                        .paymentCard(paymentCard)
                        .status(TransactionStatus.PENDING)
                        .build();
                transaction = transactionRepository.save(transaction);

                OrderContext context = new OrderContext();
                context.order = order;
                context.transaction = transaction;
                context.cartItems = cartItems;
                context.amount = totalAmount;
                context.method = method;
                return context;
            });
        } catch (Exception ex) {
            if (ex instanceof CoreThrowHandler) {
                throw (CoreThrowHandler) ex;
            }
            throw ex;
        }

        // 5. Call payment gateway outside of any database transaction, keeping DB connections free
        if (ctx.method == PaymentMethod.CARD) {
            CardChargeRequest chargeRequest = new CardChargeRequest(
                    userCheckoutRequest.getUserCheckoutRequestCardNumber(),
                    userCheckoutRequest.getUserCheckoutRequestExpiryDate(),
                    userCheckoutRequest.getUserCheckoutRequestCvc(),
                    ctx.amount,
                    userCheckoutRequest.getUserCheckoutRequestCardHolderName()
            );
            return executePaymentFlow(
                    ctx,
                    () -> paymentGatewayClient.chargeCard(chargeRequest).transactionId(),
                    RestApiError.USR_0013
            );
        } else {
            WalletChargeRequest chargeRequest = new WalletChargeRequest(ctx.amount);
            return executePaymentFlow(
                    ctx,
                    () -> paymentGatewayClient.chargeWallet(chargeRequest).transactionId(),
                    RestApiError.USR_0012
            );
        }
    }

    @FunctionalInterface
    private interface PaymentCall {
        String execute() throws HttpClientErrorException, RestClientException;
    }

    private RestApiResponse<UserCheckoutResponse> executePaymentFlow(
            OrderContext ctx,
            PaymentCall paymentCall,
            RestApiError declinedError
    ) throws CoreThrowHandler {
        TransactionTemplate template = new TransactionTemplate(transactionManager);

        try {
            String gatewayRef = paymentCall.execute();

            // Update database state on gateway success (committed in Transaction 2)
            Order finalOrder = ctx.order;
            Transaction finalTransaction = ctx.transaction;
            List<CartItem> finalCartItems = ctx.cartItems;
            template.executeWithoutResult(status -> {
                Order ord = orderRepository.findById(finalOrder.getId()).orElseThrow();
                Transaction trx = transactionRepository.findById(finalTransaction.getId()).orElseThrow();

                ord.setStatus(OrderStatus.PAID_ON_HOLD);
                orderRepository.save(ord);

                trx.setStatus(TransactionStatus.SUCCESS);
                trx.setPaymentGatewayRef(gatewayRef);
                transactionRepository.save(trx);

                checkoutFinalizer.finalizeOrder(ord, finalCartItems);
            });

            Order committedOrder = orderRepository.findById(ctx.order.getId()).orElseThrow();
            Transaction committedTrx = transactionRepository.findById(ctx.transaction.getId()).orElseThrow();
            return RestApiResponse.success(UserCheckoutResponse.from(committedOrder, committedTrx));

        } catch (HttpClientErrorException ex) {
            Order finalOrder = ctx.order;
            Transaction finalTransaction = ctx.transaction;
            template.executeWithoutResult(status -> {
                Order ord = orderRepository.findById(finalOrder.getId()).orElseThrow();
                Transaction trx = transactionRepository.findById(finalTransaction.getId()).orElseThrow();

                ord.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(ord);

                if (ex.getStatusCode().value() == 402) {
                    trx.setStatus(TransactionStatus.DECLINED);
                } else {
                    trx.setStatus(TransactionStatus.FAILED);
                }
                transactionRepository.save(trx);
            });

            if (ex.getStatusCode().value() == 402) {
                throw new CoreThrowHandler(declinedError);
            } else {
                throw new CoreThrowHandler(RestApiError.USR_0014);
            }
        } catch (ResourceAccessException ex) {
            Order finalOrder = ctx.order;
            Transaction finalTransaction = ctx.transaction;
            template.executeWithoutResult(status -> {
                Order ord = orderRepository.findById(finalOrder.getId()).orElseThrow();
                Transaction trx = transactionRepository.findById(finalTransaction.getId()).orElseThrow();

                ord.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(ord);

                trx.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(trx);
            });

            throw new CoreThrowHandler(RestApiError.USR_0017);
        } catch (RestClientException ex) {
            Order finalOrder = ctx.order;
            Transaction finalTransaction = ctx.transaction;
            template.executeWithoutResult(status -> {
                Order ord = orderRepository.findById(finalOrder.getId()).orElseThrow();
                Transaction trx = transactionRepository.findById(finalTransaction.getId()).orElseThrow();

                ord.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(ord);

                trx.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(trx);
            });

            throw new CoreThrowHandler(RestApiError.USR_0014);
        }
    }
}
