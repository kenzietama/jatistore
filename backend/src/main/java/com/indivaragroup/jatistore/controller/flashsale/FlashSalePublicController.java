package com.indivaragroup.jatistore.controller.flashsale;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.service.flashsale.FlashSaleService; // Import service
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/public/flash-sale")
@RequiredArgsConstructor
@Slf4j
public class FlashSalePublicController {

    private final FlashSaleService flashSaleService;

    @GetMapping("/active")
    public RestApiResponse<FlashSale> getActiveFlashSale() {
        log.info("Menerima permintaan untuk mengambil data flash sale aktif.");

        FlashSale activeFlashSale = flashSaleService.getActiveFlashSaleEvent();

        return RestApiResponse.<FlashSale>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage("Flash sale aktif berhasil diambil.")
                .restApiResponseData(activeFlashSale)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }
}