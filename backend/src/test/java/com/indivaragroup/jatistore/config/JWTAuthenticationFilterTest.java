package com.indivaragroup.jatistore.config;

import com.indivaragroup.jatistore.data.entity.Token;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.TokenRepository;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTAuthenticationFilterTest {

    @Mock
    private AuthJWTUtility authJWTUtility;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withoutAuthHeader_shouldContinueChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authJWTUtility, tokenRepository, userDetailsService);
    }

    @Test
    void doFilterInternal_withInvalidHeaderFormat_shouldThrowException() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat xyz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(CoreThrowHandler.class));
        verifyNoInteractions(authJWTUtility, tokenRepository, userDetailsService);
    }

    @Test
    void doFilterInternal_withValidHeader_butNotInDataBase_shouldThrowException() throws Exception {
        String token = "my-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        doNothing().when(authJWTUtility).verifyToken(token);
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), any(CoreThrowHandler.class));
    }

    @Test
    void doFilterInternal_withValidHeader_andInDataBase_shouldAuthenticate() throws Exception {
        String tokenStr = "my-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenStr);
        doNothing().when(authJWTUtility).verifyToken(tokenStr);
        
        Token tokenEntity = new Token();
        tokenEntity.setToken(tokenStr);
        when(tokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(tokenEntity));
        
        String email = "test@example.com";
        when(authJWTUtility.resolveSubjectFromEncryptedToken(tokenStr)).thenReturn(email);
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert(auth != null);
        assert(auth.getPrincipal().equals(userDetails));
    }
}
