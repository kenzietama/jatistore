package com.indivaragroup.jatistore.controller.user;

import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.user.UserCardResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.service.user.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.USER_BASE_PATH + RestApiPath.USER_CARDS_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserCardController {

    private final UserCardService userCardService;

    @GetMapping
    public RestApiResponse<List<UserCardResponse>> getUserCards(Principal principal) throws CoreThrowHandler {
        return userCardService.getUserCards(principal);
    }

    @DeleteMapping("/{cardId}")
    public RestApiResponse<Void> deleteUserCard(@PathVariable UUID cardId, Principal principal) throws CoreThrowHandler {
        return userCardService.deleteUserCard(cardId, principal);
    }
}
