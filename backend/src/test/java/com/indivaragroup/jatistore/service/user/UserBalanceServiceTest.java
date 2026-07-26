package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserBalanceResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBalanceServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserBalanceService userBalanceService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userBalanceService, "restTemplate", restTemplate);
    }

    @Test
    void getBalance_ShouldReturnBalance_WhenSuccess() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "SUCCESS");
        mockResponse.put("amount", 150000);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        RestApiResponse<UserBalanceResponse> response = userBalanceService.getBalance();

        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertEquals(new BigDecimal("150000"), response.getRestApiResponseData().getBalance());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Map.class));
    }

    @Test
    void getBalance_ShouldReturnZero_WhenAmountNotNumber() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "SUCCESS");
        mockResponse.put("amount", "invalid_amount");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        RestApiResponse<UserBalanceResponse> response = userBalanceService.getBalance();

        assertNotNull(response);
        assertEquals(new BigDecimal("0"), response.getRestApiResponseData().getBalance());
    }

    @Test
    void getBalance_ShouldThrowException_WhenStatusNotSuccess() {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "FAILED");

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        assertThrows(CoreThrowHandler.class, () -> userBalanceService.getBalance());
    }
    
    @Test
    void getBalance_ShouldThrowException_WhenResponseNull() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

        assertThrows(CoreThrowHandler.class, () -> userBalanceService.getBalance());
    }

    @Test
    void getBalance_ShouldThrowException_WhenRestTemplateThrows() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("Gateway error"));

        assertThrows(CoreThrowHandler.class, () -> userBalanceService.getBalance());
    }
}
