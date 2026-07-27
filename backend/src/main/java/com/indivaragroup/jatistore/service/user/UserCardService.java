package com.indivaragroup.jatistore.service.user;

import com.indivaragroup.jatistore.data.entity.PaymentCard;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCardResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.PaymentCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final AuthRepository authRepository;
    private final SecurityHelper securityHelper;

    @Transactional(readOnly = true)
    public RestApiResponse<List<UserCardResponse>> getUserCards(Principal principal) throws CoreThrowHandler {
        User user = securityHelper.getUserByPrincipal(principal);
        log.info("Fetching payment cards for user id: {}", user.getId());

        List<PaymentCard> cards = paymentCardRepository.findByUserIdAndDeletedAtIsNull(user.getId());

        List<UserCardResponse> cardResponses = cards.stream()
                .map(card -> {
                    String cardNumber = card.getCardNumber();
                    String last4 = (cardNumber != null && cardNumber.length() >= 4)
                            ? cardNumber.substring(cardNumber.length() - 4)
                            : cardNumber;
                    return UserCardResponse.builder()
                            .id(card.getId())
                            .last4(last4)
                            .cardHolderName(card.getCardHolderName())
                            .expiryDate(card.getExpiryDate())
                            .build();
                })
                .toList();

        return RestApiResponse.success(cardResponses);
    }

    @Transactional
    public RestApiResponse<Void> deleteUserCard(UUID cardId, Principal principal) throws CoreThrowHandler {
        User user = securityHelper.getUserByPrincipal(principal);
        log.info("Deleting payment card id: {} for user id: {}", cardId, user.getId());

        PaymentCard card = paymentCardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, user.getId())
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0015));

        card.setDeletedAt(Instant.now());
        paymentCardRepository.save(card);

        return RestApiResponse.success(null);
    }
}
