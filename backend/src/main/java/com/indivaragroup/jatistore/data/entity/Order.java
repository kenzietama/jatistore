package com.indivaragroup.jatistore.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "trx_orders")
@Data
public class Order {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}
