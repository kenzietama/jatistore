package com.indivaragroup.jatistore.data.entity;

import com.indivaragroup.jatistore.data.utility.table.schema.CartVariable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
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
    @GeneratedValue
    @UuidGenerator
    @Column(name = CartVariable.COLUMN_TRX_CARTS_ID, nullable = false)
    private UUID id;

    @Column(name = CartVariable.COLUMN_TRX_CARTS_USER_ID, nullable = false, unique = true)
    private UUID userId;

    @Column(name = CartVariable.COLUMN_TRX_CARTS_CREATED_AT, nullable = false)
    private Instant createdAt;

    @Column(name = CartVariable.COLUMN_TRX_CARTS_UPDATED_AT, nullable = false)
    private Instant updatedAt;
}