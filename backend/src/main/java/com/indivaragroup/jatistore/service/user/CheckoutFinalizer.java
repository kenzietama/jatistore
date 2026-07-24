package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.data.entity.FlashSaleItem;
import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.SellerLedger;
import com.indivaragroup.jatistore.data.utility.constant.BalanceType;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.CartItemRepository;
import com.indivaragroup.jatistore.repository.FlashSaleItemRepository;
import com.indivaragroup.jatistore.repository.OrderDetailRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.repository.SellerLedgerRepository;
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
    private final FlashSaleItemRepository flashSaleItemRepository;

    @Transactional(noRollbackFor = CoreThrowHandler.class)
    public void finalizeOrder(Order order, List<CartItem> cartItems) {
        // 1. Decrement flash sale quota with pessimistic locking
        for (CartItem item : cartItems) {
            List<OrderDetail> flashSaleDetails = (order.getOrderDetails() != null) ? order.getOrderDetails().stream()
                    .filter(od -> od.getProduct().getId().equals(item.getProduct().getId())
                            && Boolean.TRUE.equals(od.getFlashSale()))
                    .toList() : List.of();

            if (!flashSaleDetails.isEmpty()) {
                OrderDetail detail = flashSaleDetails.getFirst();
                FlashSaleItem flashSaleItem = flashSaleItemRepository
                        .findByProductAndActiveFlashSaleForUpdate(detail.getProduct().getId())
                        .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0024));

                if (flashSaleItem.getRemainingQuota() < detail.getQuantity()) {
                    throw new CoreThrowHandler(RestApiError.USR_0025);
                }

                flashSaleItem.setRemainingQuota(
                        flashSaleItem.getRemainingQuota() - detail.getQuantity()
                );
                flashSaleItemRepository.save(flashSaleItem);
            }
        }

        // 2. Deduct product stock
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // 3. Create seller ledger
        Seller seller = cartItems.getFirst().getProduct().getStore().getSeller();
        SellerLedger ledger = SellerLedger.builder()
                .seller(seller)
                .order(order)
                .amount(order.getTotalAmount())
                .balanceType(BalanceType.ON_HOLD)
                .build();
        sellerLedgerRepository.save(ledger);

        // 4. Clear cart items
        List<UUID> cartItemIds = cartItems.stream().map(CartItem::getId).toList();
        cartItemRepository.deleteAllById(cartItemIds);
    }
}
