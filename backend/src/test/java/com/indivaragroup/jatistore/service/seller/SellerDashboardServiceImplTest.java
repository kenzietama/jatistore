package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.DashboardStatsResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.FinancialOverviewResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.RecentOrderResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.SellerProfileResponse;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.repository.StoreRepository;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.OrderDetailRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SellerDashboardServiceImplTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private SellerDashboardServiceImpl sellerDashboardService;

    private UUID sellerId;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
    }

    // ==========================================
    // GET PROFILE
    // ==========================================

    @Test
    void getProfile_shouldReturnProfile() throws CoreThrowHandler {
        // Arrange
        User mockUser = new User();
        mockUser.setEmail("seller@test.com");
        Seller mockSeller = new Seller();
        mockSeller.setId(sellerId);
        mockSeller.setUser(mockUser);
        
        Store mockStore = new Store();
        mockStore.setStoreName("Test Store");
        mockStore.setImage("store.jpg");
        
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(storeRepository.findBySellerId(sellerId)).thenReturn(Optional.of(mockStore));

        // Act
        SellerProfileResponse response = sellerDashboardService.getProfile(sellerId);

        // Assert
        assertNotNull(response);
        assertEquals("Test Store", response.getStoreName());
        assertEquals("store.jpg", response.getStoreImage());
        assertEquals("seller@test.com", response.getEmail());
    }

    @Test
    void getProfile_shouldThrowException_whenStoreNotFound() {
        // Arrange
        User mockUser = new User();
        mockUser.setEmail("seller@test.com");
        Seller mockSeller = new Seller();
        mockSeller.setId(sellerId);
        mockSeller.setUser(mockUser);
        
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(storeRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            sellerDashboardService.getProfile(sellerId);
            fail("Expected CoreThrowHandler");
        } catch (CoreThrowHandler ex) {
            assertEquals(RestApiError.SLR_0002, ex.getRestApiError());
        }
    }

    // ==========================================
    // GET DASHBOARD STATS
    // ==========================================

    @Test
    void getDashboardStats_shouldReturnStats() {
        // Arrange
        User mockUser = new User();
        mockUser.setFullName("Alex Mercer");
        Seller mockSeller = new Seller();
        mockSeller.setId(sellerId);
        mockSeller.setUser(mockUser);
        
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.countActiveProductsBySellerId(sellerId)).thenReturn(15L);
        when(orderDetailRepository.countDistinctOrdersBySellerId(sellerId)).thenReturn(25L);

        // Act
        DashboardStatsResponse response = sellerDashboardService.getDashboardStats(sellerId);

        // Assert
        assertNotNull(response);
        assertEquals("Alex Mercer", response.getSellerName());
        assertEquals(15L, response.getTotalProducts());
        assertEquals(25L, response.getTotalOrders());
    }

    @Test
    void getDashboardStats_shouldThrowException_whenSellerNotFound() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            sellerDashboardService.getDashboardStats(sellerId);
            fail("Expected CoreThrowHandler");
        } catch (CoreThrowHandler ex) {
            assertEquals(RestApiError.SLR_0002, ex.getRestApiError());
        }
    }

    // ==========================================
    // GET FINANCIAL OVERVIEW
    // ==========================================

    @Test
    void getFinancialOverview_shouldReturnFinancials() {
        // Arrange
        Seller mockSeller = new Seller();
        mockSeller.setId(sellerId);
        mockSeller.setCachedAvailableBalance(new BigDecimal("1500.00"));
        mockSeller.setCachedOnHoldBalance(new BigDecimal("300.00"));

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));

        // Act
        FinancialOverviewResponse response = sellerDashboardService.getFinancialOverview(sellerId);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("1500.00"), response.getAvailableBalance());
        assertEquals(new BigDecimal("300.00"), response.getOnHoldBalance());
    }

    @Test
    void getFinancialOverview_shouldThrowException_whenSellerNotFound() {
        // Arrange
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            sellerDashboardService.getFinancialOverview(sellerId);
            fail("Expected CoreThrowHandler");
        } catch (CoreThrowHandler ex) {
            assertEquals(RestApiError.SLR_0002, ex.getRestApiError());
        }
    }

    // ==========================================
    // GET RECENT ORDERS
    // ==========================================

    @Test
    void getRecentOrders_shouldReturnRecentOrdersList() {
        // Arrange
        Order mockOrder = new Order();
        mockOrder.setId(UUID.randomUUID());
        mockOrder.setStatus(OrderStatus.SHIPPED);

        Product mockProduct = new Product();
        mockProduct.setName("Test Product");
        mockProduct.setImage("test.jpg");

        OrderDetail mockDetail = new OrderDetail();
        mockDetail.setId(UUID.randomUUID());
        mockDetail.setOrder(mockOrder);
        mockDetail.setProduct(mockProduct);
        mockDetail.setQuantity(2);
        mockDetail.setPricePerItem(new BigDecimal("50.00"));

        Page<OrderDetail> mockPage = new PageImpl<>(Arrays.asList(mockDetail));
        when(orderDetailRepository.searchAndFilterOrders(eq(sellerId), any(), any(), any(PageRequest.class)))
                .thenReturn(mockPage);

        // Act
        Page<RecentOrderResponse> result = sellerDashboardService.getRecentOrders(sellerId, null, null, null, null, 1, 5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        RecentOrderResponse response = result.getContent().get(0);
        assertEquals("Test Product", response.getItemName());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("SHIPPED", response.getStatus());
    }

    @Test
    void getRecentOrders_shouldReturnOrders_withSortDesc() {
        // Arrange
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.RECEIVED);
        
        Product product = new Product();
        product.setName("Test Product");
        product.setImage("test.jpg");
        
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setProduct(product);
        detail.setQuantity(2);
        detail.setPricePerItem(new BigDecimal("50.00"));

        Page<OrderDetail> page = new PageImpl<>(Arrays.asList(detail));
        
        when(orderDetailRepository.searchAndFilterOrders(eq(sellerId), any(), any(), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        Page<RecentOrderResponse> result = sellerDashboardService.getRecentOrders(
                sellerId, null, null, "createdAt", "desc", 1, 5
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("RECEIVED", result.getContent().get(0).getStatus());
    }

    @Test
    void getRecentOrders_shouldReturnOrders_withDifferentSortProperties() {
        // Arrange
        Page<OrderDetail> emptyPage = new PageImpl<>(Arrays.asList());
        when(orderDetailRepository.searchAndFilterOrders(eq(sellerId), any(), any(), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act & Assert
        // test sort property "amount"
        Page<RecentOrderResponse> resultAmount = sellerDashboardService.getRecentOrders(
                sellerId, null, null, "amount", "asc", 0, 5
        );
        assertNotNull(resultAmount);

        // test sort property "name"
        Page<RecentOrderResponse> resultName = sellerDashboardService.getRecentOrders(
                sellerId, null, null, "name", null, 2, 5
        );
        assertNotNull(resultName);

        // test sort property null
        Page<RecentOrderResponse> resultNullSort = sellerDashboardService.getRecentOrders(
                sellerId, null, null, null, "", 0, 5
        );
        assertNotNull(resultNullSort);

        // test sort property empty string
        Page<RecentOrderResponse> resultEmptySort = sellerDashboardService.getRecentOrders(
                sellerId, null, null, "", "asc", 0, 5
        );
        assertNotNull(resultEmptySort);
    }

    @Test
    void getRecentOrders_shouldReturnEmptyPage_whenNoOrdersFound() {
        // Arrange
        Page<OrderDetail> mockPage = new PageImpl<>(Arrays.asList());
        when(orderDetailRepository.searchAndFilterOrders(eq(sellerId), any(), any(), any(PageRequest.class)))
                .thenReturn(mockPage);

        // Act
        Page<RecentOrderResponse> result = sellerDashboardService.getRecentOrders(sellerId, null, null, null, null, 1, 5);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }
}
