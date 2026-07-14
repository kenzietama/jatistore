package com.indivaragroup.jatistore.controller.handler;

import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestControllerAdviceHandlerTest {

    private RestControllerAdviceHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestControllerAdviceHandler();
    }

    @Test
    void handleValidationException_NotBlankCode() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "authLoginRequestEmail", null, false, new String[]{"NotBlank"}, null, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing mandatory property email", response.getBody().getRestApiResponseError().get("email"));
    }

    @Test
    void handleValidationException_SizeWithMaxLimit() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Object[] valArgs = new Object[]{ null, 4, 0 }; // max=4, min=0
        FieldError fieldError = new FieldError("objectName", "authLoginRequestPassword", null, false, new String[]{"Size"}, valArgs, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals("Maximum length for property password is 4", response.getBody().getRestApiResponseError().get("password"));
    }

    @Test
    void handleValidationException_SizeWithMinLimit() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Object[] valArgs = new Object[]{ null, null, 8 }; // max=null, min=8
        FieldError fieldError = new FieldError("objectName", "authLoginRequestPassword", null, false, new String[]{"Size"}, valArgs, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals("Maximum length for property password is 8", response.getBody().getRestApiResponseError().get("password"));
    }

    @Test
    void handleHttpMessageNotReadable_WithPropertyInMessage() {
        // Arrange
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Cannot deserialize value ... [\"password\"] ...");

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleHttpMessageNotReadable(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data type for property password", response.getBody().getRestApiResponseMessage());
    }

    @Test
    void handleHttpMessageNotReadable_WithoutPropertyInMessage() {
        // Arrange
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Invalid JSON format");

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleHttpMessageNotReadable(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data type for property payload", response.getBody().getRestApiResponseMessage());
    }

    @Test
    void handleCoreThrowHandler_WithEmptyError() {
        // Arrange
        CoreThrowHandler ex = new CoreThrowHandler(RestApiError.AUT_0004);

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleCoreThrowHandler(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().getRestApiResponseHttpStatus());
        assertNull(response.getBody().getRestApiResponseError());
    }

    @Test
    void handleCoreThrowHandler_WithPopulatedError() {
        // Arrange
        Map<String, Serializable> errorMap = Map.of("email", "Already exists");
        CoreThrowHandler ex = new CoreThrowHandler(RestApiError.AUT_0004, errorMap);

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleCoreThrowHandler(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody().getRestApiResponseError());
        assertEquals("Already exists", response.getBody().getRestApiResponseError().get("email"));
    }

    @Test
    void handleCoreThrowHandler_WithInvalidHttpStatus() {
        // Arrange: status code 999 is invalid/non-existent in Spring HttpStatus enum, pass non-null map to cover constructor branch
        CoreThrowHandler ex = new CoreThrowHandler(999, "Custom generic error", Map.of("key", "val"));

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleCoreThrowHandler(ex);

        // Assert
        assertNotNull(response);
        assertEquals(999, response.getStatusCode().value());
        assertEquals("ERROR", response.getBody().getRestApiResponseHttpStatus());
        assertEquals("val", response.getBody().getRestApiResponseError().get("key"));
    }

    @Test
    void handleValidationException_WithBlankField_ShouldReturn400() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError blankField = mock(FieldError.class);
        when(blankField.getField()).thenReturn("");
        when(blankField.getCode()).thenReturn("NotBlank");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(blankField));

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleValidationException_WithNullField_ShouldThrowNullPointerException() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError nullField = mock(FieldError.class);
        when(nullField.getField()).thenReturn(null);
        when(nullField.getCode()).thenReturn("NotBlank");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(nullField));

        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            handler.handleValidationException(ex)
        );
    }

    @Test
    void handleValidationException_WithFieldWithoutRequestSuffix() {
        // Arrange: Field does not contain "Request" (e.g. just "email")
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "email", null, false, new String[]{"NotBlank"}, null, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals("Missing mandatory property email", response.getBody().getRestApiResponseError().get("email"));
    }

    @Test
    void handleHttpMessageNotReadable_WithNullMessage() {
        // Arrange
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn(null);

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleHttpMessageNotReadable(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data type for property payload", response.getBody().getRestApiResponseMessage());
    }

    @Test
    void handleServletException() {
        // Arrange
        ServletException ex = new ServletException("Filter authentication failed");

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleServletException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Filter authentication failed", response.getBody().getRestApiResponseMessage());
    }

    @Test
    void handleNoResourceFoundException() {
        // Arrange
        NoResourceFoundException ex = mock(NoResourceFoundException.class);
        when(ex.getMessage()).thenReturn("Resource not found");

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleNoResourceFoundException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().getRestApiResponseMessage());
    }

    @Test
    void handleAnyThrowable() {
        // Arrange
        Throwable ex = new RuntimeException("Unexpected db crash");

        // Act
        ResponseEntity<RestApiResponse<Void>> response = handler.handleAnyThrowable(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody().getRestApiResponseError().get("errorId"));
    }

    @Test
    void handleValidationException_NotEmptyCode_ShouldReturnGen0001() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "email", null, false, new String[]{"NotEmpty"}, null, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals("Missing mandatory property email", response.getBody().getRestApiResponseError().get("email"));
    }

    @Test
    void handleValidationException_LengthCode_ShouldReturnGen0003() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Object[] valArgs = new Object[]{ null, 10, 0 };
        FieldError fieldError = new FieldError("objectName", "password", null, false, new String[]{"Length"}, valArgs, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals("Maximum length for property password is 10", response.getBody().getRestApiResponseError().get("password"));
    }

    @Test
    void handleValidationException_UnmappedCode_ShouldReturnGen0002() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "email", null, false, new String[]{"Pattern"}, null, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals("Invalid data type for property email", response.getBody().getRestApiResponseError().get("email"));
    }

    @Test
    void handleValidationException_SizeWithNullArguments() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "password", null, false, new String[]{"Size"}, null, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals("Maximum length for property password is unknown", response.getBody().getRestApiResponseError().get("password"));
    }

    @Test
    void handleValidationException_SizeWithShortArguments() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "password", null, false, new String[]{"Size"}, new Object[]{null}, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals("Maximum length for property password is unknown", response.getBody().getRestApiResponseError().get("password"));
    }

    @Test
    void handleHttpMessageNotReadable_WithUnclosedPropertyInMessage() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Cannot deserialize value ... [\"password");

        ResponseEntity<RestApiResponse<Void>> response = handler.handleHttpMessageNotReadable(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data type for property payload", response.getBody().getRestApiResponseMessage());
    }

    @Test
    void handleValidationException_NotNullCode_ShouldReturnGen0001() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "email", null, false, new String[]{"NotNull"}, null, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals("Missing mandatory property email", response.getBody().getRestApiResponseError().get("email"));
    }

    @Test
    void handleValidationException_MaxCode_ShouldReturnGen0003() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Object[] valArgs = new Object[]{ null, 10, 0 };
        FieldError fieldError = new FieldError("objectName", "password", null, false, new String[]{"Max"}, valArgs, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals("Maximum length for property password is 10", response.getBody().getRestApiResponseError().get("password"));
    }

    @Test
    void handleValidationException_SizeWithIntegerMaxLimit() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        Object[] valArgs = new Object[]{ null, Integer.MAX_VALUE, 8 };
        FieldError fieldError = new FieldError("objectName", "authLoginRequestPassword", null, false, new String[]{"Size"}, valArgs, "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<RestApiResponse<Void>> response = handler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals("Maximum length for property password is 8", response.getBody().getRestApiResponseError().get("password"));
    }

    @Test
    void handleCoreThrowHandler_WithGenericConstructorNullError() {
        CoreThrowHandler ex = new CoreThrowHandler(400, "Generic message", null);

        ResponseEntity<RestApiResponse<Void>> response = handler.handleCoreThrowHandler(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody().getRestApiResponseError());
    }
}
