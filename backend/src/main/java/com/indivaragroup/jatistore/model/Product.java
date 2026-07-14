package com.indivaragroup.jatistore.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    private long id;

    private String name;
    private BigDecimal price;
    private Integer stock;
}
