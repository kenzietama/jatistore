package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.request.seller.ProductCreateRequest;
import com.indivaragroup.jatistore.dto.request.seller.ProductUpdateRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.ProductResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.seller.SellerProductService;
import com.indivaragroup.jatistore.service.seller.SellerSecurityHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.SELLER_PRODUCTS_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerProductController {

    private final SellerProductService sellerProductService;
    private final SellerSecurityHelper securityHelper;

    @GetMapping
    public RestApiResponse<PageData<ProductResponse>> getProducts(
            Principal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) throws CoreThrowHandler {
        
        Page<ProductResponse> products = sellerProductService.getProducts(securityHelper.getSellerIdFromPrincipal(principal), search, category, status, sortBy, sortDir, page, size);
        return RestApiResponse.success(PageData.from(products));
    }

    @GetMapping("/{id}")
    public RestApiResponse<ProductResponse> getProduct(Principal principal, @PathVariable UUID id) throws CoreThrowHandler {
        return RestApiResponse.success(sellerProductService.getProduct(securityHelper.getSellerIdFromPrincipal(principal), id));
    }

    @PostMapping
    public RestApiResponse<Map<String, String>> createProduct(Principal principal, @Valid @RequestBody ProductCreateRequest request) throws CoreThrowHandler {
        ProductResponse product = sellerProductService.createProduct(securityHelper.getSellerIdFromPrincipal(principal), request);
        return RestApiResponse.success(Map.of("productId", product.getId().toString()));
    }

    @PatchMapping("/{id}")
    public RestApiResponse<Void> updateProduct(Principal principal, @PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) throws CoreThrowHandler {
        sellerProductService.updateProduct(securityHelper.getSellerIdFromPrincipal(principal), id, request);
        return RestApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public RestApiResponse<Void> deleteProduct(Principal principal, @PathVariable UUID id) throws CoreThrowHandler {
        sellerProductService.deleteProduct(securityHelper.getSellerIdFromPrincipal(principal), id);
        return RestApiResponse.success(null);
    }
}
