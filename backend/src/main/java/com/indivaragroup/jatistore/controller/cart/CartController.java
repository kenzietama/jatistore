package com.indivaragroup.jatistore.controller.cart;

import com.indivaragroup.jatistore.data.entity.CartItem;
import com.indivaragroup.jatistore.dto.request.cart.AddToCartRequest;
import com.indivaragroup.jatistore.dto.response.RestApiPath;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(RestApiPath.BASE_PATH + RestApiPath.CART_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping(RestApiPath.CART_ADD_ITEM_PATH) // Menghasilkan POST /api/v1/carts/items
    public RestApiResponse<CartItem> addToCart(@RequestBody AddToCartRequest request) {
        log.info("Menerima permintaan REST untuk menambah produk ke keranjang belanja");

        // CATATAN SEMENTARA: Karena login session/JWT belum dikoneksikan ke AuthenticationContext Spring Security,
        // Kita gunakan ID User Demo dari database Postgres kita kemarin untuk uji coba.
        UUID mockUserId = UUID.fromString("b0000000-0000-0000-0000-000000000001");

        CartItem savedItem = cartService.addToCart(mockUserId, request);

        return RestApiResponse.<CartItem>builder()
                .restApiResponseHttpCode(200)
                .restApiResponseHttpStatus("OK")
                .restApiResponseMessage("Produk berhasil ditambahkan ke keranjang!")
                .restApiResponseData(savedItem)
                .restApiResponseTimestamp(Instant.now())
                .restApiResponseRequestId("REQ-CART-" + System.currentTimeMillis())
                .build();
    }

    @GetMapping
    public ResponseEntity<RestApiResponse<List<CartItem>>> getCart() {
        UUID mockUserId = UUID.fromString("b0000000-0000-0000-0000-000000000001");

        List<CartItem> cartItems = cartService.getCartByUserId(mockUserId);

        RestApiResponse<List<CartItem>> response = RestApiResponse.<List<CartItem>>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus(HttpStatus.OK.name())
                .restApiResponseMessage("Berhasil mengambil data keranjang belanja")
                .restApiResponseData(cartItems)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<RestApiResponse<String>> removeItem(@PathVariable UUID cartItemId) {
        log.info("Menerima permintaan REST untuk menghapus item keranjang belanja dengan ID: {}", cartItemId);

        UUID mockUserId = UUID.fromString("b0000000-0000-0000-0000-000000000001");

        // Panggil service untuk menghapus data item keranjang
        cartService.removeItemFromCart(mockUserId, cartItemId);

        RestApiResponse<String> response = RestApiResponse.<String>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus(HttpStatus.OK.name())
                .restApiResponseMessage("Barang berhasil dihapus dari keranjang!")
                .restApiResponseData("Item " + cartItemId + " Deleted")
                .build();

        return ResponseEntity.ok(response);
    }
}