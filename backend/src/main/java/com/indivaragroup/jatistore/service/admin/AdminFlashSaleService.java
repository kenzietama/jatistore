package com.indivaragroup.jatistore.service.admin;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.dto.request.module.admin.FlashSaleRequest;
import com.indivaragroup.jatistore.dto.response.module.admin.FlashSaleResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.FlashSaleItemRepository;
import com.indivaragroup.jatistore.repository.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminFlashSaleService {
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;

    public Page<FlashSaleResponse> getAllFlashSales(int page, int size, String status, String search, String sortBy, String direction) throws CoreThrowHandler {
        if (page < 0 || size <= 0 || size > 100) {
            throw new CoreThrowHandler(RestApiError.ADM_0004);
        }
        if (status != null && !status.isEmpty() && !status.equals("UPCOMING") && !status.equals("ACTIVE") && !status.equals("ENDED")) {
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
        if (request.getStartTime().isBefore(Instant.now())) {
            throw new CoreThrowHandler(RestApiError.ADM_0018);
        }
        validateTime(request.getStartTime(), request.getEndTime());

        FlashSale flashSale = FlashSale.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        flashSale = flashSaleRepository.save(flashSale);
        return mapToResponse(flashSale);
    }

    public FlashSaleResponse getFlashSaleDetail(UUID id) throws CoreThrowHandler {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.ADM_0016));
        return mapToResponse(flashSale);
    }

    @Transactional
    public void updateFlashSale(UUID id, FlashSaleRequest request) throws CoreThrowHandler {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.ADM_0016));

        if (!flashSale.getStartTime().equals(request.getStartTime()) && request.getStartTime().isBefore(Instant.now())) {
            throw new CoreThrowHandler(RestApiError.ADM_0018);
        }
        validateTime(request.getStartTime(), request.getEndTime());

        flashSale.setName(request.getName());
        flashSale.setStartTime(request.getStartTime());
        flashSale.setEndTime(request.getEndTime());

        flashSaleRepository.save(flashSale);
    }

    @Transactional
    public void deleteFlashSale(UUID id) throws CoreThrowHandler {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.ADM_0016));

        long itemCount = flashSaleItemRepository.countByFlashSaleId(id);
        if (itemCount > 0) {
            throw new CoreThrowHandler(RestApiError.ADM_0017);
        }

        flashSaleRepository.delete(flashSale);
    }

    private void validateTime(Instant startTime, Instant endTime) throws CoreThrowHandler {
        if (endTime.compareTo(startTime) <= 0) {
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
