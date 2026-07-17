package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.OrderDetail;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.DashboardStatsResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.FinancialOverviewResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.RecentOrderResponse;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.data.utility.constant.OrderStatus;
import com.indivaragroup.jatistore.dto.response.module.seller.dashboard.SellerProfileResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.OrderDetailRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import com.indivaragroup.jatistore.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StoreRepository storeRepository;

    @Override
    public SellerProfileResponse getProfile(UUID sellerId) throws CoreThrowHandler {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0002));
        Store store = storeRepository.findBySellerId(sellerId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0002));
        return SellerProfileResponse.builder()
                .storeName(store.getStoreName())
                .storeImage(store.getImage())
                .email(seller.getUser().getEmail())
                .build();
    }

    @Override
    public DashboardStatsResponse getDashboardStats(UUID sellerId) throws CoreThrowHandler {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0002));
        long totalProducts = productRepository.countActiveProductsBySellerId(sellerId);
        long totalOrders = orderDetailRepository.countDistinctOrdersBySellerId(sellerId);
        
        return DashboardStatsResponse.builder()
                .sellerName(seller.getUser().getFullName())
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .build();
    }

    @Override
    public FinancialOverviewResponse getFinancialOverview(UUID sellerId) throws CoreThrowHandler {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0002));

        return FinancialOverviewResponse.builder()
                .availableBalance(seller.getCachedAvailableBalance())
                .onHoldBalance(seller.getCachedOnHoldBalance())
                .build();
    }

    @Override
    public Page<RecentOrderResponse> getRecentOrders(UUID sellerId, String search, OrderStatus status, String sortBy, String sortDir, int page, int limit) {
        Sort.Direction direction = sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortProperty = sortBy != null && !sortBy.isEmpty() ? sortBy : "createdAt";
        if (sortProperty.equals("createdAt")) sortProperty = "order.createdAt";
        if (sortProperty.equals("amount")) sortProperty = "pricePerItem";
        if (sortProperty.equals("name")) sortProperty = "product.name";
        
        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : 0, limit, Sort.by(direction, sortProperty));

        Page<OrderDetail> recentDetails = orderDetailRepository.searchAndFilterOrders(
                sellerId, search, status, pageRequest
        );

        return recentDetails.map(detail -> {
            String orderIdStr = detail.getOrder().getId().toString();
            String shortId = "#ORD-" + orderIdStr.substring(0, 4).toUpperCase(); // mock short ID
            
            return RecentOrderResponse.builder()
                    .orderId(detail.getOrder().getId())
                    .displayId(shortId)
                    .itemName(detail.getProduct().getName())
                    .itemImage(detail.getProduct().getImage())
                    .quantity(detail.getQuantity())
                    .amount(detail.getPricePerItem().multiply(new java.math.BigDecimal(detail.getQuantity())))
                    .status(detail.getOrder().getStatus() != null ? detail.getOrder().getStatus().name().replace("_", " ") : null)
                    .build();
        });
    }
}
