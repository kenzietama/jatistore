package com.indivaragroup.jatistore.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "mst_sellers")
@Data
public class Seller {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "cached_available_balance", nullable = false)
    private BigDecimal cachedAvailableBalance;

    @Column(name = "cached_on_hold_balance", nullable = false)
    private BigDecimal cachedOnHoldBalance;

    @Column(nullable = false)
    private Boolean active;
}
