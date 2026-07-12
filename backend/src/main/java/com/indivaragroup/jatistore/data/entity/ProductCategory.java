package com.indivaragroup.jatistore.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "mst_product_categories")
@Data
public class ProductCategory {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;
}
