package com.indivaragroup.jatistore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.indivaragroup.jatistore.service.product.ProductService;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
public class TempTest {
    @Autowired
    private ProductService productService;

    @Test
    public void test() {
        try {
            productService.getProductList(null, null, PageRequest.of(0, 20));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
