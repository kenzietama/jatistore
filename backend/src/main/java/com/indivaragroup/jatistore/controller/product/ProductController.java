package com.indivaragroup.jatistore.controller.product;

import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.PRODUCT_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductRepository productRepository;

    @GetMapping(RestApiPath.PRODUCT_LIST_PATH)
    public RestApiResponse<List<Product>> getProductList() {
        log.info("Menerima permintaan REST untuk mengambil seluruh daftar produk");
        List<Product> products = productRepository.findAll();
        return RestApiResponse.<List<Product>>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage("Daftar produk berhasil diambil!")
                .restApiResponseData(products)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId("REQ-PROD-" + System.currentTimeMillis())
                .build();
    }

    @GetMapping(RestApiPath.PRODUCT_DETAIL_PATH)
    public RestApiResponse<Product> getProductById(@PathVariable("id") UUID id) {
        log.info("Menerima permintaan REST untuk mengambil detail produk dengan ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produk tidak ditemukan"));

        return RestApiResponse.<Product>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage("Detail produk berhasil diambil!")
                .restApiResponseData(product)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId("REQ-PROD-DETAIL-" + System.currentTimeMillis())
                .build();
    }


}