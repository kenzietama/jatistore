package com.indivaragroup.jatistore.data.entity;

import com.indivaragroup.jatistore.data.utility.table.schema.CartVariable;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = CartVariable.TABLE_TRX_CARTS)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @Column(name = CartVariable.COLUMN_TRX_CARTS_ID, nullable = false)
    private UUID id;

    @Column(name = CartVariable.COLUMN_TRX_CARTS_USER_ID, nullable = false, unique = true)
    private UUID userId;
}