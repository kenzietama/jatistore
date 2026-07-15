package com.indivaragroup.jatistore.dto.response.module.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSellerResponse {
    private UUID id;
    private String storeName;
    private String sellerName;
    private String email;
    private long productCount;
    private Boolean active;
    private java.time.Instant joinDate;
}
