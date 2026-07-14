package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import com.indivaragroup.jatistore.dto.request.seller.ProductCreateRequest;
import com.indivaragroup.jatistore.dto.request.seller.ProductUpdateRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.ProductResponse;
import com.indivaragroup.jatistore.dto.response.utility.ApiResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.service.seller.SellerProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductService sellerProductService;
    private final AuthRepository authRepository;
    private final SellerRepository sellerRepository;

    private UUID getSellerIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = authRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a registered seller"));
        return seller.getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageData<ProductResponse>>> getProducts(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        
        Page<ProductResponse> products = sellerProductService.getProducts(getSellerIdFromPrincipal(principal), search, category, status, sortBy, sortDir, page, size);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully.", PageData.from(products)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(Principal principal, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully.", sellerProductService.getProduct(getSellerIdFromPrincipal(principal), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> createProduct(Principal principal, @Valid @RequestBody ProductCreateRequest request) throws Exception {
        ProductResponse product = sellerProductService.createProduct(getSellerIdFromPrincipal(principal), request);
        return ResponseEntity.status(201).body(ApiResponse.success("201", "Product created successfully.", Map.of("productId", product.getId().toString())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(Principal principal, @PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) throws Exception {
        sellerProductService.updateProduct(getSellerIdFromPrincipal(principal), id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully.", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(Principal principal, @PathVariable UUID id) {
        sellerProductService.deleteProduct(getSellerIdFromPrincipal(principal), id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully.", null));
    }
}
