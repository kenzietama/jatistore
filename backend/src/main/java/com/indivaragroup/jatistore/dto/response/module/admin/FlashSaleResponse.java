package com.indivaragroup.jatistore.dto.response.module.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleResponse {

    private UUID id;
    private String name;
    private Instant startTime;
    private Instant endTime;
    private String status;
    private long itemCount;
}
