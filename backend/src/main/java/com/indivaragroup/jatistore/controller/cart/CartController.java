package com.indivaragroup.jatistore.controller.cart;

import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.cart.AddToCartRequest;
import com.indivaragroup.jatistore.dto.request.cart.UpdateCartItemQuantityRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.cart.CartResponse;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.CART_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
public class CartController {

    private final CartService cartService;
    private final AuthRepository authRepository;

    @GetMapping
    public ResponseEntity<RestApiResponse<CartResponse>> getCart(
            Principal principal) {

        String email = principal.getName();
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0006));

        CartResponse cartResponse = cartService.getCartByUserId(user.getId());

        RestApiResponse<CartResponse> response = RestApiResponse.<CartResponse>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus(HttpStatus.OK.name())
                .restApiResponseMessage(cartResponse.getItems().isEmpty()
                        ? "Your cart is empty."
                        : "Cart retrieved successfully.")
                .restApiResponseData(cartResponse)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping(RestApiPath.CART_ADD_ITEM_PATH)
    public ResponseEntity<RestApiResponse<UUID>> addToCart(
            Principal principal,
            @RequestBody AddToCartRequest request) {

        String email = principal.getName();
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0006));

        CartItem savedItem = cartService.addToCart(user.getId(), request);

        RestApiResponse<UUID> response = RestApiResponse.<UUID>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus(HttpStatus.OK.name())
                .restApiResponseMessage("Product added to cart successfully.")
                .restApiResponseData(savedItem.getId())
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping(RestApiPath.CART_UPDATE_ITEM_PATH)
    public ResponseEntity<RestApiResponse<Void>> updateCartItemQuantity(
            Principal principal,
            @PathVariable UUID cartItemId,
            @RequestBody UpdateCartItemQuantityRequest request) {

        String email = principal.getName();
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0006));

        cartService.updateCartItemQuantity(user.getId(), cartItemId, request);

        RestApiResponse<Void> response = RestApiResponse.<Void>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus(HttpStatus.OK.name())
                .restApiResponseMessage("Cart item updated successfully.")
                .restApiResponseData(null)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(RestApiPath.CART_DELETE_ITEM_PATH)
    public ResponseEntity<RestApiResponse<Void>> removeItem(
            Principal principal,
            @PathVariable UUID cartItemId) {

        String email = principal.getName();
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.USR_0006));

        cartService.removeItemFromCart(user.getId(), cartItemId);

        RestApiResponse<Void> response = RestApiResponse.<Void>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus(HttpStatus.OK.name())
                .restApiResponseMessage("Cart item removed successfully.")
                .restApiResponseData(null)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId(MDC.get("requestId"))
                .build();

        return ResponseEntity.ok(response);
    }
}