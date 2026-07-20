package com.indivaragroup.jatistore.controller.admin;

import com.indivaragroup.jatistore.dto.request.module.admin.CategoryRequest;
import com.indivaragroup.jatistore.dto.request.module.admin.UpdateSellerStatusRequest;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminCategoryResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminDashboardResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminSellerResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.service.admin.AdminDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping(RestApiPath.ADMIN_DASHBOARD_PATH)
    public RestApiResponse<AdminDashboardResponse> getDashboardSummary() {
        return RestApiResponse.success(adminDashboardService.getDashboardSummary());
    }

    @GetMapping(RestApiPath.ADMIN_SELLERS_PATH)
    public RestApiResponse<PageData<AdminSellerResponse>> getSellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        return RestApiResponse.success(adminDashboardService.getSellers(page, size, status, search, category));
    }

    @PatchMapping(RestApiPath.ADMIN_SELLERS_PATH + "/{sellerId}/status")
    @Audit(action = "SELLER_UPDATE_STATUS", affectedModule = "SELLERS", description = "Admin updated seller status")
    public RestApiResponse<Void> updateSellerStatus(
            @PathVariable UUID sellerId,
            @Valid @RequestBody UpdateSellerStatusRequest request) throws CoreThrowHandler {
        adminDashboardService.updateSellerStatus(sellerId, request);
        return RestApiResponse.success(null);
    }

    @GetMapping(RestApiPath.ADMIN_CATEGORIES_PATH)
    public RestApiResponse<List<AdminCategoryResponse>> getCategories() {
        return RestApiResponse.success(adminDashboardService.getCategories());
    }

    @PostMapping(RestApiPath.ADMIN_CATEGORIES_PATH)
    @Audit(action = "CATEGORY_CREATE", affectedModule = "CATEGORIES", description = "Admin created a new category")
    public RestApiResponse<AdminCategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) throws CoreThrowHandler {
        return RestApiResponse.success(adminDashboardService.createCategory(request));
    }

    @PutMapping(RestApiPath.ADMIN_CATEGORIES_PATH + "/{categoryId}")
    @Audit(action = "CATEGORY_UPDATE", affectedModule = "CATEGORIES", description = "Admin updated a category")
    public RestApiResponse<Void> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest request) throws CoreThrowHandler {
        adminDashboardService.updateCategory(categoryId, request);
        return RestApiResponse.success(null);
    }

    @DeleteMapping(RestApiPath.ADMIN_CATEGORIES_PATH + "/{categoryId}")
    @Audit(action = "CATEGORY_DELETE", affectedModule = "CATEGORIES", description = "Admin deleted a category")
    public RestApiResponse<Void> deleteCategory(@PathVariable UUID categoryId) throws CoreThrowHandler {
        adminDashboardService.deleteCategory(categoryId);
        return RestApiResponse.success(null);
    }
}
