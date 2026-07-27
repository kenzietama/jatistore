package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.*;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.repository.CartItemRepository;
import com.indivaragroup.jatistore.repository.SellerLedgerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutFinalizerTest {

    @Mock
    private SellerLedgerRepository sellerLedgerRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CheckoutFinalizer checkoutFinalizer;

    private Order order;
    private Product product;
    private CartItem cartItem;
    private Seller seller;
    private Store store;

    @BeforeEach
    void setUp() {
        seller = new Seller();
        seller.setId(UUID.randomUUID());

        store = new Store();
        store.setId(UUID.randomUUID());
        store.setSeller(seller);

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setStore(store);

        User user = new User();
        user.setId(UUID.randomUUID());

        Cart cart = new Cart();
        cart.setUserId(user.getId());

        cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID());
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("160.00"));
        order.setStatus(OrderStatus.PAID_ON_HOLD);
    }

    @Test
    void finalizeOrder_Success_CreatesSellerLedgerAndDeletesCartItems() {
        when(sellerLedgerRepository.save(any(SellerLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        checkoutFinalizer.finalizeOrder(order, List.of(cartItem));

        ArgumentCaptor<SellerLedger> ledgerCaptor = ArgumentCaptor.forClass(SellerLedger.class);
        verify(sellerLedgerRepository).save(ledgerCaptor.capture());
        SellerLedger savedLedger = ledgerCaptor.getValue();
        assertThat(savedLedger.getSeller()).isEqualTo(seller);
        assertThat(savedLedger.getOrder()).isEqualTo(order);
        assertThat(savedLedger.getAmount()).isEqualByComparingTo(new BigDecimal("160.00"));
        assertThat(savedLedger.getBalanceType()).isEqualTo(BalanceType.ON_HOLD);

        verify(cartItemRepository).deleteAllById(List.of(cartItem.getId()));
    }
}
