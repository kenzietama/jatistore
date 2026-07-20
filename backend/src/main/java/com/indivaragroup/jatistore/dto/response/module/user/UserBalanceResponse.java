package com.indivaragroup.jatistore.dto.response.module.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBalanceResponse {
    private BigDecimal balance;
}
