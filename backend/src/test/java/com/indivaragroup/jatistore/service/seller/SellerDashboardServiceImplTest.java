package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Order;
import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.DashboardStatsResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.FinancialOverviewResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.RecentOrderResponse;
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
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private SellerDashboardServiceImpl sellerDashboardService;

    private UUID sellerId;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
    }

    @Test
    void getDashboardStats_shouldReturnStats() {
        User mockUser = new User();
        mockUser.setFullName("Alex Mercer");
        Seller mockSeller = new Seller();
        mockSeller.setId(sellerId);
        mockSeller.setUser(mockUser);
        
        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));
        when(productRepository.countActiveProductsBySellerId(sellerId)).thenReturn(15L);
        when(orderDetailRepository.countDistinctOrdersBySellerId(sellerId)).thenReturn(25L);

        DashboardStatsResponse response = sellerDashboardService.getDashboardStats(sellerId);

        assertNotNull(response);
        assertEquals("Alex Mercer", response.getSellerName());
        assertEquals(15L, response.getTotalProducts());
        assertEquals(25L, response.getTotalOrders());
    }

    @Test
    void getFinancialOverview_shouldReturnFinancials() {
        Seller mockSeller = new Seller();
        mockSeller.setId(sellerId);
        mockSeller.setCachedAvailableBalance(new BigDecimal("1500.00"));
        mockSeller.setCachedOnHoldBalance(new BigDecimal("300.00"));

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(mockSeller));

        FinancialOverviewResponse response = sellerDashboardService.getFinancialOverview(sellerId);

        assertNotNull(response);
        assertEquals(new BigDecimal("1500.00"), response.getAvailableBalance());
        assertEquals(new BigDecimal("300.00"), response.getOnHoldBalance());
    }

    @Test
    void getRecentOrders_shouldReturnRecentOrdersList() {
        Order mockOrder = new Order();
        mockOrder.setId(UUID.randomUUID());
        mockOrder.setStatus("SHIPPED");

        Product mockProduct = new Product();
        mockProduct.setName("Test Product");
        mockProduct.setImage("http://example.com/img.png");

        OrderDetail mockDetail = new OrderDetail();
        mockDetail.setOrder(mockOrder);
        mockDetail.setProduct(mockProduct);
        mockDetail.setPricePerItem(new BigDecimal("10.00"));
        mockDetail.setQuantity(2);

        when(orderDetailRepository.searchAndFilterOrders(eq(sellerId), any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(mockDetail)));

        Page<RecentOrderResponse> response = sellerDashboardService.getRecentOrders(sellerId, null, null, null, null, 1, 5);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        RecentOrderResponse recentOrder = response.getContent().get(0);
        assertEquals(mockOrder.getId(), recentOrder.getOrderId());
        assertEquals("Test Product", recentOrder.getItemName());
        assertEquals(new BigDecimal("20.00"), recentOrder.getAmount());
        assertEquals("SHIPPED", recentOrder.getStatus());
    }
}
