package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.PaymentCard;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCardResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.PaymentCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private Principal principal;

    @InjectMocks
    private UserCardService userCardService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
    }

    @Test
    void getUserCards_Success() throws Exception {
        when(securityHelper.getUserByPrincipal(principal)).thenReturn(user);

        PaymentCard card1 = PaymentCard.builder()
                .id(UUID.randomUUID())
                .user(user)
                .cardNumber("1234567812345678")
                .cardHolderName("John Doe")
                .expiryDate("12/28")
                .build();

        PaymentCard card2 = PaymentCard.builder()
                .id(UUID.randomUUID())
                .user(user)
                .cardNumber("9876543298764321")
                .cardHolderName("Jane Doe")
                .expiryDate("11/27")
                .build();

        when(paymentCardRepository.findByUserIdAndDeletedAtIsNull(userId)).thenReturn(List.of(card1, card2));

        RestApiResponse<List<UserCardResponse>> response = userCardService.getUserCards(principal);

        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());
        assertNotNull(response.getRestApiResponseData());
        List<UserCardResponse> cards = response.getRestApiResponseData();
        assertEquals(2, cards.size());
        assertEquals("5678", cards.get(0).getLast4());
        assertEquals("John Doe", cards.get(0).getCardHolderName());
        assertEquals("4321", cards.get(1).getLast4());
        assertEquals("Jane Doe", cards.get(1).getCardHolderName());
    }

    @Test
    void deleteUserCard_Success() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(securityHelper.getUserByPrincipal(principal)).thenReturn(user);

        PaymentCard card = PaymentCard.builder()
                .id(cardId)
                .user(user)
                .cardNumber("1234567812345678")
                .cardHolderName("John Doe")
                .expiryDate("12/28")
                .build();

        when(paymentCardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, userId)).thenReturn(Optional.of(card));

        RestApiResponse<Void> response = userCardService.deleteUserCard(cardId, principal);

        assertNotNull(response);
        assertEquals(200, response.getRestApiResponseHttpCode());

        ArgumentCaptor<PaymentCard> captor = ArgumentCaptor.forClass(PaymentCard.class);
        verify(paymentCardRepository).save(captor.capture());
        assertNotNull(captor.getValue().getDeletedAt());
    }

    @Test
    void deleteUserCard_NotFound_ThrowsException() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(securityHelper.getUserByPrincipal(principal)).thenReturn(user);

        when(paymentCardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, userId)).thenReturn(Optional.empty());

        CoreThrowHandler exception = assertThrows(CoreThrowHandler.class, () -> userCardService.deleteUserCard(cardId, principal));
        assertEquals(RestApiError.USR_0015.getCode(), exception.getCode());
        verify(paymentCardRepository, never()).save(any());
    }
}
