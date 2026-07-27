package com.indivaragroup.jatistore.dto.response.module.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCardResponse {
    private UUID id;
    private String last4;
    private String cardHolderName;
    private String expiryDate;
}
