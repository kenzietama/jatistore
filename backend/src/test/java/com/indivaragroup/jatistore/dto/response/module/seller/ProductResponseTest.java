package com.indivaragroup.jatistore.dto.response.module.seller;

import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.ProductCategory;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProductResponseTest {

    @Test
    void fromEntity_shouldMapAllFields_whenCategoryIsNotNull() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setStock(15);
        
        ProductCategory category = new ProductCategory();
        category.setId(UUID.randomUUID());
        category.setName("Elektronik");
        product.setCategory(category);

        ProductResponse response = ProductResponse.fromEntity(product);

        assertEquals("ACTIVE", response.getStatus());
        assertEquals(category.getId(), response.getCategoryId());
        assertEquals(category.getName(), response.getCategoryName());
    }

    @Test
    void fromEntity_shouldMapCategoryAsNull_whenCategoryIsNull() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setStock(5); // LOW STOCK
        product.setCategory(null);

        ProductResponse response = ProductResponse.fromEntity(product);

        assertEquals("LOW STOCK", response.getStatus());
        assertNull(response.getCategoryId());
        assertNull(response.getCategoryName());
    }

    @Test
    void fromEntity_shouldSetStatusToOutOfStock_whenStockIsNull() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setStock(null);

        ProductResponse response = ProductResponse.fromEntity(product);

        assertEquals("OUT OF STOCK", response.getStatus());
    }

    @Test
    void fromEntity_shouldSetStatusToOutOfStock_whenStockIsZeroOrLess() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setStock(0);

        ProductResponse response = ProductResponse.fromEntity(product);
        assertEquals("OUT OF STOCK", response.getStatus());
        
        product.setStock(-5);
        ProductResponse response2 = ProductResponse.fromEntity(product);
        assertEquals("OUT OF STOCK", response2.getStatus());
    }
}
