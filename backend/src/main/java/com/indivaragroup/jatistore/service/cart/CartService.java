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
import com.indivaragroup.jatistore.audit.Audit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Audit(action = "CART_ADD_ITEM", affectedModule = "CART", description = "User added item to cart")
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

        List<Object[]> rawItems = cartItemRepository.getCartItemsWithFlashSale(cart.getId());
        BigDecimal totalPrice = BigDecimal.ZERO;

        List<CartItemResponse> itemResponses = rawItems.stream().map(row -> {
            UUID cartItemId = (UUID) row[0];
            UUID productId = (UUID) row[1];
            String productName = (String) row[2];
            String productImage = (String) row[3];
            BigDecimal unitPrice = (BigDecimal) row[4];
            BigDecimal originalPrice = (BigDecimal) row[5];
            Integer quantity = (Integer) row[6];
            Integer maxStock = (Integer) row[7];
            UUID storeId = (UUID) row[8];
            String storeName = (String) row[9];

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            return CartItemResponse.builder()
                    .id(cartItemId)
                    .productId(productId)
                    .productName(productName)
                    .productImage(productImage)
                    .unitPrice(unitPrice)
                    .originalPrice(originalPrice)
                    .quantity(quantity)
                    .subtotal(subtotal)
                    .maxStock(maxStock)
                    .storeId(storeId)
                    .storeName(storeName)
                    .build();
        }).collect(Collectors.toList());

        for (CartItemResponse item : itemResponses) {
            totalPrice = totalPrice.add(item.getSubtotal());
        }

        return CartResponse.builder()
                .items(itemResponses)
                .totalPrice(totalPrice)
                .build();
    }

    @Audit(action = "CART_UPDATE_ITEM", affectedModule = "CART", description = "User updated cart item quantity")
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

    @Audit(action = "CART_REMOVE_ITEM", affectedModule = "CART", description = "User removed item from cart")
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