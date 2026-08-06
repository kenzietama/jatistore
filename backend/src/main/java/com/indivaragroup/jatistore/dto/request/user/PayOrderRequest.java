package com.indivaragroup.jatistore.dto.request.user;

import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Optional: Card ID for saved cards
    private UUID cardId;

    @Size(min = 16, max = 16, message = "Card number must be exactly 16 digits")
    @Pattern(regexp = "\\d{16}", message = "Card number must contain only digits")
    private String cardNumber;

    @Size(max = 100, message = "Card holder name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Card holder name must contain only letters and spaces")
    private String cardHolderName;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Expiry date must be in MM/YY format")
    private String expiryDate;

    @Size(min = 3, max = 4, message = "CVC must be 3 or 4 digits")
    @Pattern(regexp = "\\d{3,4}", message = "CVC must contain only digits")
    private String cvc;
}
