package com.indivaragroup.jatistore.dto.request.module.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleRequest {

    @NotBlank(message = "Flash sale name is mandatory")
    @Size(max = 50, message = "Flash sale name must be less than 50 characters")
    private String name;

    @NotNull(message = "Start time is mandatory")
    private Instant startTime;

    @NotNull(message = "End time is mandatory")
    private Instant endTime;
}
