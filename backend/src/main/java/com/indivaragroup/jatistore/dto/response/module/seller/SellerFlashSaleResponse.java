package com.indivaragroup.jatistore.dto.response.module.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SellerFlashSaleResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Available {
        private UUID id;
        private String name;
        private Instant startTime;
        private Instant endTime;
        private String status;
        private Long yourProductCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private UUID itemId;
        private UUID productId;
        private String productName;
        private String productImage;
        private BigDecimal originalPrice;
        private BigDecimal flashPrice;
        private Integer remainingQuota;
        private Double discountPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Wrapper {
        private UUID eventId;
        private String eventName;
        private Instant startTime;
        private Instant endTime;
        
        private List<Item> items;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public static Wrapper from(
                com.indivaragroup.jatistore.data.entity.FlashSale flashSale, 
                Page<Item> springPage) {
                
            return Wrapper.builder()
                    .eventId(flashSale.getId())
                    .eventName(flashSale.getName())
                    .startTime(flashSale.getStartTime())
                    .endTime(flashSale.getEndTime())
                    .items(springPage.getContent())
                    .page(springPage.getNumber())
                    .size(springPage.getSize())
                    .totalElements(springPage.getTotalElements())
                    .totalPages(springPage.getTotalPages())
                    .build();
        }
    }
}
