package com.indivaragroup.jatistore.controller.publics;

import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminCategoryResponse;
import com.indivaragroup.jatistore.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(RestApiPath.BASE_PATH)
@RequiredArgsConstructor
public class PublicCategoryController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/categories")
    public RestApiResponse<List<AdminCategoryResponse>> getCategories() {
        return RestApiResponse.success(adminDashboardService.getCategories());
    }
}
