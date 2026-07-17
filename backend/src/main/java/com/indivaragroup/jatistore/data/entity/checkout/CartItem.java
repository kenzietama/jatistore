package com.indivaragroup.jatistore.data.entity.checkout;

import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.utility.table.schema.CartItemVariable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = CartItemVariable.TABLE_TRX_CART_ITEMS)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = CartItemVariable.COLUMN_TRX_CART_ITEMS_ID, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = CartItemVariable.COLUMN_TRX_CART_ITEMS_CART_ID, nullable = false)
    private CartItem cartItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = CartItemVariable.COLUMN_TRX_CART_ITEMS_PRODUCT_ID, nullable = false)
    private Product product;

    @Column(name = CartItemVariable.COLUMN_TRX_CART_ITEMS_QUANTITY, nullable = false)
    private int quantity;
}
