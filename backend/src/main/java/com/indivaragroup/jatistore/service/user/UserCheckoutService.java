package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.checkout.PaymentCard;
import com.indivaragroup.jatistore.data.entity.checkout.Transaction;
import com.indivaragroup.jatistore.data.entity.checkout.CartItem;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import com.indivaragroup.jatistore.data.utility.constant.TransactionStatus;
import com.indivaragroup.jatistore.dto.request.user.UserCheckoutRequest;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCheckoutResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.checkout.CartItemRepository;
import com.indivaragroup.jatistore.repository.OrderRepository;
import com.indivaragroup.jatistore.repository.checkout.PaymentCardRepository;
import com.indivaragroup.jatistore.repository.checkout.TransactionRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.repository.OrderDetailRepository;
import com.indivaragroup.jatistore.data.entity.checkout.SellerLedger;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import com.indivaragroup.jatistore.repository.checkout.SellerLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCheckoutService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final AuthRepository authRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SellerLedgerRepository sellerLedgerRepository;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://mock.apidog.com/m1/1332593-1333794-default")
            .build();

    private record CardChargeRequest(
        String cardNumber,
        String expiry,
        String cvc,
        int amount,
        String cardHolderName
    ) {}

    private record CardChargeResponse(
        String status,
        int amount,
        String message,
        String transactionId,
        String cardLast4
    ) {}

    private record WalletChargeRequest(
        int amount
    ) {}

    private record WalletChargeResponse(
        String status,
        int amount,
        String message,
        String transactionId
    ) {}

    @Transactional
    public RestApiResponse<UserCheckoutResponse> checkout(
            UserCheckoutRequest userCheckoutRequest,
            String email
    ) throws CoreThrowHandler {
        
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
        int amountInt = totalAmount.intValue();

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

        // 5. Call payment gateway
        if (method == PaymentMethod.CARD) {
            CardChargeRequest chargeRequest = new CardChargeRequest(
                    userCheckoutRequest.getUserCheckoutRequestCardNumber(),
                    userCheckoutRequest.getUserCheckoutRequestExpiryDate(),
                    userCheckoutRequest.getUserCheckoutRequestCvc(),
                    amountInt,
                    userCheckoutRequest.getUserCheckoutRequestCardHolderName()
            );

            try {
                CardChargeResponse response = restClient.post()
                        .uri("/api/card/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(chargeRequest)
                        .retrieve()
                        .body(CardChargeResponse.class);

                order.setStatus(OrderStatus.PAID_ON_HOLD);
                orderRepository.save(order);

                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setPaymentGatewayRef(response.transactionId());
                transactionRepository.save(transaction);

                finalizeOrder(order, cartItems);

                return buildSuccessResponse(order, transaction);

            } catch (HttpClientErrorException ex) {
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
            } catch (RestClientException ex) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);

                throw new CoreThrowHandler(RestApiError.USR_0014);
            }
        } else {
            WalletChargeRequest chargeRequest = new WalletChargeRequest(amountInt);

            try {
                WalletChargeResponse response = restClient.post()
                        .uri("/api/wallet/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(chargeRequest)
                        .retrieve()
                        .body(WalletChargeResponse.class);

                order.setStatus(OrderStatus.PAID_ON_HOLD);
                orderRepository.save(order);

                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setPaymentGatewayRef(response.transactionId());
                transactionRepository.save(transaction);

                finalizeOrder(order, cartItems);

                return buildSuccessResponse(order, transaction);

            } catch (HttpClientErrorException ex) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                if (ex.getStatusCode().value() == 402) {
                    transaction.setStatus(TransactionStatus.DECLINED);
                    transactionRepository.save(transaction);
                    throw new CoreThrowHandler(RestApiError.USR_0012);
                } else {
                    transaction.setStatus(TransactionStatus.FAILED);
                    transactionRepository.save(transaction);
                    throw new CoreThrowHandler(RestApiError.USR_0014);
                }
            } catch (RestClientException ex) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);

                throw new CoreThrowHandler(RestApiError.USR_0014);
            }
        }
    }

    private void finalizeOrder(Order order, List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            detail.setPricePerItem(item.getProduct().getPrice());
            detail.setFlashSale(false);
            orderDetailRepository.save(detail);

            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        Seller seller = cartItems.get(0).getProduct().getStore().getSeller();
        SellerLedger ledger = SellerLedger.builder()
                .seller(seller)
                .order(order)
                .amount(order.getTotalAmount())
                .balanceType(BalanceType.ON_HOLD)
                .build();
        sellerLedgerRepository.save(ledger);

        List<UUID> cartItemIds = cartItems.stream().map(CartItem::getId).toList();
        cartItemRepository.deleteAllById(cartItemIds);
    }

    private RestApiResponse<UserCheckoutResponse> buildSuccessResponse(Order order, Transaction transaction) {
        UserCheckoutResponse data = new UserCheckoutResponse(
                order.getId(),
                transaction.getId(),
                transaction.getPaymentGatewayRef(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getTotalAmount()
        );
        return RestApiResponse.success(data);
    }
}
