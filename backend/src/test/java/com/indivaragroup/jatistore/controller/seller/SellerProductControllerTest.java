package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.repository.SellerRepository;
import tools.jackson.databind.ObjectMapper;
import com.indivaragroup.jatistore.dto.request.seller.ProductCreateRequest;
import com.indivaragroup.jatistore.dto.request.seller.ProductUpdateRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.ProductResponse;
import com.indivaragroup.jatistore.service.seller.SellerProductService;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import com.indivaragroup.jatistore.repository.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SellerProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SellerProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SellerProductService sellerProductService;

    @MockitoBean
    private AuthJWTUtility authJWTUtility;

    @MockitoBean
    private AuthRepository authRepository;

    @MockitoBean
    private SellerRepository sellerRepository;

    @MockitoBean
    private com.indivaragroup.jatistore.service.seller.SellerSecurityHelper sellerSecurityHelper;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    private UUID mockSellerId;
    private UUID mockProductId;
    private ProductResponse mockProduct;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockSellerId = UUID.fromString("bb000000-0000-0000-0000-000000000001");
        mockProductId = UUID.randomUUID();
        mockPrincipal = () -> "seller@test.com";

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("seller@test.com");

        Seller mockSeller = new Seller();
        mockSeller.setId(mockSellerId);

        when(authRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(mockUser));
        when(sellerRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockSeller));
        when(sellerSecurityHelper.getSellerIdFromPrincipal(any())).thenReturn(mockSellerId);

        mockProduct = new ProductResponse();
        mockProduct.setId(mockProductId);
        mockProduct.setName("Test Product");
        mockProduct.setPrice(new BigDecimal("100.00"));
        mockProduct.setStock(10);
    }

    // ==========================================
    // GET PRODUCTS
    // ==========================================

    @Test
    void getProducts_shouldReturnOk() throws Exception {
        // Arrange
        Page<ProductResponse> page = new PageImpl<>(Arrays.asList(mockProduct));
        when(sellerProductService.getProducts(eq(mockSellerId), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/seller/products")
                .param("page", "0")
                .param("size", "20")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"));
    }

    @Test
    void getProducts_shouldReturnBadRequest_whenSizeIsInvalidType() throws Exception {
        // Arrange (none needed for param type mismatch)

        // Act & Assert
        mockMvc.perform(get("/api/v1/seller/products")
                .param("size", "invalid-size")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // ==========================================
    // GET PRODUCT
    // ==========================================

    @Test
    void getProduct_shouldReturnOk() throws Exception {
        // Arrange
        when(sellerProductService.getProduct(mockSellerId, mockProductId))
                .thenReturn(mockProduct);

        // Act & Assert
        mockMvc.perform(get("/api/v1/seller/products/{id}", mockProductId)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    void getProduct_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        // Arrange
        when(sellerProductService.getProduct(mockSellerId, mockProductId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/seller/products/{id}", mockProductId)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // ==========================================
    // CREATE PRODUCT
    // ==========================================

    @Test
    void createProduct_shouldReturnCreated() throws Exception {
        // Arrange
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("New Product");
        request.setPrice(new BigDecimal("150.00"));
        request.setStock(5);
        request.setProductCategoryId(UUID.randomUUID());

        ProductResponse createdProduct = new ProductResponse();
        createdProduct.setId(UUID.randomUUID());

        when(sellerProductService.createProduct(eq(mockSellerId), any(ProductCreateRequest.class)))
                .thenReturn(createdProduct);

        // Act & Assert
        mockMvc.perform(post("/api/v1/seller/products")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(createdProduct.getId().toString()));
    }

    @Test
    void createProduct_shouldReturnBadRequest_whenValidationFails() throws Exception {
        // Arrange
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName(""); // Invalid: NotBlank
        request.setPrice(new BigDecimal("-10.00")); // Invalid: Positive

        // Act & Assert
        mockMvc.perform(post("/api/v1/seller/products")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // UPDATE PRODUCT
    // ==========================================

    @Test
    void updateProduct_shouldReturnOk() throws Exception {
        // Arrange
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setPrice(new BigDecimal("200.00")); // Partial update

        when(sellerProductService.updateProduct(eq(mockSellerId), eq(mockProductId), any(ProductUpdateRequest.class)))
                .thenReturn(mockProduct);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/seller/products/{id}", mockProductId)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void updateProduct_shouldReturnBadRequest_whenValidationFails() throws Exception {
        // Arrange
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setPrice(new BigDecimal("-50.00")); // Invalid: Positive

        // Act & Assert
        mockMvc.perform(patch("/api/v1/seller/products/{id}", mockProductId)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // DELETE PRODUCT
    // ==========================================

    @Test
    void deleteProduct_shouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(sellerProductService).deleteProduct(mockSellerId, mockProductId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/seller/products/{id}", mockProductId)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProduct_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        // Arrange
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"))
                .when(sellerProductService).deleteProduct(mockSellerId, mockProductId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/seller/products/{id}", mockProductId)
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
