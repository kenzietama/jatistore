package com.indivaragroup.jatistore.service.admin;

import com.indivaragroup.jatistore.dto.request.module.admin.CategoryRequest;
import com.indivaragroup.jatistore.dto.request.module.admin.UpdateSellerStatusRequest;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminCategoryResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminDashboardResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminSellerResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;

import java.util.List;
import java.util.UUID;

public interface AdminDashboardService {
    AdminDashboardResponse getDashboardSummary();
    PageData<AdminSellerResponse> getSellers(int page, int size, String status, String search, String category);
    void updateSellerStatus(UUID sellerId, UpdateSellerStatusRequest request) throws CoreThrowHandler;
    
    List<AdminCategoryResponse> getCategories();
    AdminCategoryResponse createCategory(CategoryRequest request) throws CoreThrowHandler;
    void updateCategory(UUID categoryId, CategoryRequest request) throws CoreThrowHandler;
    void deleteCategory(UUID categoryId) throws CoreThrowHandler;
}
