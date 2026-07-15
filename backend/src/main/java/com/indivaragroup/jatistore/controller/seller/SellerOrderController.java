package com.indivaragroup.jatistore.controller.seller;

import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderDetailResponse;
import com.indivaragroup.jatistore.dto.response.module.seller.order.SellerOrderListResponse;
import com.indivaragroup.jatistore.dto.response.utility.PageData;
import com.indivaragroup.jatistore.service.seller.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;
    private final AuthRepository authRepository;
    private final SellerRepository sellerRepository;
    
    private UUID getSellerIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = authRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a registered seller"));
        return seller.getId();
    }

    @GetMapping
    public ResponseEntity<RestApiResponse<PageData<SellerOrderListResponse>>> getOrders(
            Principal principal,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
            
        Page<SellerOrderListResponse> orders = sellerOrderService.getSellerOrders(getSellerIdFromPrincipal(principal), status, page, size);
        
        RestApiResponse<PageData<SellerOrderListResponse>> response = RestApiResponse.<PageData<SellerOrderListResponse>>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Orders retrieved successfully.")
                .restApiResponseData(PageData.from(orders))
                .restApiResponseTimestamp(Instant.now())
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<RestApiResponse<SellerOrderDetailResponse>> getOrderDetail(Principal principal, @PathVariable UUID orderId) {
        SellerOrderDetailResponse detail = sellerOrderService.getSellerOrderDetail(getSellerIdFromPrincipal(principal), orderId);
        
        RestApiResponse<SellerOrderDetailResponse> response = RestApiResponse.<SellerOrderDetailResponse>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Order detail retrieved successfully.")
                .restApiResponseData(detail)
                .restApiResponseTimestamp(Instant.now())
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<RestApiResponse<Void>> markAsShipped(Principal principal, @PathVariable UUID orderId) {
        sellerOrderService.markOrderAsShipped(getSellerIdFromPrincipal(principal), orderId);
        
        RestApiResponse<Void> response = RestApiResponse.<Void>builder()
                .restApiResponseHttpCode(HttpStatus.OK.value())
                .restApiResponseHttpStatus("SUCCESS")
                .restApiResponseMessage("Order marked as shipped successfully.")
                .restApiResponseData(null)
                .restApiResponseTimestamp(Instant.now())
                .build();
                
        return ResponseEntity.ok(response);
    }
}
