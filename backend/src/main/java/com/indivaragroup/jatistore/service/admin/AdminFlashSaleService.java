package com.indivaragroup.jatistore.service.admin;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.dto.request.module.admin.FlashSaleRequest;
import com.indivaragroup.jatistore.dto.response.module.admin.FlashSaleResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.FlashSaleItemRepository;
import com.indivaragroup.jatistore.repository.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminFlashSaleService {
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;

    public Page<FlashSaleResponse> getAllFlashSales(int page, int size, String status, String search, String sortBy, String direction) throws CoreThrowHandler {
        log.info("Admin fetching all flash sales. page={}, size={}, status={}, search={}", page, size, status, search);
        if (page < 0 || size <= 0 || size > 100) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            throw new CoreThrowHandler(RestApiError.ADM_0004);
        }
        if (status != null && !status.isEmpty() && !status.equals("UPCOMING") && !status.equals("ACTIVE") && !status.equals("ENDED")) {
            log.warn("Invalid status filter: {}", status);
            throw new CoreThrowHandler(RestApiError.ADM_0013);
        }

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "startTime";
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        
        String searchQuery = (search != null && !search.trim().isEmpty()) ? search : "";
        String statusQuery = (status != null && !status.isEmpty()) ? status : "";
        Page<FlashSale> flashSales = flashSaleRepository.findBySearchAndStatus(searchQuery, statusQuery, Instant.now(), pageRequest);

        return flashSales.map(this::mapToResponse);
    }

    @Transactional
    public FlashSaleResponse createFlashSale(FlashSaleRequest request) throws CoreThrowHandler {
        log.info("Creating new Flash Sale: {}", request.getName());
        if (request.getStartTime().isBefore(Instant.now())) {
            log.error("Flash Sale start time is in the past: {}", request.getStartTime());
            throw new CoreThrowHandler(RestApiError.ADM_0018);
        }
        validateTime(request.getStartTime(), request.getEndTime());

        if (flashSaleRepository.existsOverlappingFlashSale(request.getStartTime(), request.getEndTime())) {
            log.error("Flash Sale overlaps with another flash sale: {} - {}", request.getStartTime(), request.getEndTime());
            throw new CoreThrowHandler(RestApiError.ADM_0019);
        }

        FlashSale flashSale = FlashSale.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        flashSale = flashSaleRepository.save(flashSale);
        log.info("Successfully created Flash Sale {}", flashSale.getId());
        return mapToResponse(flashSale);
    }

    public FlashSaleResponse getFlashSaleDetail(UUID id) throws CoreThrowHandler {
        log.info("Fetching Flash Sale detail: {}", id);
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Flash sale {} not found", id);
                    return new CoreThrowHandler(RestApiError.ADM_0016);
                });
        return mapToResponse(flashSale);
    }

    @Transactional
    public void updateFlashSale(UUID id, FlashSaleRequest request) throws CoreThrowHandler {
        log.info("Updating Flash Sale {}", id);
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Flash sale {} not found", id);
                    return new CoreThrowHandler(RestApiError.ADM_0016);
                });

        if (flashSale.getEndTime().isBefore(Instant.now())) {
            log.error("Cannot edit ended Flash Sale {}", id);
            throw new CoreThrowHandler(RestApiError.ADM_0020);
        }

        if (!flashSale.getStartTime().equals(request.getStartTime()) && request.getStartTime().isBefore(Instant.now())) {
            log.error("Updated start time is in the past: {}", request.getStartTime());
            throw new CoreThrowHandler(RestApiError.ADM_0018);
        }
        validateTime(request.getStartTime(), request.getEndTime());

        if (flashSaleRepository.existsOverlappingFlashSaleExcludeId(request.getStartTime(), request.getEndTime(), id)) {
            log.error("Updated Flash Sale overlaps with another flash sale: {} - {}", request.getStartTime(), request.getEndTime());
            throw new CoreThrowHandler(RestApiError.ADM_0019);
        }

        flashSale.setName(request.getName());
        flashSale.setStartTime(request.getStartTime());
        flashSale.setEndTime(request.getEndTime());

        flashSaleRepository.save(flashSale);
        log.info("Successfully updated Flash Sale {}", id);
    }

    @Transactional
    public void deleteFlashSale(UUID id) throws CoreThrowHandler {
        log.info("Deleting Flash Sale {}", id);
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Flash sale {} not found", id);
                    return new CoreThrowHandler(RestApiError.ADM_0016);
                });

        long itemCount = flashSaleItemRepository.countByFlashSaleId(id);
        if (itemCount > 0) {
            log.warn("Cannot delete Flash Sale {}. It has {} items.", id, itemCount);
            throw new CoreThrowHandler(RestApiError.ADM_0017);
        }

        flashSaleRepository.delete(flashSale);
        log.info("Successfully deleted Flash Sale {}", id);
    }

    private void validateTime(Instant startTime, Instant endTime) throws CoreThrowHandler {
        if (endTime.compareTo(startTime) <= 0) {
            log.error("Invalid time: endTime {} must be after startTime {}", endTime, startTime);
            throw new CoreThrowHandler(RestApiError.ADM_0014);
        }
    }

    private FlashSaleResponse mapToResponse(FlashSale flashSale) {
        Instant now = Instant.now();
        String status;
        if (now.isBefore(flashSale.getStartTime())) {
            status = "UPCOMING";
        } else if (now.isAfter(flashSale.getEndTime())) {
            status = "ENDED";
        } else {
            status = "ACTIVE";
        }

        long itemCount = flashSaleItemRepository.countByFlashSaleId(flashSale.getId());

        return FlashSaleResponse.builder()
                .id(flashSale.getId())
                .name(flashSale.getName())
                .startTime(flashSale.getStartTime())
                .endTime(flashSale.getEndTime())
                .status(status)
                .itemCount(itemCount)
                .build();
    }
}
