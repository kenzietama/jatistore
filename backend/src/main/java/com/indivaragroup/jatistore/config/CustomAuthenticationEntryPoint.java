package com.indivaragroup.jatistore.config;

import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver handlerExceptionResolver;

    public CustomAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        // Forward to our advice handler to return AUT_0006 401 Unauthorized
        handlerExceptionResolver.resolveException(request, response, null, new CoreThrowHandler(RestApiError.AUT_0006));
    }
}
