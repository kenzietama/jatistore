package com.indivaragroup.jatistore.service.flashsale;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.repository.FlashSaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashSaleServiceTest {

    @Mock
    private FlashSaleRepository flashSaleRepository;

    @InjectMocks
    private FlashSaleService flashSaleService;

    @Test
    void getActiveFlashSaleEvent_ShouldReturnFlashSale_WhenPresent() {
        FlashSale flashSale = new FlashSale();
        flashSale.setName("Big Sale");
        
        when(flashSaleRepository.findActiveFlashSale(any())).thenReturn(Optional.of(flashSale));

        FlashSale result = flashSaleService.getActiveFlashSaleEvent();

        assertNotNull(result);
        assertEquals("Big Sale", result.getName());
        verify(flashSaleRepository, times(1)).findActiveFlashSale(any());
    }

    @Test
    void getActiveFlashSaleEvent_ShouldReturnNull_WhenNotPresent() {
        when(flashSaleRepository.findActiveFlashSale(any())).thenReturn(Optional.empty());

        FlashSale result = flashSaleService.getActiveFlashSaleEvent();

        assertNull(result);
        verify(flashSaleRepository, times(1)).findActiveFlashSale(any());
    }

    @Test
    void getUpcomingFlashSale_ShouldReturnFlashSale_WhenPresent() {
        FlashSale flashSale = new FlashSale();
        flashSale.setName("Next Sale");
        
        when(flashSaleRepository.findAvailableFlashSales(any())).thenReturn(java.util.List.of(flashSale));

        FlashSale result = flashSaleService.getUpcomingFlashSale();

        assertNotNull(result);
        assertEquals("Next Sale", result.getName());
        verify(flashSaleRepository, times(1)).findAvailableFlashSales(any());
    }

    @Test
    void getUpcomingFlashSale_ShouldReturnNull_WhenNotPresent() {
        when(flashSaleRepository.findAvailableFlashSales(any())).thenReturn(java.util.List.of());

        FlashSale result = flashSaleService.getUpcomingFlashSale();

        assertNull(result);
        verify(flashSaleRepository, times(1)).findAvailableFlashSales(any());
    }
}
