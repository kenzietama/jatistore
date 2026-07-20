package com.indivaragroup.jatistore.controller.admin;

import com.indivaragroup.jatistore.audit.Audit;
import com.indivaragroup.jatistore.dto.request.module.admin.FlashSaleRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.admin.FlashSaleResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.admin.AdminFlashSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + "/admin/flash-sales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFlashSaleController {

    private final AdminFlashSaleService flashSaleService;

    @GetMapping
    public RestApiResponse<PageData<FlashSaleResponse>> getFlashSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) throws CoreThrowHandler {

        Page<FlashSaleResponse> flashSales = flashSaleService.getAllFlashSales(page, size, status, search, sortBy, direction);
        return RestApiResponse.success(PageData.from(flashSales));
    }

    @PostMapping
    @Audit(action = "FLASH_SALE_CREATE", affectedModule = "FLASH_SALES", description = "Admin created flash sale event")
    public ResponseEntity<RestApiResponse<FlashSaleResponse>> createFlashSale(@Valid @RequestBody FlashSaleRequest request) throws CoreThrowHandler {
        FlashSaleResponse response = flashSaleService.createFlashSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RestApiResponse.success(response));
    }

    @GetMapping("/{flashSaleId}")
    public RestApiResponse<FlashSaleResponse> getFlashSaleDetail(@PathVariable UUID flashSaleId) throws CoreThrowHandler {
        return RestApiResponse.success(flashSaleService.getFlashSaleDetail(flashSaleId));
    }

    @PutMapping("/{flashSaleId}")
    @Audit(action = "FLASH_SALE_UPDATE", affectedModule = "FLASH_SALES", description = "Admin updated flash sale event")
    public RestApiResponse<Void> updateFlashSale(
            @PathVariable UUID flashSaleId,
            @Valid @RequestBody FlashSaleRequest request) throws CoreThrowHandler {
        flashSaleService.updateFlashSale(flashSaleId, request);
        return RestApiResponse.success(null);
    }

    @DeleteMapping("/{flashSaleId}")
    @Audit(action = "FLASH_SALE_DELETE", affectedModule = "FLASH_SALES", description = "Admin deleted flash sale event")
    public RestApiResponse<Void> deleteFlashSale(@PathVariable UUID flashSaleId) throws CoreThrowHandler {
        flashSaleService.deleteFlashSale(flashSaleId);
        return RestApiResponse.success(null);
    }
}
