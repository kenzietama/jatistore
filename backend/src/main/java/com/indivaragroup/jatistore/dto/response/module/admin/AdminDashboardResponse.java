package com.indivaragroup.jatistore.dto.response.module.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalSellers;
    private long totalProducts;
    private long totalTransactions;
}
