package com.indivaragroup.jatistore.dto.request.module.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSellerStatusRequest {
    @NotNull(message = "Active status is mandatory")
    private Boolean active;
}
