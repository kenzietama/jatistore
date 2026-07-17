package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.checkout.CartItem;
import com.indivaragroup.jatistore.data.entity.checkout.SellerLedger;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import com.indivaragroup.jatistore.repository.OrderDetailRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.repository.checkout.CartItemRepository;
import com.indivaragroup.jatistore.repository.checkout.SellerLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutFinalizer {

    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final SellerLedgerRepository sellerLedgerRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public void finalizeOrder(Order order, List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            detail.setPricePerItem(item.getProduct().getPrice());
            detail.setFlashSale(false);
            orderDetailRepository.save(detail);

            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        Seller seller = cartItems.get(0).getProduct().getStore().getSeller();
        SellerLedger ledger = SellerLedger.builder()
                .seller(seller)
                .order(order)
                .amount(order.getTotalAmount())
                .balanceType(BalanceType.ON_HOLD)
                .build();
        sellerLedgerRepository.save(ledger);

        List<UUID> cartItemIds = cartItems.stream().map(CartItem::getId).toList();
        cartItemRepository.deleteAllById(cartItemIds);
    }
}
