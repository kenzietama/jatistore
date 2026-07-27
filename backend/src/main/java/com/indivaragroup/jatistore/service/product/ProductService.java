package com.indivaragroup.jatistore.service.product;

import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.dto.response.module.product.ProductListItemResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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

    @Transactional(readOnly = true)
    public ProductListItemResponse getProductDetailWithFlashSale(UUID productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or deleted"));

        if (!product.getStore().getSeller().getActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or deleted");
        }

        // Ambil info harga & flash sale tambahan
        List<Object[]> flashInfoList = productRepository.getFlashSaleDetailInfo(productId);

        BigDecimal finalPrice = product.getPrice();
        BigDecimal originalPrice = null;
        Boolean isFlashSale = false;
        Instant flashSaleEndTime = null;
        Integer remainingQuota = null;

        if (!flashInfoList.isEmpty() && flashInfoList.get(0) != null) {
            Object[] row = flashInfoList.get(0);
            finalPrice = row[0] != null ? (BigDecimal) row[0] : product.getPrice();
            originalPrice = (BigDecimal) row[1];
            isFlashSale = row[2] != null && (Boolean) row[2];
            flashSaleEndTime = (Instant) row[3];
            remainingQuota = (Integer) row[4];
        }

        return ProductListItemResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .storeName(product.getStore() != null ? product.getStore().getStoreName() : null)
                .description(product.getDescription())
                .price(finalPrice)
                .originalPrice(originalPrice)
                .stock(product.getStock())
                .remainingQuota(remainingQuota)
                .isFlashSale(isFlashSale)
                .flashSaleEndTime(flashSaleEndTime)
                .image(product.getImage())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
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
                .remainingQuota((Integer) row[7])
                .isFlashSale((Boolean) row[8])
                .flashSaleEndTime((Instant) row[9])
                .image((String) row[10])
                .categoryId(row[11] != null ? UUID.fromString((String) row[11]) : null)
                .build();
    }
}