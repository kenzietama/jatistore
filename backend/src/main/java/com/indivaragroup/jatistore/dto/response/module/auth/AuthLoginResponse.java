package com.indivaragroup.jatistore.dto.response.module.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthLoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private int expiresIn;
    private String role;
}
