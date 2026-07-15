package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.dto.request.seller.ProductCreateRequest;
import com.indivaragroup.jatistore.dto.request.seller.ProductUpdateRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.ProductResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface SellerProductService {
    Page<ProductResponse> getProducts(UUID sellerId, String search, String category, String status, String sortBy, String sortDir, int page, int limit) throws CoreThrowHandler;
    ProductResponse getProduct(UUID sellerId, UUID id) throws CoreThrowHandler;
    ProductResponse createProduct(UUID sellerId, ProductCreateRequest request) throws CoreThrowHandler;
    ProductResponse updateProduct(UUID sellerId, UUID id, ProductUpdateRequest request) throws CoreThrowHandler;
    void deleteProduct(UUID sellerId, UUID id) throws CoreThrowHandler;
}
