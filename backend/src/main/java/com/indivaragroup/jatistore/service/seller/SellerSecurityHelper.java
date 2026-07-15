package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SellerSecurityHelper {

    private final AuthRepository authRepository;
    private final SellerRepository sellerRepository;

    public UUID getSellerIdFromPrincipal(Principal principal) throws CoreThrowHandler {
        if (principal == null) {
            throw new CoreThrowHandler(RestApiError.AUT_0006);
        }
        User user = authRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.AUT_0006));
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0002));
        return seller.getId();
    }
}
