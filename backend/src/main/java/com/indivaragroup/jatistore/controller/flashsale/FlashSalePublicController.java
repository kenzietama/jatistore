package com.indivaragroup.jatistore.controller.flashsale;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.service.flashsale.FlashSaleService;
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
        log.info("Received request to fetch active flash sale data.");

        FlashSale activeFlashSale = flashSaleService.getActiveFlashSaleEvent();

        return RestApiResponse.<FlashSale>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage("Active flash sale successfully retrieved.")
                .restApiResponseData(activeFlashSale)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }

    @GetMapping("/upcoming")
    public RestApiResponse<FlashSale> getUpcomingFlashSale() {
        log.info("Received request to fetch upcoming flash sale data.");

        FlashSale upcomingFlashSale = flashSaleService.getUpcomingFlashSale();

        return RestApiResponse.<FlashSale>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage(upcomingFlashSale != null ? "Upcoming flash sale successfully retrieved." : "No upcoming flash sale available.")
                .restApiResponseData(upcomingFlashSale)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();
    }
}