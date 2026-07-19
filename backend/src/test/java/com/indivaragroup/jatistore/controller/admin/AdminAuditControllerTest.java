package com.indivaragroup.jatistore.controller.admin;

import com.indivaragroup.jatistore.dto.response.module.admin.AuditTrailResponse;
import com.indivaragroup.jatistore.service.admin.AdminAuditService;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAuditController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminAuditService adminAuditService;

    @MockitoBean
    private AuthJWTUtility authJWTUtility;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.AuthRepository authRepository;

    @MockitoBean
    private com.indivaragroup.jatistore.repository.TokenRepository tokenRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void getAuditTrails_shouldReturnOk() throws Exception {
        AuditTrailResponse response = AuditTrailResponse.builder()
                .id(UUID.randomUUID())
                .action("LOGIN")
                .build();
        
        when(adminAuditService.getAuditTrails(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/audit-trails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].action").value("LOGIN"));
    }
}
