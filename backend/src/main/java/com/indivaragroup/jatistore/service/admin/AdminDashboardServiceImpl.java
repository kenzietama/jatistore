package com.indivaragroup.jatistore.service.admin;

import com.indivaragroup.jatistore.data.entity.ProductCategory;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.dto.request.module.admin.CategoryRequest;
import com.indivaragroup.jatistore.dto.request.module.admin.UpdateSellerStatusRequest;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminCategoryResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminDashboardResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminSellerResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AuthRepository authRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductCategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardSummary() {
        long totalUsers = authRepository.count();
        long totalSellers = sellerRepository.count();
        long totalProducts = productRepository.countByDeletedAtIsNull();
        long totalTransactions = orderRepository.countSuccessfulTransactions();

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalSellers(totalSellers)
                .totalProducts(totalProducts)
                .totalTransactions(totalTransactions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<AdminSellerResponse> getSellers(int page, int size, String status, String search, String category) {
        // Since we need to filter by store name and category (complex joins),
        // we will fetch all and filter in memory for this prototype.
        List<Seller> allSellers = sellerRepository.findAll();

        List<AdminSellerResponse> allResponses = allSellers.stream().map(seller -> {
            Store store = storeRepository.findBySellerId(seller.getId()).orElse(null);
            long productCount = productRepository.countActiveProductsBySellerId(seller.getId());

            return AdminSellerResponse.builder()
                    .id(seller.getId())
                    .storeName(store != null ? store.getStoreName() : "Unknown")
                    .sellerName(seller.getUser().getFullName())
                    .email(seller.getUser().getEmail())
                    .productCount(productCount)
                    .active(seller.getActive())
                    .joinDate(seller.getUser().getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        // Apply filters
        List<AdminSellerResponse> filtered = allResponses.stream()
            .filter(r -> {
                if (status != null && !status.isEmpty()) {
                    boolean active = "ACTIVE".equalsIgnoreCase(status);
                    if (r.getActive() != active) return false;
                }
                if (search != null && !search.isEmpty()) {
                    if (!r.getStoreName().toLowerCase().contains(search.toLowerCase())) return false;
                }

                return true;
            })
            .collect(Collectors.toList());

        // Manual Pagination
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = Math.min(page * size, totalElements);
        int end = Math.min((page + 1) * size, totalElements);
        List<AdminSellerResponse> pagedContent = filtered.subList(start, end);

        PageData<AdminSellerResponse> pageData = new PageData<>();
        pageData.setContent(pagedContent);
        pageData.setPage(page);
        pageData.setSize(size);
        pageData.setTotalElements(totalElements);
        pageData.setTotalPages(totalPages);
        return pageData;
    }

    @Override
    @Transactional
    public void updateSellerStatus(UUID sellerId, UpdateSellerStatusRequest request) throws CoreThrowHandler {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.ADM_0006));
        
        seller.setActive(request.getActive());
        sellerRepository.save(seller);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> getCategories() {
        List<ProductCategory> categories = categoryRepository.findAll();
        
        return categories.stream().map(category -> {
            long productCount = productRepository.countByCategoryIdAndDeletedAtIsNull(category.getId());
            long sellerCount = productRepository.countDistinctStoresByCategoryId(category.getId());
            
            return AdminCategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .productCount(productCount)
                    .sellerCount(sellerCount)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminCategoryResponse createCategory(CategoryRequest request) throws CoreThrowHandler {
        // Validation could be added here if needed, but unique constraint is DB level
        try {
            ProductCategory category = new ProductCategory();
            category.setName(request.getName());
            category = categoryRepository.save(category);
            
            return AdminCategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .productCount(0)
                    .sellerCount(0)
                    .build();
        } catch (Exception e) {
            throw new CoreThrowHandler(RestApiError.ADM_0010);
        }
    }

    @Override
    @Transactional
    public void updateCategory(UUID categoryId, CategoryRequest request) throws CoreThrowHandler {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.ADM_0011));
        
        try {
            category.setName(request.getName());
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new CoreThrowHandler(RestApiError.ADM_0010);
        }
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) throws CoreThrowHandler {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.ADM_0011));
        
        long activeProducts = productRepository.countByCategoryIdAndDeletedAtIsNull(categoryId);
        if (activeProducts > 0) {
            throw new CoreThrowHandler(RestApiError.ADM_0012);
        }
        
        categoryRepository.delete(category);
    }
}
