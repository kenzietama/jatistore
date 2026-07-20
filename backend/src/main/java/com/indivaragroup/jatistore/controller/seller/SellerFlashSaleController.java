package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.dto.request.module.seller.FlashSaleItemRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.SellerFlashSaleResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.seller.SellerFlashSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.indivaragroup.jatistore.audit.Audit;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.SELLER_FLASH_SALES_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerFlashSaleController {

    private final SellerFlashSaleService sellerFlashSaleService;

    @GetMapping("/available")
    public RestApiResponse<List<SellerFlashSaleResponse.Available>> getAvailableFlashSales(Authentication authentication) throws CoreThrowHandler {
        return RestApiResponse.success(sellerFlashSaleService.getAvailableFlashSales(authentication.getName()));
    }

    @GetMapping("/{flashSaleId}/items")
    public RestApiResponse<SellerFlashSaleResponse.Wrapper> getFlashSaleItems(
            Authentication authentication,
            @PathVariable UUID flashSaleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) throws CoreThrowHandler {
        return RestApiResponse.success(sellerFlashSaleService.getFlashSaleItems(authentication.getName(), flashSaleId, page, size));
    }

    @PostMapping("/{flashSaleId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Audit(action = "SELLER_FLASH_SALE_ADD_ITEM", affectedModule = "FLASH_SALES", description = "Seller added a product to flash sale")
    public RestApiResponse<Map<String, UUID>> addFlashSaleItem(
            Authentication authentication,
            @PathVariable UUID flashSaleId,
            @Valid @RequestBody FlashSaleItemRequest request
    ) throws CoreThrowHandler {
        sellerFlashSaleService.addFlashSaleItem(authentication.getName(), flashSaleId, request);
        return RestApiResponse.<Map<String, UUID>>builder()
                .restApiResponseHttpCode(HttpStatus.CREATED.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Product added to flash sale event successfully.")
                .restApiResponseData(Map.of("flashSaleId", flashSaleId))
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(org.slf4j.MDC.get("requestId"))
                .build();
    }

    @DeleteMapping("/{flashSaleId}/items/{productId}")
    @Audit(action = "SELLER_FLASH_SALE_REMOVE_ITEM", affectedModule = "FLASH_SALES", description = "Seller removed a product from flash sale")
    public RestApiResponse<Void> removeFlashSaleItem(
            Authentication authentication,
            @PathVariable UUID flashSaleId,
            @PathVariable UUID productId
    ) throws CoreThrowHandler {
        sellerFlashSaleService.removeFlashSaleItem(authentication.getName(), flashSaleId, productId);
        return RestApiResponse.<Void>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Product removed from flash sale event successfully.")
                .restApiResponseData(null)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(org.slf4j.MDC.get("requestId"))
                .build();
    }

    @PatchMapping("/{flashSaleId}/items/{productId}")
    @Audit(action = "SELLER_FLASH_SALE_UPDATE_ITEM", affectedModule = "FLASH_SALES", description = "Seller updated a flash sale item")
    public RestApiResponse<Void> updateFlashSaleItem(
            Authentication authentication,
            @PathVariable UUID flashSaleId,
            @PathVariable UUID productId,
            @Valid @RequestBody FlashSaleItemRequest request
    ) throws CoreThrowHandler {
        sellerFlashSaleService.updateFlashSaleItem(authentication.getName(), flashSaleId, productId, request);
        return RestApiResponse.<Void>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Flash sale item updated successfully.")
                .restApiResponseData(null)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(org.slf4j.MDC.get("requestId"))
                .build();
    }
}
