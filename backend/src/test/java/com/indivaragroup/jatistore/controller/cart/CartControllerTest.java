package com.indivaragroup.jatistore.controller.cart;

import tools.jackson.databind.ObjectMapper;
import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.cart.AddToCartRequest;
import com.indivaragroup.jatistore.dto.request.cart.UpdateCartItemQuantityRequest;
import com.indivaragroup.jatistore.dto.response.cart.CartItemResponse;
import com.indivaragroup.jatistore.dto.response.cart.CartResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.cart.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private com.indivaragroup.jatistore.service.utility.AuthJWTUtility authJWTUtility;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Principal mockPrincipal;
    private UUID userId;
    private UUID productId;
    private UUID cartItemId;
    private CartItem cartItem;
    private CartResponse cartResponse;
    private CartItemResponse cartItemResponse;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "user@example.com";

        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartItemId = UUID.randomUUID();

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("user@example.com");

        when(authRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        cartItemResponse = CartItemResponse.builder()
                .id(cartItemId)
                .productId(productId)
                .productName("Test Product")
                .productImage("test.jpg")
                .unitPrice(new BigDecimal("100.00"))
                .quantity(2)
                .subtotal(new BigDecimal("200.00"))
                .maxStock(10)
                .build();

        cartResponse = CartResponse.builder()
                .items(List.of(cartItemResponse))
                .totalPrice(new BigDecimal("200.00"))
                .build();
    }

    // ====================================================================
    // GET /api/v1/cart
    // ====================================================================

    @Test
    void getCart_WithItems_ShouldReturnOkWithCartResponse() throws Exception {
        // Arrange
        when(cartService.getCartByUserId(any(UUID.class))).thenReturn(cartResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Cart retrieved successfully."))
                .andExpect(jsonPath("$.data.items[0].productName").value("Test Product"))
                .andExpect(jsonPath("$.data.totalPrice").value(200.00));

        verify(cartService, times(1)).getCartByUserId(any(UUID.class));
    }

    @Test
    void getCart_EmptyCart_ShouldReturnOkWithEmptyMessage() throws Exception {
        // Arrange
        CartResponse emptyCart = CartResponse.builder()
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .build();
        when(cartService.getCartByUserId(any(UUID.class))).thenReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(get("/api/v1/cart")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Your cart is empty."))
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.totalPrice").value(0));

        verify(cartService, times(1)).getCartByUserId(any(UUID.class));
    }

    // ====================================================================
    // POST /api/v1/cart/items
    // ====================================================================

    @Test
    void addToCart_ValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setQuantity(2);

        when(cartService.addToCart(any(UUID.class), any(AddToCartRequest.class)))
                .thenReturn(cartItem);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                        .principal(mockPrincipal)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Product added to cart successfully."))
                .andExpect(jsonPath("$.data").value(cartItemId.toString()));

        verify(cartService, times(1)).addToCart(any(UUID.class), any(AddToCartRequest.class));
    }

    @Test
    void addToCart_ProductNotFound_ShouldReturn404() throws Exception {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        when(cartService.addToCart(any(UUID.class), any(AddToCartRequest.class)))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0001));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                        .principal(mockPrincipal)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).addToCart(any(UUID.class), any(AddToCartRequest.class));
    }

    @Test
    void addToCart_InsufficientStock_ShouldReturn400() throws Exception {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(100)
                .build();

        when(cartService.addToCart(any(UUID.class), any(AddToCartRequest.class)))
                .thenThrow(new CoreThrowHandler(RestApiError.USR_0003));

        // Act & Assert
        mockMvc.perform(post("/api/v1/cart/items")
                        .principal(mockPrincipal)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).addToCart(any(UUID.class), any(AddToCartRequest.class));
    }

    // ====================================================================
    // PATCH /api/v1/cart/items/{cartItemId}
    // ====================================================================

    @Test
    void updateCartItemQuantity_ValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(5)
                .build();

        doNothing().when(cartService).updateCartItemQuantity(any(UUID.class), eq(cartItemId), any(UpdateCartItemQuantityRequest.class));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .principal(mockPrincipal)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart item updated successfully."));

        verify(cartService, times(1)).updateCartItemQuantity(any(UUID.class), eq(cartItemId), any(UpdateCartItemQuantityRequest.class));
    }

    @Test
    void updateCartItemQuantity_CartItemNotFound_ShouldReturn404() throws Exception {
        // Arrange
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(5)
                .build();

        doThrow(new CoreThrowHandler(RestApiError.USR_0004))
                .when(cartService).updateCartItemQuantity(any(UUID.class), eq(cartItemId), any(UpdateCartItemQuantityRequest.class));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .principal(mockPrincipal)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).updateCartItemQuantity(any(UUID.class), eq(cartItemId), any(UpdateCartItemQuantityRequest.class));
    }

    @Test
    void updateCartItemQuantity_InsufficientStock_ShouldReturn400() throws Exception {
        // Arrange
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder()
                .quantity(100)
                .build();

        doThrow(new CoreThrowHandler(RestApiError.USR_0003))
                .when(cartService).updateCartItemQuantity(any(UUID.class), eq(cartItemId), any(UpdateCartItemQuantityRequest.class));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .principal(mockPrincipal)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).updateCartItemQuantity(any(UUID.class), eq(cartItemId), any(UpdateCartItemQuantityRequest.class));
    }

    // ====================================================================
    // DELETE /api/v1/cart/items/{cartItemId}
    // ====================================================================

    @Test
    void removeItem_ValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(cartService).removeItemFromCart(any(UUID.class), eq(cartItemId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .principal(mockPrincipal)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart item removed successfully."));

        verify(cartService, times(1)).removeItemFromCart(any(UUID.class), eq(cartItemId));
    }

    @Test
    void removeItem_CartItemNotFound_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new CoreThrowHandler(RestApiError.USR_0004))
                .when(cartService).removeItemFromCart(any(UUID.class), eq(cartItemId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .principal(mockPrincipal)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).removeItemFromCart(any(UUID.class), eq(cartItemId));
    }

    @Test
    void removeItem_UnauthorizedUser_ShouldReturn401() throws Exception {
        // Arrange
        doThrow(new CoreThrowHandler(RestApiError.USR_0006))
                .when(cartService).removeItemFromCart(any(UUID.class), eq(cartItemId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/cart/items/{cartItemId}", cartItemId)
                        .principal(mockPrincipal)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartService, times(1)).removeItemFromCart(any(UUID.class), eq(cartItemId));
    }
}
