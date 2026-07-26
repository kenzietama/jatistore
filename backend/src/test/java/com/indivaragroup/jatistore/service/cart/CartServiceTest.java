package com.indivaragroup.jatistore.service.cart;

import com.indivaragroup.jatistore.data.entity.Cart;
import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.dto.request.cart.AddToCartRequest;
import com.indivaragroup.jatistore.dto.request.cart.UpdateCartItemQuantityRequest;
import com.indivaragroup.jatistore.dto.response.cart.CartResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.CartItemRepository;
import com.indivaragroup.jatistore.repository.CartRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private UUID userId;
    private UUID productId;
    private UUID cartId;
    private UUID cartItemId;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        cartItemId = UUID.randomUUID();

        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setImage("test.jpg");

        com.indivaragroup.jatistore.data.entity.Seller seller = new com.indivaragroup.jatistore.data.entity.Seller();
        seller.setActive(true);

        com.indivaragroup.jatistore.data.entity.Store store = new com.indivaragroup.jatistore.data.entity.Store();
        store.setSeller(seller);

        product.setStore(store);

        cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(userId);

        cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
    }

    // ====================================================================
    // addToCart
    // ====================================================================

    @Test
    void addToCart_NewCart_ShouldCreateCartAndAddItem() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.findByCartIdAndProductId(any(UUID.class), eq(productId)))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // Act
        CartItem result = cartService.addToCart(userId, request);

        // Assert
        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_ExistingCartItem_ShouldIncrementQuantity() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                .thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // Act
        CartItem result = cartService.addToCart(userId, request);

        // Assert
        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_ProductNotFound_ShouldThrowUSR0001() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
                () -> cartService.addToCart(userId, request));

        assertEquals(RestApiError.USR_0001, exception.getRestApiError());
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_InsufficientStock_ShouldThrowUSR0003() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(20)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
                () -> cartService.addToCart(userId, request));

        assertEquals(RestApiError.USR_0003, exception.getRestApiError());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addToCart_ExistingItemExceedsStock_ShouldThrowUSR0003() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(9)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                .thenReturn(Optional.of(cartItem));

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
                () -> cartService.addToCart(userId, request));

        assertEquals(RestApiError.USR_0003, exception.getRestApiError());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    // ====================================================================
    // getCartByUserId
    // ====================================================================

    @Test
    void getCartByUserId_EmptyCart_ShouldReturnEmptyResponse() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act
        CartResponse result = cartService.getCartByUserId(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
    }

    @Test
    void getCartByUserId_WithItems_ShouldReturnCartResponse() {
        // Arrange
        Object[] rawItem = new Object[] {
                cartItemId,
                productId,
                "Test Product",
                "test.jpg",
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                2,
                10,
                UUID.randomUUID(),
                "Test Store",
                true
        };
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.getCartItemsWithFlashSale(cartId)).thenReturn(List.<Object[]>of(rawItem));

        // Act
        CartResponse result = cartService.getCartByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(new BigDecimal("200.00"), result.getTotalPrice());
        assertEquals("Test Product", result.getItems().get(0).getProductName());
        assertEquals(10, result.getItems().get(0).getMaxStock());
        assertEquals(productId, result.getItems().get(0).getProductId());
        assertEquals(cartItemId, result.getItems().get(0).getId());
    }

    // ====================================================================
    // updateCartItemQuantity
    // ====================================================================

    @Test
    void updateCartItemQuantity_ValidQuantity_ShouldUpdate() {
        // Arrange
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(5)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // Act
        cartService.updateCartItemQuantity(userId, cartItemId, request);

        // Assert
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void updateCartItemQuantity_QuantityZero_ShouldDeleteItem() {
        // Arrange
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(0)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // Act
        cartService.updateCartItemQuantity(userId, cartItemId, request);

        // Assert
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updateCartItemQuantity_CartItemNotFound_ShouldThrowUSR0004() {
        // Arrange
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(5)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
                () -> cartService.updateCartItemQuantity(userId, cartItemId, request));

        assertEquals(RestApiError.USR_0004, exception.getRestApiError());
    }

    @Test
    void updateCartItemQuantity_UnauthorizedUser_ShouldThrowUSR0006() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(5)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
                () -> cartService.updateCartItemQuantity(otherUserId, cartItemId, request));

        assertEquals(RestApiError.USR_0006, exception.getRestApiError());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updateCartItemQuantity_InsufficientStock_ShouldThrowUSR0003() {
        // Arrange
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(20)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
                () -> cartService.updateCartItemQuantity(userId, cartItemId, request));

        assertEquals(RestApiError.USR_0003, exception.getRestApiError());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updateCartItemQuantity_NegativeQuantity_ShouldThrowUSR0021() {
        // Arrange
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(-1)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
                () -> cartService.updateCartItemQuantity(userId, cartItemId, request));

        assertEquals(RestApiError.USR_0021, exception.getRestApiError());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    // ====================================================================
    // removeItemFromCart
    // ====================================================================

    @Test
    void removeItemFromCart_ValidRequest_ShouldDeleteItem() {
        // Arrange
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // Act
        cartService.removeItemFromCart(userId, cartItemId);

        // Assert
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    void removeItemFromCart_CartItemNotFound_ShouldThrowUSR0004() {
        // Arrange
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
                () -> cartService.removeItemFromCart(userId, cartItemId));

        assertEquals(RestApiError.USR_0004, exception.getRestApiError());
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void removeItemFromCart_UnauthorizedUser_ShouldThrowUSR0006() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // Act & Assert
        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class,
                () -> cartService.removeItemFromCart(otherUserId, cartItemId));

        assertEquals(RestApiError.USR_0006, exception.getRestApiError());
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }
}
