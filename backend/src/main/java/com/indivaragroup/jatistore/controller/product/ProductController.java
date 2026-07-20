package com.indivaragroup.jatistore.controller.product;

import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.product.ProductListItemResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.PRODUCT_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductService productService;

    @GetMapping(value = RestApiPath.PRODUCT_LIST_PATH, params = {"!id"})
    public RestApiResponse<PageData<ProductListItemResponse>> getProductList(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching product list - search: {}, page: {}, size: {}", search, page, size);

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or size parameter");
        }

        Pageable pageable = PageRequest.of(page, size);
        PageData<ProductListItemResponse> pageData = productService.getProductList(search, pageable);

        return RestApiResponse.<PageData<ProductListItemResponse>>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Products retrieved successfully.")
                .restApiResponseData(pageData)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }

    @GetMapping(RestApiPath.PRODUCT_DETAIL_PATH)
    public RestApiResponse<Product> getProductById(@PathVariable("id") UUID id) {
        log.info("Fetching product detail for ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        return RestApiResponse.<Product>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage("Product detail retrieved successfully.")
                .restApiResponseData(product)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }


}