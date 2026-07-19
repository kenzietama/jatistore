package com.indivaragroup.jatistore.dto.response.module.seller;

import com.indivaragroup.jatistore.data.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private Integer stock;
    private UUID categoryId;
    private String categoryName;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setImage(product.getImage());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        if (product.getStock() == null || product.getStock() <= 0) {
            response.setStatus("OUT OF STOCK");
        } else if (product.getStock() <= 10) {
            response.setStatus("LOW STOCK");
        } else {
            response.setStatus("ACTIVE");
        }

        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
