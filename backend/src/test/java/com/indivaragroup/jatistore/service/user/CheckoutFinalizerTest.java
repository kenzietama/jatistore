package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.*;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutFinalizerTest {

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SellerLedgerRepository sellerLedgerRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private FlashSaleItemRepository flashSaleItemRepository;

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
    void finalizeOrder_WithFlashSaleItem_DecrementsQuota() {
        // Setup
        OrderDetail flashSaleDetail = new OrderDetail();
        flashSaleDetail.setProduct(product);
        flashSaleDetail.setQuantity(2);
        flashSaleDetail.setPricePerItem(new BigDecimal("80.00"));
        flashSaleDetail.setFlashSale(true);
        order.setOrderDetails(List.of(flashSaleDetail));

        FlashSaleItem flashSaleItem = new FlashSaleItem();
        flashSaleItem.setId(UUID.randomUUID());
        flashSaleItem.setProduct(product);
        flashSaleItem.setRemainingQuota(50);

        when(flashSaleItemRepository.findByProductAndActiveFlashSaleForUpdate(product.getId()))
            .thenReturn(Optional.of(flashSaleItem));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(sellerLedgerRepository.save(any(SellerLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        // Execute
        checkoutFinalizer.finalizeOrder(order, List.of(cartItem));

        // Verify
        verify(flashSaleItemRepository).findByProductAndActiveFlashSaleForUpdate(product.getId());
        verify(flashSaleItemRepository).save(flashSaleItem);
        assertThat(flashSaleItem.getRemainingQuota()).isEqualTo(48); // 50 - 2
        verify(orderDetailRepository, never()).save(any(OrderDetail.class));
        verify(orderDetailRepository, never()).saveAll(anyList());
    }

    @Test
    void finalizeOrder_QuotaExhausted_ThrowsUSR_0025() {
        // Setup
        OrderDetail flashSaleDetail = new OrderDetail();
        flashSaleDetail.setProduct(product);
        flashSaleDetail.setQuantity(5);
        flashSaleDetail.setPricePerItem(new BigDecimal("80.00"));
        flashSaleDetail.setFlashSale(true);
        order.setOrderDetails(List.of(flashSaleDetail));

        FlashSaleItem flashSaleItem = new FlashSaleItem();
        flashSaleItem.setId(UUID.randomUUID());
        flashSaleItem.setProduct(product);
        flashSaleItem.setRemainingQuota(3); // Less than quantity ordered

        when(flashSaleItemRepository.findByProductAndActiveFlashSaleForUpdate(product.getId()))
            .thenReturn(Optional.of(flashSaleItem));

        // Execute & Verify
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            checkoutFinalizer.finalizeOrder(order, List.of(cartItem));
        });

        assertThat(exception.getRestApiError()).isEqualTo(RestApiError.USR_0025);
        verify(flashSaleItemRepository, never()).save(any());
    }

    @Test
    void finalizeOrder_FlashSaleEndedDuringPayment_ThrowsUSR_0024() {
        // Setup
        OrderDetail flashSaleDetail = new OrderDetail();
        flashSaleDetail.setProduct(product);
        flashSaleDetail.setQuantity(2);
        flashSaleDetail.setPricePerItem(new BigDecimal("80.00"));
        flashSaleDetail.setFlashSale(true);
        order.setOrderDetails(List.of(flashSaleDetail));

        when(flashSaleItemRepository.findByProductAndActiveFlashSaleForUpdate(product.getId()))
            .thenReturn(Optional.empty()); // No active flash sale found

        // Execute & Verify
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> {
            checkoutFinalizer.finalizeOrder(order, List.of(cartItem));
        });

        assertThat(exception.getRestApiError()).isEqualTo(RestApiError.USR_0024);
    }

    @Test
    void finalizeOrder_NoFlashSale_SkipsQuotaDecrement() {
        // Setup
        OrderDetail normalDetail = new OrderDetail();
        normalDetail.setProduct(product);
        normalDetail.setQuantity(2);
        normalDetail.setPricePerItem(new BigDecimal("100.00"));
        normalDetail.setFlashSale(false);
        order.setOrderDetails(List.of(normalDetail));

        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(sellerLedgerRepository.save(any(SellerLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        // Execute
        checkoutFinalizer.finalizeOrder(order, List.of(cartItem));

        // Verify
        verify(flashSaleItemRepository, never()).findByProductAndActiveFlashSaleForUpdate(any());
        verify(flashSaleItemRepository, never()).save(any());
        verify(productRepository).save(product);
        assertThat(product.getStock()).isEqualTo(8); // 10 - 2
    }

    @Test
    void finalizeOrder_NullOrderDetails_SkipsFlashSaleLogic() {
        order.setOrderDetails(null);
        
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(sellerLedgerRepository.save(any(SellerLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        checkoutFinalizer.finalizeOrder(order, List.of(cartItem));

        verify(flashSaleItemRepository, never()).findByProductAndActiveFlashSaleForUpdate(any());
        verify(flashSaleItemRepository, never()).save(any());
        verify(productRepository).save(product);
        assertThat(product.getStock()).isEqualTo(8); // 10 - 2
    }
}
