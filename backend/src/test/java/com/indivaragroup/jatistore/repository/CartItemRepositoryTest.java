package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.*;
import com.indivaragroup.jatistore.repository.projection.CheckoutPriceProjection;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findCheckoutPrices_NoFlashSale_ReturnsBasePrice() {
        // Setup
        User user = createUser("buyer1@test.com");
        Seller seller = createSeller("seller1@test.com");
        Store store = createStore(seller);
        Product product = createProduct(store, new BigDecimal("100.00"), 10);
        Cart cart = createCart(user);
        CartItem cartItem = createCartItem(cart, product, 2);

        entityManager.flush();

        // Execute
        List<CheckoutPriceProjection> result = cartItemRepository.findCheckoutPrices(
            new UUID[]{cartItem.getId()}
        );

        // Verify
        assertThat(result).hasSize(1);
        CheckoutPriceProjection projection = result.get(0);
        assertThat(projection.getCartItemId()).isEqualTo(cartItem.getId());
        assertThat(projection.getProductId()).isEqualTo(product.getId());
        assertThat(projection.getQuantity()).isEqualTo(2);
        assertThat(projection.getEffectivePrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(projection.getFlashSale()).isFalse();
    }

    @Test
    void findCheckoutPrices_ActiveFlashSale_ReturnsFlashPrice() {
        // Setup
        User user = createUser("buyer2@test.com");
        Seller seller = createSeller("seller2@test.com");
        Store store = createStore(seller);
        Product product = createProduct(store, new BigDecimal("100.00"), 10);
        FlashSale flashSale = createActiveFlashSale();
        FlashSaleItem flashSaleItem = createFlashSaleItem(flashSale, product, new BigDecimal("80.00"));
        Cart cart = createCart(user);
        CartItem cartItem = createCartItem(cart, product, 2);

        entityManager.flush();

        // Execute
        List<CheckoutPriceProjection> result = cartItemRepository.findCheckoutPrices(
            new UUID[]{cartItem.getId()}
        );

        // Verify
        assertThat(result).hasSize(1);
        CheckoutPriceProjection projection = result.get(0);
        assertThat(projection.getEffectivePrice()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(projection.getFlashSale()).isTrue();
    }

    @Test
    void findCheckoutPrices_ExpiredFlashSale_ReturnsBasePrice() {
        // Setup
        User user = createUser("buyer3@test.com");
        Seller seller = createSeller("seller3@test.com");
        Store store = createStore(seller);
        Product product = createProduct(store, new BigDecimal("100.00"), 10);
        FlashSale flashSale = createExpiredFlashSale();
        FlashSaleItem flashSaleItem = createFlashSaleItem(flashSale, product, new BigDecimal("80.00"));
        Cart cart = createCart(user);
        CartItem cartItem = createCartItem(cart, product, 2);

        entityManager.flush();

        // Execute
        List<CheckoutPriceProjection> result = cartItemRepository.findCheckoutPrices(
            new UUID[]{cartItem.getId()}
        );

        // Verify
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEffectivePrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.get(0).getFlashSale()).isFalse();
    }

    @Test
    void findCheckoutPrices_FutureFlashSale_ReturnsBasePrice() {
        // Setup
        User user = createUser("buyer4@test.com");
        Seller seller = createSeller("seller4@test.com");
        Store store = createStore(seller);
        Product product = createProduct(store, new BigDecimal("100.00"), 10);
        FlashSale flashSale = createFutureFlashSale();
        FlashSaleItem flashSaleItem = createFlashSaleItem(flashSale, product, new BigDecimal("80.00"));
        Cart cart = createCart(user);
        CartItem cartItem = createCartItem(cart, product, 2);

        entityManager.flush();

        // Execute
        List<CheckoutPriceProjection> result = cartItemRepository.findCheckoutPrices(
            new UUID[]{cartItem.getId()}
        );

        // Verify
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEffectivePrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.get(0).getFlashSale()).isFalse();
    }

    // Helper methods
    private User createUser(String email) {
        String randomDigits = String.valueOf(System.nanoTime()).substring(5);
        User user = new User();
        user.setEmail(email);
        user.setUsername("user_" + randomDigits);
        user.setPhoneNumber("08" + randomDigits);
        user.setPasswordHash("hashedpassword");
        user.setFullName("Test User");
        entityManager.persist(user);
        return user;
    }

    private Seller createSeller(String email) {
        User user = createUser(email);
        Seller seller = new Seller();
        seller.setUser(user);
        seller.setCachedAvailableBalance(BigDecimal.ZERO);
        seller.setCachedOnHoldBalance(BigDecimal.ZERO);
        seller.setActive(true);
        entityManager.persist(seller);
        return seller;
    }

    private Store createStore(Seller seller) {
        Store store = new Store();
        store.setSeller(seller);
        store.setStoreName("Test Store " + UUID.randomUUID());
        entityManager.persist(store);
        return store;
    }

    private ProductCategory createCategory() {
        ProductCategory category = new ProductCategory();
        category.setName("Test Category " + UUID.randomUUID());
        entityManager.persist(category);
        return category;
    }

    private Product createProduct(Store store, BigDecimal price, Integer stock) {
        ProductCategory category = createCategory();
        Product product = new Product();
        product.setStore(store);
        product.setCategory(category);
        product.setName("Test Product");
        product.setPrice(price);
        product.setStock(stock);
        product.setDescription("Test Description");
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        entityManager.persist(product);
        return product;
    }

    private Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUserId(user.getId());
        entityManager.persist(cart);
        return cart;
    }

    private CartItem createCartItem(Cart cart, Product product, Integer quantity) {
        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        entityManager.persist(item);
        return item;
    }

    private FlashSale createActiveFlashSale() {
        FlashSale flashSale = new FlashSale();
        flashSale.setName("Active Flash Sale");
        flashSale.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        flashSale.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));
        entityManager.persist(flashSale);
        return flashSale;
    }

    private FlashSale createExpiredFlashSale() {
        FlashSale flashSale = new FlashSale();
        flashSale.setName("Expired Flash Sale");
        flashSale.setStartTime(Instant.now().minus(2, ChronoUnit.HOURS));
        flashSale.setEndTime(Instant.now().minus(1, ChronoUnit.HOURS));
        entityManager.persist(flashSale);
        return flashSale;
    }

    private FlashSale createFutureFlashSale() {
        FlashSale flashSale = new FlashSale();
        flashSale.setName("Future Flash Sale");
        flashSale.setStartTime(Instant.now().plus(1, ChronoUnit.HOURS));
        flashSale.setEndTime(Instant.now().plus(2, ChronoUnit.HOURS));
        entityManager.persist(flashSale);
        return flashSale;
    }

    private FlashSaleItem createFlashSaleItem(FlashSale flashSale, Product product, BigDecimal flashPrice) {
        FlashSaleItem item = new FlashSaleItem();
        item.setFlashSale(flashSale);
        item.setProduct(product);
        item.setFlashPrice(flashPrice);
        item.setRemainingQuota(100);
        entityManager.persist(item);
        return item;
    }
}
