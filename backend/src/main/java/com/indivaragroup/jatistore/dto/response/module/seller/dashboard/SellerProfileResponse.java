package com.indivaragroup.jatistore.dto.response.module.seller.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerProfileResponse {
    private String storeName;
    private String storeImage;
    private String email;
}
