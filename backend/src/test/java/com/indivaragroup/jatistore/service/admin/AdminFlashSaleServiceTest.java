package com.indivaragroup.jatistore.service.admin;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.dto.request.module.admin.FlashSaleRequest;
import com.indivaragroup.jatistore.dto.response.module.admin.FlashSaleResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.FlashSaleItemRepository;
import com.indivaragroup.jatistore.repository.FlashSaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminFlashSaleServiceTest {

    @Mock
    private FlashSaleRepository flashSaleRepository;

    @Mock
    private FlashSaleItemRepository flashSaleItemRepository;

    @InjectMocks
    private AdminFlashSaleService adminFlashSaleService;

    @Test
    void getAllFlashSales_shouldReturnPage() throws CoreThrowHandler {
        FlashSale flashSale = new FlashSale();
        flashSale.setId(UUID.randomUUID());
        flashSale.setName("Event 1");
        flashSale.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        flashSale.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));

        FlashSale activeSale = new FlashSale();
        activeSale.setId(UUID.randomUUID());
        activeSale.setName("Event 2");
        activeSale.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS));
        activeSale.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS));

        FlashSale endedSale = new FlashSale();
        endedSale.setId(UUID.randomUUID());
        endedSale.setName("Event 3");
        endedSale.setStartTime(Instant.now().minus(2, ChronoUnit.DAYS));
        endedSale.setEndTime(Instant.now().minus(1, ChronoUnit.DAYS));

        Page<FlashSale> page = new PageImpl<>(List.of(flashSale, activeSale, endedSale));
        when(flashSaleRepository.findBySearchAndStatus(anyString(), anyString(), any(Instant.class), any(Pageable.class)))
                .thenReturn(page);
        when(flashSaleItemRepository.countByFlashSaleId(any(UUID.class))).thenReturn(10L);

        Page<FlashSaleResponse> result = adminFlashSaleService.getAllFlashSales(0, 10, "UPCOMING", "Event", "startTime", "desc");
        
        assertEquals(3, result.getTotalElements());
        assertEquals("Event 1", result.getContent().get(0).getName());
        assertEquals("UPCOMING", result.getContent().get(0).getStatus());
        assertEquals("ACTIVE", result.getContent().get(1).getStatus());
        assertEquals("ENDED", result.getContent().get(2).getStatus());
    }

    @Test
    void getAllFlashSales_invalidPagination_shouldThrow() {
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.getAllFlashSales(-1, 10, "", "", "", ""));
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.getAllFlashSales(0, 0, "", "", "", ""));
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.getAllFlashSales(0, 101, "", "", "", ""));
    }

    @Test
    void getAllFlashSales_invalidStatus_shouldThrow() {
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.getAllFlashSales(0, 10, "INVALID", "", "", ""));
    }

    @Test
    void getAllFlashSales_validStatuses_shouldSucceed() throws CoreThrowHandler {
        Page<FlashSale> page = new PageImpl<>(List.of());
        when(flashSaleRepository.findBySearchAndStatus(anyString(), eq("ACTIVE"), any(Instant.class), any(Pageable.class)))
                .thenReturn(page);
        adminFlashSaleService.getAllFlashSales(0, 10, "ACTIVE", "", "", "");

        when(flashSaleRepository.findBySearchAndStatus(anyString(), eq("ENDED"), any(Instant.class), any(Pageable.class)))
                .thenReturn(page);
        adminFlashSaleService.getAllFlashSales(0, 10, "ENDED", "", "", "");
    }

    @Test
    void getAllFlashSales_emptyStatusAndSearch_shouldSucceed() throws CoreThrowHandler {
        Page<FlashSale> page = new PageImpl<>(List.of());
        when(flashSaleRepository.findBySearchAndStatus(eq(""), eq(""), any(Instant.class), any(Pageable.class)))
                .thenReturn(page);

        Page<FlashSaleResponse> result = adminFlashSaleService.getAllFlashSales(0, 10, "", "", "", "asc");
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void createFlashSale_shouldSave() throws CoreThrowHandler {
        FlashSaleRequest request = new FlashSaleRequest();
        request.setName("Event");
        request.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        request.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));

        FlashSale flashSale = new FlashSale();
        flashSale.setId(UUID.randomUUID());
        flashSale.setName("Event");
        flashSale.setStartTime(request.getStartTime());
        flashSale.setEndTime(request.getEndTime());

        when(flashSaleRepository.save(any(FlashSale.class))).thenReturn(flashSale);

        FlashSaleResponse response = adminFlashSaleService.createFlashSale(request);
        assertEquals("Event", response.getName());
    }

    @Test
    void createFlashSale_invalidDates_shouldThrow() {
        FlashSaleRequest request = new FlashSaleRequest();
        request.setName("Event");
        request.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS)); // Past
        request.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.createFlashSale(request));

        request.setStartTime(Instant.now().plus(2, ChronoUnit.DAYS));
        request.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS)); // End before start
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.createFlashSale(request));
    }

    @Test
    void getFlashSaleDetail_shouldReturn() throws CoreThrowHandler {
        FlashSale flashSale = new FlashSale();
        flashSale.setId(UUID.randomUUID());
        flashSale.setName("Event");
        flashSale.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        flashSale.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));

        when(flashSaleRepository.findById(flashSale.getId())).thenReturn(Optional.of(flashSale));
        when(flashSaleItemRepository.countByFlashSaleId(flashSale.getId())).thenReturn(5L);

        FlashSaleResponse response = adminFlashSaleService.getFlashSaleDetail(flashSale.getId());
        assertEquals("Event", response.getName());
    }

    @Test
    void getFlashSaleDetail_notFound_shouldThrow() {
        when(flashSaleRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.getFlashSaleDetail(UUID.randomUUID()));
    }

    @Test
    void updateFlashSale_shouldUpdate() throws CoreThrowHandler {
        FlashSale flashSale = new FlashSale();
        flashSale.setId(UUID.randomUUID());
        flashSale.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        flashSale.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));
        
        when(flashSaleRepository.findById(flashSale.getId())).thenReturn(Optional.of(flashSale));

        FlashSaleRequest request = new FlashSaleRequest();
        request.setName("Updated");
        request.setStartTime(Instant.now().plus(2, ChronoUnit.DAYS));
        request.setEndTime(Instant.now().plus(3, ChronoUnit.DAYS));

        adminFlashSaleService.updateFlashSale(flashSale.getId(), request);
        assertEquals("Updated", flashSale.getName());
        verify(flashSaleRepository).save(flashSale);
    }

    @Test
    void updateFlashSale_invalidDates_shouldThrow() {
        FlashSale flashSale = new FlashSale();
        flashSale.setId(UUID.randomUUID());
        flashSale.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        flashSale.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));
        
        when(flashSaleRepository.findById(flashSale.getId())).thenReturn(Optional.of(flashSale));

        FlashSaleRequest request = new FlashSaleRequest();
        request.setStartTime(Instant.now().plus(3, ChronoUnit.DAYS));
        request.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS)); // End before start

        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.updateFlashSale(flashSale.getId(), request));

        FlashSaleRequest requestPast = new FlashSaleRequest();
        requestPast.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS)); // Changed to past
        requestPast.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));
        
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.updateFlashSale(flashSale.getId(), requestPast));
    }

    @Test
    void updateFlashSale_notFound_shouldThrow() {
        when(flashSaleRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.updateFlashSale(UUID.randomUUID(), new FlashSaleRequest()));
    }

    @Test
    void deleteFlashSale_shouldDelete() throws CoreThrowHandler {
        FlashSale flashSale = new FlashSale();
        flashSale.setId(UUID.randomUUID());
        
        when(flashSaleRepository.findById(flashSale.getId())).thenReturn(Optional.of(flashSale));
        when(flashSaleItemRepository.countByFlashSaleId(flashSale.getId())).thenReturn(0L);

        adminFlashSaleService.deleteFlashSale(flashSale.getId());
        verify(flashSaleRepository).delete(flashSale);
    }

    @Test
    void deleteFlashSale_hasItems_shouldThrow() {
        FlashSale flashSale = new FlashSale();
        flashSale.setId(UUID.randomUUID());
        
        when(flashSaleRepository.findById(flashSale.getId())).thenReturn(Optional.of(flashSale));
        when(flashSaleItemRepository.countByFlashSaleId(flashSale.getId())).thenReturn(5L);

        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.deleteFlashSale(flashSale.getId()));
    }

    @Test
    void deleteFlashSale_notFound_shouldThrow() {
        when(flashSaleRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> adminFlashSaleService.deleteFlashSale(UUID.randomUUID()));
    }
}
