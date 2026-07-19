package com.indivaragroup.jatistore.controller.admin;

import tools.jackson.databind.ObjectMapper;
import com.indivaragroup.jatistore.dto.request.module.admin.CategoryRequest;
import com.indivaragroup.jatistore.dto.request.module.admin.UpdateSellerStatusRequest;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminCategoryResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminDashboardResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.AdminSellerResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.service.admin.AdminDashboardService;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminDashboardService adminDashboardService;

    @MockitoBean
    private AuthJWTUtility authJWTUtility;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void getDashboardSummary_shouldReturnOk() throws Exception {
        AdminDashboardResponse response = AdminDashboardResponse.builder()
                .totalUsers(10L)
                .totalSellers(5L)
                .totalProducts(50L)
                .totalTransactions(100L)
                .build();

        when(adminDashboardService.getDashboardSummary()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(10))
                .andExpect(jsonPath("$.data.totalSellers").value(5));
    }

    @Test
    void getSellers_shouldReturnOk() throws Exception {
        AdminSellerResponse seller = AdminSellerResponse.builder()
                .id(UUID.randomUUID())
                .storeName("My Store")
                .sellerName("John Doe")
                .build();

        PageData<AdminSellerResponse> pageData = new PageData<>();
        pageData.setContent(List.of(seller));
        pageData.setTotalElements(1);

        when(adminDashboardService.getSellers(anyInt(), anyInt(), any(), any(), any())).thenReturn(pageData);

        mockMvc.perform(get("/api/v1/admin/sellers")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].storeName").value("My Store"));
    }

    @Test
    void updateSellerStatus_shouldReturnOk() throws Exception {
        UpdateSellerStatusRequest request = new UpdateSellerStatusRequest();
        request.setActive(true);

        UUID sellerId = UUID.randomUUID();
        doNothing().when(adminDashboardService).updateSellerStatus(eq(sellerId), any(UpdateSellerStatusRequest.class));

        mockMvc.perform(patch("/api/v1/admin/sellers/{sellerId}/status", sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getCategories_shouldReturnOk() throws Exception {
        AdminCategoryResponse category = AdminCategoryResponse.builder()
                .id(UUID.randomUUID())
                .name("Electronics")
                .build();

        when(adminDashboardService.getCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/v1/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Electronics"));
    }

    @Test
    void createCategory_shouldReturnOk() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("New Category");

        AdminCategoryResponse response = AdminCategoryResponse.builder()
                .id(UUID.randomUUID())
                .name("New Category")
                .build();

        when(adminDashboardService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Category"));
    }

    @Test
    void updateCategory_shouldReturnOk() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Category");
        UUID categoryId = UUID.randomUUID();

        doNothing().when(adminDashboardService).updateCategory(eq(categoryId), any(CategoryRequest.class));

        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCategory_shouldReturnOk() throws Exception {
        UUID categoryId = UUID.randomUUID();
        doNothing().when(adminDashboardService).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", categoryId))
                .andExpect(status().isOk());
    }
}
