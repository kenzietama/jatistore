package com.indivaragroup.jatistore.dto.utility.validation;

import com.indivaragroup.jatistore.dto.request.user.UserCheckoutRequest;
import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckoutRequestValidator implements ConstraintValidator<ValidCheckoutRequest, UserCheckoutRequest> {

    @Override
    public boolean isValid(UserCheckoutRequest request, ConstraintValidatorContext context) {
        if (request.getUserCheckoutRequestPaymentMethod() == null) {
            return true; // Let @NotBlank handle null payment method
        }
        
        if (request.getUserCheckoutRequestPaymentMethod() == PaymentMethod.CARD) {
            return validateCardFields(request, context);
        }
        
        return true; // Wallet doesn't need card fields
    }
    
    private boolean validateCardFields(UserCheckoutRequest request, ConstraintValidatorContext context) {
        boolean valid = true;
        
        if (isBlank(request.getUserCheckoutRequestCardNumber())) {
            addViolation(context, "userCheckoutRequestCardNumber", "Card number is required when payment method is CARD");
            valid = false;
        } else if (!request.getUserCheckoutRequestCardNumber().matches("^[0-9]{16}$")) {
            addViolation(context, "userCheckoutRequestCardNumber", "Card number must be exactly 16 digits");
            valid = false;
        }
        
        if (isBlank(request.getUserCheckoutRequestCardHolderName())) {
            addViolation(context, "userCheckoutRequestCardHolderName", "Card holder name is required when payment method is CARD");
            valid = false;
        } else if (!request.getUserCheckoutRequestCardHolderName().matches("^[a-zA-Z\\s]+$")) {
            addViolation(context, "userCheckoutRequestCardHolderName", "Card holder name must contain only letters and spaces");
            valid = false;
        } else if (request.getUserCheckoutRequestCardHolderName().length() > 100) {
            addViolation(context, "userCheckoutRequestCardHolderName", "Card holder name cannot exceed 100 characters");
            valid = false;
        }
        
        if (isBlank(request.getUserCheckoutRequestExpiryDate())) {
            addViolation(context, "userCheckoutRequestExpiryDate", "Expiry date is required when payment method is CARD");
            valid = false;
        } else if (!request.getUserCheckoutRequestExpiryDate().matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            addViolation(context, "userCheckoutRequestExpiryDate", "Expiry date must be in MM/YY format");
            valid = false;
        }
        
        if (isBlank(request.getUserCheckoutRequestCvc())) {
            addViolation(context, "userCheckoutRequestCvc", "CVC is required when payment method is CARD");
            valid = false;
        } else if (!request.getUserCheckoutRequestCvc().matches("^[0-9]{3,4}$")) {
            addViolation(context, "userCheckoutRequestCvc", "CVC must be 3 or 4 digits");
            valid = false;
        }
        
        return valid;
    }
    
    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addPropertyNode(field)
               .addConstraintViolation();
    }
    
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
