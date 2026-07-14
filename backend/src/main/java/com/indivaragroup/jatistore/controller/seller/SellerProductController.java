package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.request.seller.ProductCreateRequest;
import com.indivaragroup.jatistore.dto.request.seller.ProductUpdateRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.ProductResponse;
import com.indivaragroup.jatistore.dto.response.utility.ApiResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.service.seller.SellerProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductService sellerProductService;

    // Use mock seller ID for now, similar to SellerDashboardController
    private UUID getMockSellerId() {
        return UUID.fromString("bb000000-0000-0000-0000-000000000001");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageData<ProductResponse>>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        
        Page<ProductResponse> products = sellerProductService.getProducts(getMockSellerId(), search, category, status, sortBy, sortDir, page, size);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully.", PageData.from(products)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully.", sellerProductService.getProduct(getMockSellerId(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> createProduct(@Valid @RequestBody ProductCreateRequest request) throws Exception {
        ProductResponse product = sellerProductService.createProduct(getMockSellerId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success("201", "Product created successfully.", Map.of("productId", product.getId().toString())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) throws Exception {
        sellerProductService.updateProduct(getMockSellerId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully.", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        sellerProductService.deleteProduct(getMockSellerId(), id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully.", null));
    }
}
