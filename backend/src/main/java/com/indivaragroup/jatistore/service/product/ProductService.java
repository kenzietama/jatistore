package com.indivaragroup.jatistore.service.product;

import com.indivaragroup.jatistore.dto.response.module.product.ProductListItemResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public PageData<ProductListItemResponse> getProductList(String search, String categoryId, Pageable pageable) {
        Page<Object[]> resultPage = productRepository.findProductsWithFlashSale(search, categoryId, pageable);

        List<ProductListItemResponse> content = resultPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageData.<ProductListItemResponse>builder()
                .content(content)
                .page(resultPage.getNumber())
                .size(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .build();
    }

    private ProductListItemResponse mapToResponse(Object[] row) {
        return ProductListItemResponse.builder()
                .id((UUID) row[0])
                .name((String) row[1])
                .storeName((String) row[2])
                .description((String) row[3])
                .price((BigDecimal) row[4])
                .originalPrice((BigDecimal) row[5])
                .stock((Integer) row[6])
                .isFlashSale((Boolean) row[7])
                .flashSaleEndTime((Instant) row[8])
                .image((String) row[9])
                .categoryId(row[10] != null ? UUID.fromString((String) row[10]) : null)
                .build();
    }
}
