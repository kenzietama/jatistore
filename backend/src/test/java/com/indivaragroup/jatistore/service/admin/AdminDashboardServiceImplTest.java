package com.indivaragroup.jatistore.service.admin;

import com.indivaragroup.jatistore.data.entity.ProductCategory;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.module.admin.CategoryRequest;
import com.indivaragroup.jatistore.dto.request.module.admin.UpdateSellerStatusRequest;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminCategoryResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminDashboardResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminSellerResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminDashboardServiceImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    @Test
    void getDashboardSummary_shouldReturnCounts() {
        when(authRepository.count()).thenReturn(10L);
        when(sellerRepository.count()).thenReturn(5L);
        when(productRepository.countByDeletedAtIsNull()).thenReturn(50L);
        when(orderRepository.countSuccessfulTransactions()).thenReturn(100L);

        AdminDashboardResponse response = adminDashboardService.getDashboardSummary();
        assertEquals(10L, response.getTotalUsers());
        assertEquals(5L, response.getTotalSellers());
        assertEquals(50L, response.getTotalProducts());
        assertEquals(100L, response.getTotalTransactions());
    }

    @Test
    void getSellers_shouldReturnFilteredPagedResult() {
        Seller seller1 = new Seller();
        seller1.setId(UUID.randomUUID());
        seller1.setActive(true);
        User user1 = new User();
        user1.setFullName("User 1");
        user1.setEmail("user1@example.com");
        user1.setCreatedAt(Instant.now());
        seller1.setUser(user1);

        Seller seller2 = new Seller();
        seller2.setId(UUID.randomUUID());
        seller2.setActive(false);
        User user2 = new User();
        user2.setFullName("User 2");
        user2.setEmail("user2@example.com");
        user2.setCreatedAt(Instant.now());
        seller2.setUser(user2);

        Store store1 = new Store();
        store1.setStoreName("Store 1");

        when(sellerRepository.findAll()).thenReturn(List.of(seller1, seller2));
        when(storeRepository.findBySellerId(seller1.getId())).thenReturn(Optional.of(store1));
        when(storeRepository.findBySellerId(seller2.getId())).thenReturn(Optional.empty());

        when(productRepository.countActiveProductsBySellerId(seller1.getId())).thenReturn(5L);
        when(productRepository.countActiveProductsBySellerId(seller2.getId())).thenReturn(0L);

        PageData<AdminSellerResponse> page1 = adminDashboardService.getSellers(0, 10, "ACTIVE", "Store", "Category: All");
        assertEquals(1, page1.getTotalElements());
        assertEquals("Store 1", page1.getContent().get(0).getStoreName());
        
        PageData<AdminSellerResponse> page2 = adminDashboardService.getSellers(0, 10, "INACTIVE", "", "");
        assertEquals(1, page2.getTotalElements());
        assertEquals("Unknown", page2.getContent().get(0).getStoreName());

        PageData<AdminSellerResponse> page3 = adminDashboardService.getSellers(0, 10, "", "", "");
        assertEquals(2, page3.getTotalElements());

        PageData<AdminSellerResponse> page4 = adminDashboardService.getSellers(1, 1, "", "", "");
        assertEquals(2, page4.getTotalElements());
        assertEquals(1, page4.getContent().size());

        PageData<AdminSellerResponse> page5 = adminDashboardService.getSellers(0, 10, null, null, "Electronics");
        assertEquals(2, page5.getTotalElements());

        PageData<AdminSellerResponse> page6 = adminDashboardService.getSellers(0, 10, null, "Store 1", "Category: All");
        assertEquals(1, page6.getTotalElements());
        
        PageData<AdminSellerResponse> page7 = adminDashboardService.getSellers(0, 10, null, "No Match", "");
        assertEquals(0, page7.getTotalElements());
    }

    @Test
    void updateSellerStatus_shouldUpdateAndSave() throws CoreThrowHandler {
        Seller seller = new Seller();
        seller.setId(UUID.randomUUID());
        seller.setActive(false);

        when(sellerRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        
        UpdateSellerStatusRequest request = new UpdateSellerStatusRequest();
        request.setActive(true);

        adminDashboardService.updateSellerStatus(seller.getId(), request);
        assertTrue(seller.getActive());
        verify(sellerRepository).save(seller);
    }

    @Test
    void updateSellerStatus_shouldThrowWhenNotFound() {
        when(sellerRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> adminDashboardService.updateSellerStatus(UUID.randomUUID(), new UpdateSellerStatusRequest()));
    }

    @Test
    void getCategories_shouldReturnList() {
        ProductCategory category = new ProductCategory();
        category.setId(UUID.randomUUID());
        category.setName("Electronics");

        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(productRepository.countByCategoryIdAndDeletedAtIsNull(category.getId())).thenReturn(10L);
        when(productRepository.countDistinctStoresByCategoryId(category.getId())).thenReturn(2L);

        List<AdminCategoryResponse> result = adminDashboardService.getCategories();
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
        assertEquals(10L, result.get(0).getProductCount());
        assertEquals(2L, result.get(0).getSellerCount());
    }

    @Test
    void createCategory_shouldSaveAndReturn() throws CoreThrowHandler {
        CategoryRequest request = new CategoryRequest();
        request.setName("New");

        ProductCategory category = new ProductCategory();
        category.setId(UUID.randomUUID());
        category.setName("New");

        when(categoryRepository.save(any())).thenReturn(category);

        AdminCategoryResponse response = adminDashboardService.createCategory(request);
        assertEquals("New", response.getName());
    }

    @Test
    void createCategory_shouldThrowOnException() {
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB Error"));
        assertThrows(CoreThrowHandler.class, () -> adminDashboardService.createCategory(new CategoryRequest()));
    }

    @Test
    void updateCategory_shouldSave() throws CoreThrowHandler {
        ProductCategory category = new ProductCategory();
        category.setId(UUID.randomUUID());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        CategoryRequest request = new CategoryRequest();
        request.setName("Updated");

        adminDashboardService.updateCategory(category.getId(), request);
        assertEquals("Updated", category.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_shouldThrowWhenNotFound() {
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> adminDashboardService.updateCategory(UUID.randomUUID(), new CategoryRequest()));
    }

    @Test
    void updateCategory_shouldThrowOnSaveException() {
        ProductCategory category = new ProductCategory();
        category.setId(UUID.randomUUID());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        CategoryRequest request = new CategoryRequest();
        request.setName("Updated");
        assertThrows(CoreThrowHandler.class, () -> adminDashboardService.updateCategory(category.getId(), request));
    }

    @Test
    void deleteCategory_shouldDelete() throws CoreThrowHandler {
        ProductCategory category = new ProductCategory();
        category.setId(UUID.randomUUID());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.countByCategoryIdAndDeletedAtIsNull(category.getId())).thenReturn(0L);

        adminDashboardService.deleteCategory(category.getId());
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldThrowWhenNotFound() {
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> adminDashboardService.deleteCategory(UUID.randomUUID()));
    }

    @Test
    void deleteCategory_shouldThrowWhenHasProducts() {
        ProductCategory category = new ProductCategory();
        category.setId(UUID.randomUUID());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.countByCategoryIdAndDeletedAtIsNull(category.getId())).thenReturn(5L);

        assertThrows(CoreThrowHandler.class, () -> adminDashboardService.deleteCategory(category.getId()));
    }
}
