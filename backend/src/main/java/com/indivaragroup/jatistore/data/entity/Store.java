package com.indivaragroup.jatistore.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "mst_stores")
@Data
public class Store {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "store_name", nullable = false, unique = true)
    private String storeName;

    @Column(name = "image")
    private String image;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false, unique = true)
    private Seller seller;
}
