package com.indivaragroup.jatistore.controller.category;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.indivaragroup.jatistore.repository.ProductCategoryRepository;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController // WAJIB ADA
@RequestMapping("/api/v1/categories") // WAJIB ADA
public class ProductCategoryController {

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", productCategoryRepository.findAll());
        return ResponseEntity.ok(response);
    }
}