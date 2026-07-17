package com.indivaragroup.jatistore.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthLoginRequest {
    @JsonProperty("email")
    @NotBlank
    private String authLoginRequestEmail;

    @JsonProperty("password")
    @NotBlank
    @Size(min = 4, message = "Minimum 4 characters in password")
    private String authLoginRequestPassword;
}
