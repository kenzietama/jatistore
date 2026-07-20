package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.FlashSale;
import com.indivaragroup.jatistore.data.entity.FlashSaleItem;
import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.request.module.seller.FlashSaleItemRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.SellerFlashSaleResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.FlashSaleItemRepository;
import com.indivaragroup.jatistore.repository.FlashSaleRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerFlashSaleServiceTest {

    @Mock
    private FlashSaleRepository flashSaleRepository;

    @Mock
    private FlashSaleItemRepository flashSaleItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerFlashSaleService sellerFlashSaleService;

    private User createMockUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    private Seller createMockSeller() {
        Seller seller = new Seller();
        seller.setId(UUID.randomUUID());
        return seller;
    }

    private void setupMockAuth(User user, Seller seller) {
        when(authRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authRepository.findUserRole(user.getId())).thenReturn("SELLER");
        when(sellerRepository.findByUserId(user.getId())).thenReturn(Optional.of(seller));
    }

    @Test
    void getAvailableFlashSales_shouldReturnSales() throws CoreThrowHandler {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        FlashSale fsUpcoming = new FlashSale();
        fsUpcoming.setId(UUID.randomUUID());
        fsUpcoming.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        
        FlashSale fsActive = new FlashSale();
        fsActive.setId(UUID.randomUUID());
        fsActive.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS));

        when(flashSaleRepository.findAvailableFlashSales(any(Instant.class))).thenReturn(List.of(fsUpcoming, fsActive));
        when(flashSaleItemRepository.countByFlashSaleIdAndSellerId(any(), eq(seller.getId()))).thenReturn(5L);

        List<SellerFlashSaleResponse.Available> result = sellerFlashSaleService.getAvailableFlashSales("test@example.com");
        assertEquals(2, result.size());
        assertEquals("upcoming", result.get(0).getStatus());
        assertEquals("active", result.get(1).getStatus());
    }

    @Test
    void getAvailableFlashSales_authFailed_shouldThrow() {
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.getAvailableFlashSales("test@example.com"));
    }

    @Test
    void getAvailableFlashSales_notSellerRole_shouldThrow() {
        User user = createMockUser();
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(authRepository.findUserRole(user.getId())).thenReturn("CUSTOMER");
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.getAvailableFlashSales("test@example.com"));
    }

    @Test
    void getAvailableFlashSales_storeNotFound_shouldThrow() {
        User user = createMockUser();
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(authRepository.findUserRole(user.getId())).thenReturn("SELLER");
        when(sellerRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.getAvailableFlashSales("test@example.com"));
    }

    @Test
    void getFlashSaleItems_shouldReturnWrapper() throws CoreThrowHandler {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        FlashSale fs = new FlashSale();
        fs.setId(UUID.randomUUID());
        when(flashSaleRepository.findById(fs.getId())).thenReturn(Optional.of(fs));

        FlashSaleItem item = new FlashSaleItem();
        item.setId(UUID.randomUUID());
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("200"));
        item.setProduct(product);
        item.setFlashPrice(new BigDecimal("100"));

        Page<FlashSaleItem> page = new PageImpl<>(List.of(item));
        when(flashSaleItemRepository.findByFlashSaleIdAndSellerId(eq(fs.getId()), eq(seller.getId()), any(Pageable.class)))
                .thenReturn(page);

        SellerFlashSaleResponse.Wrapper wrapper = sellerFlashSaleService.getFlashSaleItems("test@example.com", fs.getId(), 0, 10);
        assertNotNull(wrapper);
        assertEquals(1, wrapper.getItems().size());
    }
    
    @Test
    void getFlashSaleItems_zeroPrice_shouldReturnWrapper() throws CoreThrowHandler {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        FlashSale fs = new FlashSale();
        fs.setId(UUID.randomUUID());
        when(flashSaleRepository.findById(fs.getId())).thenReturn(Optional.of(fs));

        FlashSaleItem item = new FlashSaleItem();
        item.setId(UUID.randomUUID());
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(BigDecimal.ZERO); // ZERO PRICE
        item.setProduct(product);
        item.setFlashPrice(new BigDecimal("100"));

        Page<FlashSaleItem> page = new PageImpl<>(List.of(item));
        when(flashSaleItemRepository.findByFlashSaleIdAndSellerId(eq(fs.getId()), eq(seller.getId()), any(Pageable.class)))
                .thenReturn(page);

        SellerFlashSaleResponse.Wrapper wrapper = sellerFlashSaleService.getFlashSaleItems("test@example.com", fs.getId(), 0, 10);
        assertNotNull(wrapper);
        assertEquals(1, wrapper.getItems().size());
    }

    @Test
    void getFlashSaleItems_notFound_shouldThrow() {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        when(flashSaleRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.getFlashSaleItems("test@example.com", UUID.randomUUID(), 0, 10));
    }

    @Test
    void addFlashSaleItem_shouldSave() throws CoreThrowHandler {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        FlashSale fs = new FlashSale();
        fs.setId(UUID.randomUUID());
        fs.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        fs.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));
        when(flashSaleRepository.findById(fs.getId())).thenReturn(Optional.of(fs));

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("200"));
        product.setStock(50);
        Store store = new Store();
        store.setSeller(seller);
        product.setStore(store);
        
        when(productRepository.findByIdAndDeletedAtIsNull(any())).thenReturn(Optional.of(product));
        when(flashSaleItemRepository.existsByFlashSaleIdAndProductId(fs.getId(), product.getId())).thenReturn(false);
        when(flashSaleItemRepository.countTimeConflicts(any(), any(), any(), any())).thenReturn(0L);

        FlashSaleItemRequest request = new FlashSaleItemRequest();
        request.setProductId(product.getId());
        request.setFlashPrice(new BigDecimal("100"));
        request.setRemainingQuota(10);

        sellerFlashSaleService.addFlashSaleItem("test@example.com", fs.getId(), request);
        verify(flashSaleItemRepository).save(any(FlashSaleItem.class));
    }

    @Test
    void addFlashSaleItem_variousExceptions() {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        FlashSale fsStarted = new FlashSale();
        fsStarted.setId(UUID.randomUUID());
        fsStarted.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS));
        lenient().when(flashSaleRepository.findById(fsStarted.getId())).thenReturn(Optional.of(fsStarted));
        
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.addFlashSaleItem("test@example.com", UUID.randomUUID(), new FlashSaleItemRequest())); // fs not found
        
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.addFlashSaleItem("test@example.com", fsStarted.getId(), new FlashSaleItemRequest())); // past start time

        FlashSale fsUpcoming = new FlashSale();
        fsUpcoming.setId(UUID.randomUUID());
        fsUpcoming.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        fsUpcoming.setEndTime(Instant.now().plus(2, ChronoUnit.DAYS));
        when(flashSaleRepository.findById(fsUpcoming.getId())).thenReturn(Optional.of(fsUpcoming));

        lenient().when(productRepository.findByIdAndDeletedAtIsNull(any())).thenReturn(Optional.empty());
        FlashSaleItemRequest reqNotFound = new FlashSaleItemRequest();
        reqNotFound.setProductId(UUID.randomUUID());
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.addFlashSaleItem("test@example.com", fsUpcoming.getId(), reqNotFound));

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("200"));
        product.setStock(50);
        Store store = new Store();
        Seller otherSeller = new Seller();
        otherSeller.setId(UUID.randomUUID());
        store.setSeller(otherSeller);
        product.setStore(store);

        when(productRepository.findByIdAndDeletedAtIsNull(product.getId())).thenReturn(Optional.of(product));
        
        FlashSaleItemRequest req = new FlashSaleItemRequest();
        req.setProductId(product.getId());
        req.setFlashPrice(new BigDecimal("100"));
        req.setRemainingQuota(10);
        
        // store mismatch
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.addFlashSaleItem("test@example.com", fsUpcoming.getId(), req));
        
        store.setSeller(seller);
        
        // price >= product price
        req.setFlashPrice(new BigDecimal("300"));
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.addFlashSaleItem("test@example.com", fsUpcoming.getId(), req));

        // quota <= 0
        req.setFlashPrice(new BigDecimal("100"));
        req.setRemainingQuota(0);
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.addFlashSaleItem("test@example.com", fsUpcoming.getId(), req));
        
        // quota > stock
        req.setRemainingQuota(100);
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.addFlashSaleItem("test@example.com", fsUpcoming.getId(), req));
        
        req.setRemainingQuota(10);
        when(flashSaleItemRepository.existsByFlashSaleIdAndProductId(fsUpcoming.getId(), product.getId())).thenReturn(true);
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.addFlashSaleItem("test@example.com", fsUpcoming.getId(), req));

        when(flashSaleItemRepository.existsByFlashSaleIdAndProductId(fsUpcoming.getId(), product.getId())).thenReturn(false);
        when(flashSaleItemRepository.countTimeConflicts(any(), any(), any(), any())).thenReturn(1L);
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.addFlashSaleItem("test@example.com", fsUpcoming.getId(), req));
    }

    @Test
    void removeFlashSaleItem_shouldDelete() throws CoreThrowHandler {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        FlashSale fs = new FlashSale();
        fs.setId(UUID.randomUUID());
        fs.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        when(flashSaleRepository.findById(fs.getId())).thenReturn(Optional.of(fs));

        Product product = new Product();
        product.setId(UUID.randomUUID());
        Store store = new Store();
        store.setSeller(seller);
        product.setStore(store);
        when(productRepository.findByIdAndDeletedAtIsNull(product.getId())).thenReturn(Optional.of(product));

        FlashSaleItem item = new FlashSaleItem();
        item.setProduct(product);
        when(flashSaleItemRepository.findByFlashSaleIdAndSellerId(eq(fs.getId()), eq(seller.getId()), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item)));

        sellerFlashSaleService.removeFlashSaleItem("test@example.com", fs.getId(), product.getId());
        verify(flashSaleItemRepository).delete(item);
    }
    
    @Test
    void removeFlashSaleItem_variousExceptions() {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        FlashSale fs = new FlashSale();
        fs.setId(UUID.randomUUID());
        fs.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        
        FlashSale fsStarted = new FlashSale();
        fsStarted.setId(UUID.randomUUID());
        fsStarted.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS));
        
        lenient().when(flashSaleRepository.findById(fs.getId())).thenReturn(Optional.of(fs));
        lenient().when(flashSaleRepository.findById(fsStarted.getId())).thenReturn(Optional.of(fsStarted));
        
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.removeFlashSaleItem("test@example.com", UUID.randomUUID(), UUID.randomUUID())); // fs not found

        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.removeFlashSaleItem("test@example.com", fsStarted.getId(), UUID.randomUUID())); // past start time
        
        lenient().when(productRepository.findByIdAndDeletedAtIsNull(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.removeFlashSaleItem("test@example.com", fs.getId(), UUID.randomUUID())); // product not found

        Product product = new Product();
        product.setId(UUID.randomUUID());
        Store store = new Store();
        Seller otherSeller = new Seller();
        otherSeller.setId(UUID.randomUUID());
        store.setSeller(otherSeller);
        product.setStore(store);
        when(productRepository.findByIdAndDeletedAtIsNull(product.getId())).thenReturn(Optional.of(product));

        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.removeFlashSaleItem("test@example.com", fs.getId(), product.getId())); // store mismatch
        
        store.setSeller(seller);
        when(flashSaleItemRepository.findByFlashSaleIdAndSellerId(eq(fs.getId()), eq(seller.getId()), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.removeFlashSaleItem("test@example.com", fs.getId(), product.getId())); // item not found
    }

    @Test
    void updateFlashSaleItem_shouldUpdate() throws CoreThrowHandler {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        FlashSale fs = new FlashSale();
        fs.setId(UUID.randomUUID());
        fs.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        when(flashSaleRepository.findById(fs.getId())).thenReturn(Optional.of(fs));

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("200"));
        product.setStock(50);
        Store store = new Store();
        store.setSeller(seller);
        product.setStore(store);
        
        when(productRepository.findByIdAndDeletedAtIsNull(product.getId())).thenReturn(Optional.of(product));

        FlashSaleItem item = new FlashSaleItem();
        item.setProduct(product);
        when(flashSaleItemRepository.findByFlashSaleIdAndSellerId(eq(fs.getId()), eq(seller.getId()), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item)));

        FlashSaleItemRequest request = new FlashSaleItemRequest();
        request.setProductId(product.getId());
        request.setFlashPrice(new BigDecimal("100"));
        request.setRemainingQuota(10);

        sellerFlashSaleService.updateFlashSaleItem("test@example.com", fs.getId(), product.getId(), request);
        assertEquals(new BigDecimal("100"), item.getFlashPrice());
        assertEquals(10, item.getRemainingQuota());
        verify(flashSaleItemRepository).save(item);
    }
    
    @Test
    void updateFlashSaleItem_variousExceptions() {
        User user = createMockUser();
        Seller seller = createMockSeller();
        setupMockAuth(user, seller);

        FlashSale fs = new FlashSale();
        fs.setId(UUID.randomUUID());
        fs.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        
        FlashSale fsStarted = new FlashSale();
        fsStarted.setId(UUID.randomUUID());
        fsStarted.setStartTime(Instant.now().minus(1, ChronoUnit.DAYS));
        
        lenient().when(flashSaleRepository.findById(fs.getId())).thenReturn(Optional.of(fs));
        lenient().when(flashSaleRepository.findById(fsStarted.getId())).thenReturn(Optional.of(fsStarted));

        FlashSaleItemRequest request = new FlashSaleItemRequest();
        request.setProductId(UUID.randomUUID());
        request.setFlashPrice(new BigDecimal("300"));
        request.setRemainingQuota(10);
        
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.updateFlashSaleItem("test@example.com", UUID.randomUUID(), UUID.randomUUID(), request)); // fs not found

        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.updateFlashSaleItem("test@example.com", fsStarted.getId(), UUID.randomUUID(), request)); // past start time

        lenient().when(productRepository.findByIdAndDeletedAtIsNull(any())).thenReturn(Optional.empty());
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.updateFlashSaleItem("test@example.com", fs.getId(), UUID.randomUUID(), request)); // product not found

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(new BigDecimal("200"));
        product.setStock(50);
        Store store = new Store();
        Seller otherSeller = new Seller();
        otherSeller.setId(UUID.randomUUID());
        store.setSeller(otherSeller);
        product.setStore(store);
        
        when(productRepository.findByIdAndDeletedAtIsNull(product.getId())).thenReturn(Optional.of(product));
        
        request.setProductId(product.getId());
        
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.updateFlashSaleItem("test@example.com", fs.getId(), product.getId(), request)); // store mismatch
        
        store.setSeller(seller);

        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.updateFlashSaleItem("test@example.com", fs.getId(), product.getId(), request)); // price >= product price

        request.setFlashPrice(new BigDecimal("100"));
        request.setRemainingQuota(0); // <= 0
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.updateFlashSaleItem("test@example.com", fs.getId(), product.getId(), request));

        request.setRemainingQuota(100); // > stock
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.updateFlashSaleItem("test@example.com", fs.getId(), product.getId(), request));

        request.setRemainingQuota(10); // reset to valid quota
        when(flashSaleItemRepository.findByFlashSaleIdAndSellerId(eq(fs.getId()), eq(seller.getId()), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        assertThrows(CoreThrowHandler.class, () -> sellerFlashSaleService.updateFlashSaleItem("test@example.com", fs.getId(), product.getId(), request)); // item not found
    }
}
