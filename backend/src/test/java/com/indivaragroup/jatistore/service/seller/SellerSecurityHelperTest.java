package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SellerSecurityHelperTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerSecurityHelper sellerSecurityHelper;

    @Test
    void getSellerIdFromPrincipal_shouldReturnId() throws CoreThrowHandler {
        Principal principal = () -> "seller@test.com";

        User user = new User();
        user.setId(UUID.randomUUID());
        when(authRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(user));

        Seller seller = new Seller();
        seller.setId(UUID.randomUUID());
        when(sellerRepository.findByUserId(user.getId())).thenReturn(Optional.of(seller));

        UUID sellerId = sellerSecurityHelper.getSellerIdFromPrincipal(principal);
        assertEquals(seller.getId(), sellerId);
    }

    @Test
    void getSellerIdFromPrincipal_userNotFound_shouldThrow() {
        Principal principal = () -> "seller@test.com";
        when(authRepository.findByEmail("seller@test.com")).thenReturn(Optional.empty());

        assertThrows(CoreThrowHandler.class, () -> sellerSecurityHelper.getSellerIdFromPrincipal(principal));
    }

    @Test
    void getSellerIdFromPrincipal_sellerNotFound_shouldThrow() {
        Principal principal = () -> "seller@test.com";

        User user = new User();
        user.setId(UUID.randomUUID());
        when(authRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(user));
        when(sellerRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        assertThrows(CoreThrowHandler.class, () -> sellerSecurityHelper.getSellerIdFromPrincipal(principal));
    }

    @Test
    void getSellerIdFromPrincipal_nullPrincipal_shouldThrow() {
        assertThrows(CoreThrowHandler.class, () -> sellerSecurityHelper.getSellerIdFromPrincipal(null));
    }
}
