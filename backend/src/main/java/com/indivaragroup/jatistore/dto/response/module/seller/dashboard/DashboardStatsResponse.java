package com.indivaragroup.jatistore.dto.response.module.seller.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {
    private String sellerName;
    private long totalOrders;
    private long totalProducts;
}
