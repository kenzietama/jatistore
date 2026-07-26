package com.indivaragroup.jatistore.controller.flashsale;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.service.flashsale.FlashSaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FlashSalePublicControllerTest {

    @Mock
    private FlashSaleService flashSaleService;

    @InjectMocks
    private FlashSalePublicController flashSalePublicController;

    private MockMvc mockMvc;
    
    private FlashSale mockFlashSale;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(flashSalePublicController).build();
        mockFlashSale = new FlashSale();
        mockFlashSale.setId(UUID.randomUUID());
        mockFlashSale.setName("Test Flash Sale");
        mockFlashSale.setStartTime(Instant.now());
        mockFlashSale.setEndTime(Instant.now().plusSeconds(3600));
    }

    @Test
    void getActiveFlashSale_Success() throws Exception {
        when(flashSaleService.getActiveFlashSaleEvent()).thenReturn(mockFlashSale);

        mockMvc.perform(get("/api/v1/public/flash-sale/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Flash Sale"));
    }

    @Test
    void getUpcomingFlashSale_Success() throws Exception {
        when(flashSaleService.getUpcomingFlashSale()).thenReturn(mockFlashSale);

        mockMvc.perform(get("/api/v1/public/flash-sale/upcoming")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Flash Sale"));
    }
    
    @Test
    void getUpcomingFlashSale_Null_Success() throws Exception {
        when(flashSaleService.getUpcomingFlashSale()).thenReturn(null);

        mockMvc.perform(get("/api/v1/public/flash-sale/upcoming")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No upcoming flash sale available."));
    }
}
