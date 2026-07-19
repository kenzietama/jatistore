package com.indivaragroup.jatistore.controller.admin;

import tools.jackson.databind.ObjectMapper;
import com.indivaragroup.jatistore.dto.request.module.admin.FlashSaleRequest;
import com.indivaragroup.jatistore.dto.response.module.admin.FlashSaleResponse;
import com.indivaragroup.jatistore.service.admin.AdminFlashSaleService;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminFlashSaleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminFlashSaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminFlashSaleService adminFlashSaleService;

    @MockitoBean
    private AuthJWTUtility authJWTUtility;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void getFlashSales_shouldReturnOk() throws Exception {
        FlashSaleResponse response = FlashSaleResponse.builder()
                .id(UUID.randomUUID())
                .name("Event")
                .build();
        when(adminFlashSaleService.getAllFlashSales(anyInt(), anyInt(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/flash-sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Event"));
    }

    @Test
    void createFlashSale_shouldReturnCreated() throws Exception {
        FlashSaleRequest request = new FlashSaleRequest();
        request.setName("Event");
        request.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        request.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));

        FlashSaleResponse response = FlashSaleResponse.builder()
                .id(UUID.randomUUID())
                .name("Event")
                .build();
        when(adminFlashSaleService.createFlashSale(any(FlashSaleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/flash-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Event"));
    }

    @Test
    void getFlashSaleDetail_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        FlashSaleResponse response = FlashSaleResponse.builder()
                .id(id)
                .name("Event")
                .build();
        when(adminFlashSaleService.getFlashSaleDetail(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/flash-sales/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Event"));
    }

    @Test
    void updateFlashSale_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        FlashSaleRequest request = new FlashSaleRequest();
        request.setName("Event");
        request.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        request.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));

        doNothing().when(adminFlashSaleService).updateFlashSale(eq(id), any(FlashSaleRequest.class));

        mockMvc.perform(put("/api/v1/admin/flash-sales/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFlashSale_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(adminFlashSaleService).deleteFlashSale(id);

        mockMvc.perform(delete("/api/v1/admin/flash-sales/{id}", id))
                .andExpect(status().isOk());
    }
}
