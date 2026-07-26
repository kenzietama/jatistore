package com.indivaragroup.jatistore.integration;

import com.indivaragroup.jatistore.dto.request.auth.AuthRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testFullRegistrationFlow() throws Exception {
        AuthRegisterRequest request = new AuthRegisterRequest(
            "newuser@example.com",
            "SecurePass123",
            "newuser",
            "08111222333",
            "New User",
            LocalDate.of(1995, 6, 15)
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Request-ID", "integration-test-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("Registration successful. Please login."));
    }

    @Test
    void testRegistrationWithDuplicateEmail() throws Exception {
        // First registration
        AuthRegisterRequest firstRequest = new AuthRegisterRequest(
            "duplicate@example.com",
            "Password123",
            "user1",
            "08111222333",
            "User One",
            null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Request-ID", "test-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
            .andExpect(status().isOk());

        // Second registration with same email
        AuthRegisterRequest secondRequest = new AuthRegisterRequest(
            "duplicate@example.com",
            "Password456",
            "user2",
            "08111222444",
            "User Two",
            null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Request-ID", "test-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
            .andExpect(status().isConflict());
    }
}
