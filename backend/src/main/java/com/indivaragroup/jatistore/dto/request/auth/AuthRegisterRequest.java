package com.indivaragroup.jatistore.dto.request.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
        message = "Password must be at least 8 characters with 1 uppercase, 1 lowercase, 1 digit"
    )
    private String password;

    @NotBlank(message = "Username is required")
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]{4,20}$",
        message = "Username must be 4-20 characters, alphanumeric with underscore/dash only"
    )
    private String username;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\d{10,20}$",
        message = "Phone number must be 10-20 digits only"
    )
    private String phoneNumber;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be 2-100 characters")
    private String fullName;

    private LocalDate dateOfBirth;
}
