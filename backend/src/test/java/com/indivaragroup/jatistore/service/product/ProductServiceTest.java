package com.indivaragroup.jatistore.service.product;

import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.dto.response.module.product.ProductListItemResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductList_ShouldReturnPageData() {
        Object[] row = new Object[]{
                UUID.randomUUID(), "Test Product", "Test Store", "Desc",
                new BigDecimal("10000"), new BigDecimal("15000"), 10, true,
                Instant.now(), "image.jpg", UUID.randomUUID().toString()
        };
        Page<Object[]> page = new PageImpl<>(Collections.singletonList(row));
        when(productRepository.findProductsWithFlashSale(any(), any(), any())).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);
        PageData<ProductListItemResponse> result = productService.getProductList("search", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).getName());
        assertEquals("Test Store", result.getContent().get(0).getStoreName());
        assertTrue(result.getContent().get(0).getIsFlashSale());
    }
    
    @Test
    void getProductList_NullCategory() {
        Object[] row = new Object[]{
                UUID.randomUUID(), "Test Product", "Test Store", "Desc",
                new BigDecimal("10000"), new BigDecimal("15000"), 10, true,
                Instant.now(), "image.jpg", null
        };
        Page<Object[]> page = new PageImpl<>(Collections.singletonList(row));
        when(productRepository.findProductsWithFlashSale(any(), any(), any())).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);
        PageData<ProductListItemResponse> result = productService.getProductList("search", null, pageable);

        assertNotNull(result);
        assertNull(result.getContent().get(0).getCategoryId());
    }

    @Test
    void getProductDetailWithFlashSale_ShouldReturnDetail_WhenActive() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setName("Detail Product");
        product.setPrice(new BigDecimal("15000"));
        
        Store store = new Store();
        store.setStoreName("My Store");
        Seller seller = new Seller();
        seller.setActive(true);
        store.setSeller(seller);
        product.setStore(store);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));
        
        Object[] flashRow = new Object[]{
                new BigDecimal("10000"), new BigDecimal("15000"), true, Instant.now()
        };
        when(productRepository.getFlashSaleDetailInfo(productId)).thenReturn(List.<Object[]>of(flashRow));

        ProductListItemResponse result = productService.getProductDetailWithFlashSale(productId);

        assertNotNull(result);
        assertEquals("Detail Product", result.getName());
        assertEquals(new BigDecimal("10000"), result.getPrice());
        assertTrue(result.getIsFlashSale());
    }
    
    @Test
    void getProductDetailWithFlashSale_NoFlashSale_ShouldReturnDetail() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setName("Detail Product");
        product.setPrice(new BigDecimal("15000"));
        
        Store store = new Store();
        store.setStoreName("My Store");
        Seller seller = new Seller();
        seller.setActive(true);
        store.setSeller(seller);
        product.setStore(store);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));
        when(productRepository.getFlashSaleDetailInfo(productId)).thenReturn(Collections.emptyList());

        ProductListItemResponse result = productService.getProductDetailWithFlashSale(productId);

        assertNotNull(result);
        assertEquals(new BigDecimal("15000"), result.getPrice());
        assertFalse(result.getIsFlashSale());
    }

    @Test
    void getProductDetailWithFlashSale_ShouldThrow_WhenNotFound() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> productService.getProductDetailWithFlashSale(productId));
    }

    @Test
    void getProductDetailWithFlashSale_ShouldThrow_WhenSellerInactive() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        Store store = new Store();
        Seller seller = new Seller();
        seller.setActive(false);
        store.setSeller(seller);
        product.setStore(store);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));

        assertThrows(ResponseStatusException.class, () -> productService.getProductDetailWithFlashSale(productId));
    }
}
