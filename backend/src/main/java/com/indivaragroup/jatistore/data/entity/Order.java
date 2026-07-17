package com.indivaragroup.jatistore.data.entity;

import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.data.utility.table.schema.OrderVariable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = OrderVariable.TABLE_TRX_ORDERS)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = OrderVariable.COLUMN_TRX_ORDERS_ID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrderVariable.COLUMN_TRX_ORDERS_USER_ID, nullable = false)
    private User user;

    @Column(name = OrderVariable.COLUMN_TRX_ORDERS_TOTAL_AMOUNT, nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = OrderVariable.COLUMN_TRX_ORDERS_STATUS, nullable = false)
    private OrderStatus status;

    @Column(name = OrderVariable.COLUMN_TRX_ORDERS_CREATED_AT, nullable = false)
    private Instant createdAt;

    @Column(name = OrderVariable.COLUMN_TRX_ORDERS_UPDATED_AT, nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
