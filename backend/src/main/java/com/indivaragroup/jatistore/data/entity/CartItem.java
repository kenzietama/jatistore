package com.indivaragroup.jatistore.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = CartItemVariable.COLUMN_TRX_CART_ITEMS_CART_ID, nullable = false)
    @JsonIgnoreProperties("items")
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = CartItemVariable.COLUMN_TRX_CART_ITEMS_PRODUCT_ID, nullable = false)
    private Product product;

    @Column(name = CartItemVariable.COLUMN_TRX_CART_ITEMS_QUANTITY, nullable = false)
    private Integer quantity;
}