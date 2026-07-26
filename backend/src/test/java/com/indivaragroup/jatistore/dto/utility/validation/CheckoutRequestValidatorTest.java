package com.indivaragroup.jatistore.dto.utility.validation;

import com.indivaragroup.jatistore.data.utility.constant.PaymentMethod;
import com.indivaragroup.jatistore.dto.request.user.UserCheckoutRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutRequestValidatorTest {

    private CheckoutRequestValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new CheckoutRequestValidator();
    }

    private void mockContext() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void isValid_nullPaymentMethod_shouldReturnTrue() {
        UserCheckoutRequest request = new UserCheckoutRequest();
        assertTrue(validator.isValid(request, context));
    }

    @Test
    void isValid_walletPaymentMethod_shouldReturnTrue() {
        UserCheckoutRequest request = new UserCheckoutRequest();
        request.setUserCheckoutRequestPaymentMethod(PaymentMethod.WALLET);
        assertTrue(validator.isValid(request, context));
    }

    @Test
    void isValid_cardPaymentMethod_validFields_shouldReturnTrue() {
        UserCheckoutRequest request = new UserCheckoutRequest();
        request.setUserCheckoutRequestPaymentMethod(PaymentMethod.CARD);
        request.setUserCheckoutRequestCardNumber("1234567890123456");
        request.setUserCheckoutRequestCardHolderName("John Doe");
        request.setUserCheckoutRequestExpiryDate("12/25");
        request.setUserCheckoutRequestCvc("123");

        assertTrue(validator.isValid(request, context));
    }

    @Test
    void isValid_cardPaymentMethod_invalidCardNumber_shouldReturnFalse() {
        mockContext();
        UserCheckoutRequest request = new UserCheckoutRequest();
        request.setUserCheckoutRequestPaymentMethod(PaymentMethod.CARD);
        request.setUserCheckoutRequestCardNumber("123"); // invalid

        assertFalse(validator.isValid(request, context));
        
        request.setUserCheckoutRequestCardNumber(null); // blank
        assertFalse(validator.isValid(request, context));
    }

    @Test
    void isValid_cardPaymentMethod_invalidCardHolderName_shouldReturnFalse() {
        mockContext();
        UserCheckoutRequest request = new UserCheckoutRequest();
        request.setUserCheckoutRequestPaymentMethod(PaymentMethod.CARD);
        request.setUserCheckoutRequestCardNumber("1234567890123456");
        
        request.setUserCheckoutRequestCardHolderName("John123"); // invalid
        assertFalse(validator.isValid(request, context));

        request.setUserCheckoutRequestCardHolderName(null); // blank
        assertFalse(validator.isValid(request, context));
        
        String longName = "a".repeat(101);
        request.setUserCheckoutRequestCardHolderName(longName);
        assertFalse(validator.isValid(request, context));
    }

    @Test
    void isValid_cardPaymentMethod_invalidExpiryDate_shouldReturnFalse() {
        mockContext();
        UserCheckoutRequest request = new UserCheckoutRequest();
        request.setUserCheckoutRequestPaymentMethod(PaymentMethod.CARD);
        request.setUserCheckoutRequestCardNumber("1234567890123456");
        request.setUserCheckoutRequestCardHolderName("John Doe");

        request.setUserCheckoutRequestExpiryDate("13/25"); // invalid month
        assertFalse(validator.isValid(request, context));

        request.setUserCheckoutRequestExpiryDate(null); // blank
        assertFalse(validator.isValid(request, context));
    }

    @Test
    void isValid_cardPaymentMethod_invalidCvc_shouldReturnFalse() {
        mockContext();
        UserCheckoutRequest request = new UserCheckoutRequest();
        request.setUserCheckoutRequestPaymentMethod(PaymentMethod.CARD);
        request.setUserCheckoutRequestCardNumber("1234567890123456");
        request.setUserCheckoutRequestCardHolderName("John Doe");
        request.setUserCheckoutRequestExpiryDate("12/25");

        request.setUserCheckoutRequestCvc("12"); // invalid length
        assertFalse(validator.isValid(request, context));

        request.setUserCheckoutRequestCvc(null); // blank
        assertFalse(validator.isValid(request, context));
    }
}
