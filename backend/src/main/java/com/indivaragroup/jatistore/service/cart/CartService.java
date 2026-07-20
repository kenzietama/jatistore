package com.indivaragroup.jatistore.service.cart;

import com.indivaragroup.jatistore.data.entity.Cart;
import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.dto.request.cart.AddToCartRequest;
import com.indivaragroup.jatistore.dto.request.cart.UpdateCartItemQuantityRequest;
import com.indivaragroup.jatistore.dto.response.cart.CartItemResponse;
import com.indivaragroup.jatistore.dto.response.cart.CartResponse;
import com.indivaragroup.jatistore.repository.CartItemRepository;
import com.indivaragroup.jatistore.repository.CartRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartItem addToCart(UUID userId, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0001));

        if (product.getStock() < request.getQuantity()) {
            throw new CoreThrowHandler(RestApiError.USR_0003);
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setId(UUID.randomUUID());
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setId(UUID.randomUUID());
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
        } else {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new CoreThrowHandler(RestApiError.USR_0003);
            }
            cartItem.setQuantity(newQuantity);
        }

        return cartItemRepository.save(cartItem);
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return CartResponse.builder()
                    .items(new ArrayList<>())
                    .totalPrice(BigDecimal.ZERO)
                    .build();
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            BigDecimal currentPrice = product.getPrice();
            BigDecimal subtotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            CartItemResponse response = CartItemResponse.builder()
                    .id(item.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImage())
                    .unitPrice(currentPrice)
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .maxStock(product.getStock())
                    .storeId(product.getStore() != null ? product.getStore().getId() : null)
                    .storeName(product.getStore() != null ? product.getStore().getStoreName() : null)
                    .build();

            itemResponses.add(response);
            totalPrice = totalPrice.add(subtotal);
        }

        return CartResponse.builder()
                .items(itemResponses)
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional
    public void updateCartItemQuantity(UUID userId, UUID cartItemId, UpdateCartItemQuantityRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0004));

        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new CoreThrowHandler(RestApiError.USR_0006);
        }

        if (request.getQuantity() == 0) {
            cartItemRepository.delete(cartItem);
            return;
        }

        if (request.getQuantity() < 0) {
            throw new CoreThrowHandler(RestApiError.USR_0021);
        }

        Product product = cartItem.getProduct();
        if (product.getStock() < request.getQuantity()) {
            throw new CoreThrowHandler(RestApiError.USR_0003);
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeItemFromCart(UUID userId, UUID cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0004));

        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new CoreThrowHandler(RestApiError.USR_0006);
        }

        cartItemRepository.delete(cartItem);
    }
}