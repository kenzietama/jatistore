package com.indivaragroup.jatistore.config;

import com.indivaragroup.jatistore.data.entity.Token;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.TokenRepository;
import com.indivaragroup.jatistore.service.utility.AuthJWTUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.time.Instant;
import java.util.Optional;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final AuthJWTUtility authJWTUtility;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final TokenRepository tokenRepository;

    public JWTAuthenticationFilter(
            AuthJWTUtility authJWTUtility,
            @Lazy UserDetailsService userDetailsService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
            TokenRepository tokenRepository
    ) {
        this.authJWTUtility = authJWTUtility;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || authHeader.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!authHeader.startsWith("Bearer ")) {
                throw new CoreThrowHandler(RestApiError.AUT_0006);  // invalid header format
            }

            String jwt = authHeader.substring(7).trim();
            if (jwt.isEmpty()) {
                throw new CoreThrowHandler(RestApiError.AUT_0006);
            }

            authJWTUtility.verifyToken(jwt); // check AUT_0007 & AUT_0008

            Optional<Token> tokenEntity = tokenRepository.findByToken(jwt);
            if (tokenEntity.isEmpty() || (tokenEntity.get().getExpiresAt() != null && tokenEntity.get().getExpiresAt().isBefore(Instant.now()))) {
                throw new CoreThrowHandler(RestApiError.AUT_0009); // session not found / already logged out / expired
            }

            String email = authJWTUtility.resolveSubjectFromEncryptedToken(jwt);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
