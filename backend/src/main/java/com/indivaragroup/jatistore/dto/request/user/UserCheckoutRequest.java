package com.indivaragroup.jatistore.dto.request.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import com.indivaragroup.jatistore.dto.utility.validation.ValidCheckoutRequest;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@ValidCheckoutRequest
public class UserCheckoutRequest {

    @JsonProperty("selected_cart_items_id")
    @NotEmpty(message = "Selected cart items cannot be empty")
    private UUID[] userSelectedCartItemId;

    @JsonProperty("payment_method")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Payment method is required")
    private PaymentMethod userCheckoutRequestPaymentMethod;

    @JsonProperty("card_number")
    private String userCheckoutRequestCardNumber;

    @JsonProperty("card_holder_name")
    private String userCheckoutRequestCardHolderName;

    @JsonProperty("expiry_date")
    private String userCheckoutRequestExpiryDate;

    @JsonProperty("cvc")
    private String userCheckoutRequestCvc;

}
