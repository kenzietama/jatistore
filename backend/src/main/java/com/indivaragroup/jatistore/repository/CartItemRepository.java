package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    List<CartItem> findByCartId(UUID cartId);
}