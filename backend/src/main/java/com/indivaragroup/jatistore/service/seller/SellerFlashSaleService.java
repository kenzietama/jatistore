package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.data.entity.FlashSaleItem;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.module.seller.FlashSaleItemRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.SellerFlashSaleResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.FlashSaleItemRepository;
import com.indivaragroup.jatistore.repository.FlashSaleRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerFlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final ProductRepository productRepository;
    private final AuthRepository authRepository;
    private final SellerRepository sellerRepository;

    private User getAuthenticatedUser(String email) throws CoreThrowHandler {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0001));
    }

    private Seller getSellerStore(User user) throws CoreThrowHandler {
        if (!authRepository.findUserRole(user.getId()).equals("SELLER")) {
            throw new CoreThrowHandler(RestApiError.SLR_0001);
        }
        return sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0002));
    }

    @Transactional(readOnly = true)
    public List<SellerFlashSaleResponse.Available> getAvailableFlashSales(String userEmail) throws CoreThrowHandler {
        User user = getAuthenticatedUser(userEmail);
        Seller seller = getSellerStore(user);

        Instant now = Instant.now();
        List<FlashSale> availableSales = flashSaleRepository.findAvailableFlashSales(now);

        return availableSales.stream().map(fs -> {
            long productCount = flashSaleItemRepository.countByFlashSaleIdAndSellerId(fs.getId(), seller.getId());
            String status = now.isBefore(fs.getStartTime()) ? "upcoming" : "active";
            
            return SellerFlashSaleResponse.Available.builder()
                    .id(fs.getId())
                    .name(fs.getName())
                    .startTime(fs.getStartTime())
                    .endTime(fs.getEndTime())
                    .status(status)
                    .yourProductCount(productCount)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SellerFlashSaleResponse.Wrapper getFlashSaleItems(String userEmail, UUID flashSaleId, int page, int size) throws CoreThrowHandler {
        User user = getAuthenticatedUser(userEmail);
        Seller seller = getSellerStore(user);
        
        FlashSale fs = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0044));

        Pageable pageable = PageRequest.of(page, size);
        Page<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleIdAndSellerId(flashSaleId, seller.getId(), pageable);
        
        Page<SellerFlashSaleResponse.Item> mappedItems = items.map(this::mapToItemResponse);
        return SellerFlashSaleResponse.Wrapper.from(fs, mappedItems);
    }

    @Transactional
    public void addFlashSaleItem(String userEmail, UUID flashSaleId, FlashSaleItemRequest request) throws CoreThrowHandler {
        User user = getAuthenticatedUser(userEmail);
        Seller seller = getSellerStore(user);

        FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0044));

        if (Instant.now().isAfter(flashSale.getEndTime())) {
            throw new CoreThrowHandler(RestApiError.SLR_0045);
        }

        Product product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0016));

        if (!product.getStore().getSeller().getId().equals(seller.getId())) {
            throw new CoreThrowHandler(RestApiError.SLR_0017);
        }

        if (request.getFlashPrice().compareTo(product.getPrice()) >= 0) {
            throw new CoreThrowHandler(RestApiError.SLR_0030);
        }

        if (request.getRemainingQuota() <= 0) {
            throw new CoreThrowHandler(RestApiError.SLR_0034);
        }

        if (request.getRemainingQuota() > product.getStock()) {
            throw new CoreThrowHandler(RestApiError.SLR_0033);
        }

        if (flashSaleItemRepository.existsByFlashSaleIdAndProductId(flashSaleId, request.getProductId())) {
            throw new CoreThrowHandler(RestApiError.SLR_0046);
        }

        long conflicts = flashSaleItemRepository.countTimeConflicts(
                request.getProductId(), flashSaleId, flashSale.getStartTime(), flashSale.getEndTime());
        
        if (conflicts > 0) {
            throw new CoreThrowHandler(RestApiError.SLR_0035);
        }

        FlashSaleItem item = FlashSaleItem.builder()
                .flashSale(flashSale)
                .product(product)
                .flashPrice(request.getFlashPrice())
                .remainingQuota(request.getRemainingQuota())
                .build();

        flashSaleItemRepository.save(item);
    }

    @Transactional
    public void removeFlashSaleItem(String userEmail, UUID flashSaleId, UUID productId) throws CoreThrowHandler {
        User user = getAuthenticatedUser(userEmail);
        Seller seller = getSellerStore(user);

        FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0044));
        
        if (Instant.now().isAfter(flashSale.getEndTime())) {
            throw new CoreThrowHandler(RestApiError.SLR_0045);
        }

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0016));

        if (!product.getStore().getSeller().getId().equals(seller.getId())) {
            throw new CoreThrowHandler(RestApiError.SLR_0017);
        }

        FlashSaleItem item = flashSaleItemRepository.findByFlashSaleIdAndSellerId(flashSaleId, seller.getId(), PageRequest.of(0, 100))
                .stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0016));

        flashSaleItemRepository.delete(item);
    }

    private SellerFlashSaleResponse.Item mapToItemResponse(FlashSaleItem item) {
        BigDecimal originalPrice = item.getProduct().getPrice();
        BigDecimal flashPrice = item.getFlashPrice();
        
        Double discountPercentage = null;
        if (originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            discountPercentage = originalPrice.subtract(flashPrice)
                    .divide(originalPrice, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        return SellerFlashSaleResponse.Item.builder()
                .itemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(item.getProduct().getImage())
                .originalPrice(originalPrice)
                .flashPrice(flashPrice)
                .remainingQuota(item.getRemainingQuota())
                .discountPercentage(discountPercentage)
                .build();
    }
}
