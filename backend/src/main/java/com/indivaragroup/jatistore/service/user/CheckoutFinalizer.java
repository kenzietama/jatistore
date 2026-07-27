package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.SellerLedger;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.CartItemRepository;
import com.indivaragroup.jatistore.repository.SellerLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutFinalizer {

    private final SellerLedgerRepository sellerLedgerRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional(noRollbackFor = CoreThrowHandler.class)
    public void finalizeOrder(Order order, List<CartItem> cartItems) {
        // 1. Create seller ledger
        Seller seller = cartItems.getFirst().getProduct().getStore().getSeller();
        SellerLedger ledger = SellerLedger.builder()
                .seller(seller)
                .order(order)
                .amount(order.getTotalAmount())
                .balanceType(BalanceType.ON_HOLD)
                .build();
        sellerLedgerRepository.save(ledger);

        // 2. Clear cart items
        List<UUID> cartItemIds = cartItems.stream().map(CartItem::getId).toList();
        cartItemRepository.deleteAllById(cartItemIds);
    }
}
