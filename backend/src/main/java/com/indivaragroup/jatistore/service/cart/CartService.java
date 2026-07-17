package com.indivaragroup.jatistore.service.cart;

import com.indivaragroup.jatistore.data.entity.Cart;
import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.dto.request.cart.AddToCartRequest;
import com.indivaragroup.jatistore.repository.CartItemRepository;
import com.indivaragroup.jatistore.repository.CartRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produk tidak ditemukan"));
        if (product.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stok produk tidak mencukupi");
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total barang di keranjang melebihi stok");
            }
            cartItem.setQuantity(newQuantity);
        }
        return cartItemRepository.save(cartItem);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartByUserId(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return new ArrayList<>();
        }
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Transactional
    public void removeItemFromCart(UUID userId, UUID cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item keranjang tidak ditemukan"));

        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Anda tidak memiliki akses untuk menghapus item ini");
        }
        cartItemRepository.delete(cartItem);
    }
}