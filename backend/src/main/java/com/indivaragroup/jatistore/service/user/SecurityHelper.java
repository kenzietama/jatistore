package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final AuthRepository authRepository;

    public User getUserByPrincipal(Principal principal) throws CoreThrowHandler {
        if (principal == null || principal.getName() == null) {
            throw new CoreThrowHandler(RestApiError.USR_0006);
        }
        return authRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.GEN_0005));
    }

    public String getEmailFromPrincipal(Principal principal) throws CoreThrowHandler {
        if (principal == null || principal.getName() == null) {
            throw new CoreThrowHandler(RestApiError.USR_0006);
        }
        return principal.getName();
    }
}
