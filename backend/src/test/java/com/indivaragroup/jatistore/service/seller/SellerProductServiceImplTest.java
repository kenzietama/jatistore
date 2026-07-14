package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.ProductCategory;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.dto.request.seller.ProductCreateRequest;
import com.indivaragroup.jatistore.dto.request.seller.ProductUpdateRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.ProductResponse;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.ProductCategoryRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import com.indivaragroup.jatistore.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductCategoryRepository productCategoryRepository;
    @Mock
    private AuthRepository authRepository;
    @Mock
    private SellerRepository sellerRepository;
    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private SellerProductServiceImpl sellerProductService;

    private UUID sellerId;
    private UUID productId;
    private Seller mockSeller;
    private Store mockStore;
    private ProductCategory mockCategory;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        mockSeller = new Seller();
        mockSeller.setId(sellerId);

        mockStore = new Store();
        mockStore.setId(UUID.randomUUID());
        mockStore.setSeller(mockSeller);

        mockCategory = new ProductCategory();
        mockCategory.setId(UUID.randomUUID());
        mockCategory.setName("Electronics");

        mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setStore(mockStore);
        mockProduct.setCategory(mockCategory);
        mockProduct.setName("Laptop");
        mockProduct.setPrice(new BigDecimal("1000.00"));
        mockProduct.setStock(10);
        mockProduct.setCreatedAt(ZonedDateTime.now());
        mockProduct.setUpdatedAt(ZonedDateTime.now());
    }

    // ==========================================
    // GET PRODUCTS
    // ==========================================

    @Test
    void getProducts_shouldReturnPageOfProducts() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        
        Page<Product> page = new PageImpl<>(Arrays.asList(mockProduct));
        // We will mock the repository call for filter OUT_OF_STOCK
        when(productRepository.findProductsBySellerAndFilters(eq(sellerId), any(), any(), eq(0), eq(0), any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<ProductResponse> result = sellerProductService.getProducts(sellerId, null, null, "OUT_OF_STOCK", "price", "desc", 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
    }

    @Test
    void getProducts_shouldReturnPageOfProducts_withFiltersAndSorts() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        Page<Product> page = new PageImpl<>(Arrays.asList(mockProduct));
        
        // Mock multiple calls for different filters
        when(productRepository.findProductsBySellerAndFilters(eq(sellerId), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // Act - OUT_OF_STOCK
        Page<ProductResponse> result1 = sellerProductService.getProducts(sellerId, "query", "cat1", "OUT_OF_STOCK", "price", "asc", 0, 10);
        assertNotNull(result1);

        // Act - OUT OF STOCK (Space)
        Page<ProductResponse> result1b = sellerProductService.getProducts(sellerId, null, null, "OUT OF STOCK", null, null, 0, 10);
        assertNotNull(result1b);

        // Act - LOW_STOCK
        Page<ProductResponse> result2 = sellerProductService.getProducts(sellerId, null, null, "LOW_STOCK", "stock", "desc", 0, 10);
        assertNotNull(result2);

        // Act - LOW STOCK (Space)
        Page<ProductResponse> result2b = sellerProductService.getProducts(sellerId, null, null, "LOW STOCK", null, null, 0, 10);
        assertNotNull(result2b);

        // Act - ACTIVE
        Page<ProductResponse> result3 = sellerProductService.getProducts(sellerId, null, null, "ACTIVE", "name", null, 0, 10);
        assertNotNull(result3);
        
        // Act - Invalid sort property
        Page<ProductResponse> result4 = sellerProductService.getProducts(sellerId, null, null, null, "invalid", null, 0, 10);
        assertNotNull(result4);

        // Act - Empty status, empty sortBy
        Page<ProductResponse> result5 = sellerProductService.getProducts(sellerId, null, null, "", "", null, 0, 10);
        assertNotNull(result5);

        // Act - Unrecognized status
        Page<ProductResponse> result6 = sellerProductService.getProducts(sellerId, null, null, "UNKNOWN_STATUS", null, null, 0, 10);
        assertNotNull(result6);
    }

    @Test
    void getProducts_shouldThrowException_whenSellerNotFound() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            sellerProductService.getProducts(sellerId, null, null, null, null, null, 0, 10);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    // ==========================================
    // GET PRODUCT
    // ==========================================

    @Test
    void getProduct_shouldReturnProduct_whenValid() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act
        ProductResponse response = sellerProductService.getProduct(sellerId, productId);

        // Assert
        assertNotNull(response);
        assertEquals("Laptop", response.getName());
    }

    @Test
    void getProduct_shouldThrowException_whenProductNotFound() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            sellerProductService.getProduct(sellerId, productId);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Test
    void getProduct_shouldThrowException_whenProductBelongsToOtherSeller() {
        // Arrange
        Seller otherSeller = new Seller();
        otherSeller.setId(UUID.randomUUID());
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(otherSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        try {
            sellerProductService.getProduct(sellerId, productId);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Test
    void getProduct_shouldThrowException_whenProductSoftDeleted() {
        // Arrange
        mockProduct.setDeletedAt(ZonedDateTime.now());
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        try {
            sellerProductService.getProduct(sellerId, productId);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    // ==========================================
    // CREATE PRODUCT
    // ==========================================

    @Test
    void createProduct_shouldSaveAndReturnProduct() throws Exception {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(storeRepository.findBySellerId(sellerId)).thenReturn(Optional.of(mockStore));
        when(productCategoryRepository.findById(mockCategory.getId())).thenReturn(Optional.of(mockCategory));

        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("New PC");
        request.setDescription("Good PC");
        request.setPrice(new BigDecimal("2000.00"));
        request.setStock(5);
        request.setImage("pc.jpg");
        request.setProductCategoryId(mockCategory.getId());

        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        // Act
        ProductResponse response = sellerProductService.createProduct(sellerId, request);

        // Assert
        assertNotNull(response);
        assertEquals("New PC", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowException_whenStoreNotFound() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(storeRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        ProductCreateRequest request = new ProductCreateRequest();

        // Act & Assert
        try {
            sellerProductService.createProduct(sellerId, request);
            fail("Expected ResponseStatusException");
        } catch (Exception ex) {
            assertTrue(ex instanceof ResponseStatusException);
            assertEquals(HttpStatus.BAD_REQUEST, ((ResponseStatusException) ex).getStatusCode());
        }
    }

    @Test
    void createProduct_shouldThrowException_whenCategoryNotFound() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(storeRepository.findBySellerId(sellerId)).thenReturn(Optional.of(mockStore));
        when(productCategoryRepository.findById(any())).thenReturn(Optional.empty());

        ProductCreateRequest request = new ProductCreateRequest();
        request.setProductCategoryId(UUID.randomUUID());

        // Act & Assert
        try {
            sellerProductService.createProduct(sellerId, request);
            fail("Expected ResponseStatusException");
        } catch (Exception ex) {
            assertTrue(ex instanceof ResponseStatusException);
            assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) ex).getStatusCode());
        }
    }

    // ==========================================
    // UPDATE PRODUCT
    // ==========================================

    @Test
    void updateProduct_shouldUpdateFieldsAndSave() throws Exception {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        ProductCategory newCategory = new ProductCategory();
        newCategory.setId(UUID.randomUUID());
        when(productCategoryRepository.findById(newCategory.getId())).thenReturn(Optional.of(newCategory));

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated Laptop");
        request.setDescription("Updated description");
        request.setPrice(new BigDecimal("1200.00"));
        request.setStock(20);
        request.setImage("newimage.jpg");
        request.setProductCategoryId(newCategory.getId());

        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        ProductResponse response = sellerProductService.updateProduct(sellerId, productId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Laptop", mockProduct.getName()); // entity mutated
        assertEquals("Updated description", mockProduct.getDescription());
        assertEquals(new BigDecimal("1200.00"), mockProduct.getPrice());
        assertEquals(20, mockProduct.getStock());
        assertEquals("newimage.jpg", mockProduct.getImage());
        verify(productRepository, times(1)).save(mockProduct);
    }

    @Test
    void updateProduct_shouldThrowException_whenCategoryNotFound() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(productCategoryRepository.findById(any())).thenReturn(Optional.empty());

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setProductCategoryId(UUID.randomUUID());

        // Act & Assert
        try {
            sellerProductService.updateProduct(sellerId, productId, request);
            fail("Expected ResponseStatusException");
        } catch (Exception ex) {
            assertTrue(ex instanceof ResponseStatusException);
            assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) ex).getStatusCode());
        }
    }

    @Test
    void updateProduct_shouldNotUpdateFields_whenFieldsAreNull() throws Exception {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        ProductUpdateRequest request = new ProductUpdateRequest();
        // everything null or empty
        request.setImage("");

        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        ProductResponse response = sellerProductService.updateProduct(sellerId, productId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Laptop", mockProduct.getName()); // entity NOT mutated
        assertEquals(new BigDecimal("1000.00"), mockProduct.getPrice());
        verify(productRepository, times(1)).save(mockProduct);
    }

    @Test
    void updateProduct_shouldNotUpdateImage_whenImageIsNull() throws Exception {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setImage(null);

        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        ProductResponse response = sellerProductService.updateProduct(sellerId, productId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Laptop", mockProduct.getName()); 
        verify(productRepository, times(1)).save(mockProduct);
    }

    @Test
    void updateProduct_shouldThrowException_whenProductNotFound() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ProductUpdateRequest request = new ProductUpdateRequest();

        // Act & Assert
        try {
            sellerProductService.updateProduct(sellerId, productId, request);
            fail("Expected ResponseStatusException");
        } catch (Exception ex) {
            assertTrue(ex instanceof ResponseStatusException);
            assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) ex).getStatusCode());
        }
    }

    @Test
    void updateProduct_shouldThrowException_whenProductBelongsToOtherSeller() throws Exception {
        // Arrange
        Seller otherSeller = new Seller();
        otherSeller.setId(UUID.randomUUID());
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(otherSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        try {
            sellerProductService.updateProduct(sellerId, productId, new ProductUpdateRequest());
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Test
    void updateProduct_shouldThrowException_whenProductSoftDeleted() throws Exception {
        // Arrange
        mockProduct.setDeletedAt(ZonedDateTime.now());
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        try {
            sellerProductService.updateProduct(sellerId, productId, new ProductUpdateRequest());
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    // ==========================================
    // DELETE PRODUCT
    // ==========================================

    @Test
    void deleteProduct_shouldSetDeletedAtAndSave() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act
        sellerProductService.deleteProduct(sellerId, productId);

        // Assert
        assertNotNull(mockProduct.getDeletedAt());
        verify(productRepository, times(1)).save(mockProduct);
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductNotFound() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            sellerProductService.deleteProduct(sellerId, productId);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductBelongsToOtherSeller() {
        // Arrange
        Seller otherSeller = new Seller();
        otherSeller.setId(UUID.randomUUID());
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(otherSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        try {
            sellerProductService.deleteProduct(sellerId, productId);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Test
    void deleteProduct_shouldThrowException_whenAlreadyDeleted() {
        // Arrange
        mockProduct.setDeletedAt(ZonedDateTime.now());
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        try {
            sellerProductService.deleteProduct(sellerId, productId);
            fail("Expected ResponseStatusException");
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }
}
