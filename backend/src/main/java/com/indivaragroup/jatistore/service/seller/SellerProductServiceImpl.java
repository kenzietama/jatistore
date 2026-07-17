package com.indivaragroup.jatistore.service.seller;

import com.indivaragroup.jatistore.data.entity.Product;
import com.indivaragroup.jatistore.data.entity.ProductCategory;
import com.indivaragroup.jatistore.data.entity.Seller;
import com.indivaragroup.jatistore.data.entity.Store;
import com.indivaragroup.jatistore.dto.request.seller.ProductCreateRequest;
import com.indivaragroup.jatistore.dto.request.seller.ProductUpdateRequest;
import com.indivaragroup.jatistore.dto.response.module.seller.ProductResponse;
import com.indivaragroup.jatistore.repository.AuthRepository;
import com.indivaragroup.jatistore.repository.ProductCategoryRepository;
import com.indivaragroup.jatistore.repository.ProductRepository;
import com.indivaragroup.jatistore.repository.SellerRepository;
import com.indivaragroup.jatistore.repository.StoreRepository;
import com.indivaragroup.jatistore.service.utility.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.indivaragroup.jatistore.dto.utility.RestApiError;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SellerProductServiceImpl implements SellerProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final AuthRepository authRepository;
    private final SellerRepository sellerRepository;
    private final StoreRepository storeRepository;

    private Seller getSellerById(UUID sellerId) throws CoreThrowHandler {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0002));
    }

    private Store getStoreBySeller(Seller seller) throws CoreThrowHandler {
        return storeRepository.findBySellerId(seller.getId())
                .orElseThrow(() -> new CoreThrowHandler(RestApiError.SLR_0002));
    }

    @Override
    public Page<ProductResponse> getProducts(UUID sellerId, String search, String category, String status, String sortBy, String sortDir, int page, int limit) throws CoreThrowHandler {
        Seller seller = getSellerById(sellerId);

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = "createdAt";
        if (sortBy != null && !sortBy.isEmpty()) {
            sortProperty = switch (sortBy) {
                case "price" -> "price";
                case "stock" -> "stock";
                case "name" -> "name";
                default -> "createdAt";
            };
        }
        Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortProperty));

        Integer minStock = null;
        Integer maxStock = null;
        if (status != null && !status.isEmpty()) {
            if ("OUT_OF_STOCK".equalsIgnoreCase(status) || "OUT OF STOCK".equalsIgnoreCase(status)) {
                minStock = 0;
                maxStock = 0;
            } else if ("LOW_STOCK".equalsIgnoreCase(status) || "LOW STOCK".equalsIgnoreCase(status)) {
                minStock = 1;
                maxStock = 10;
            } else if ("ACTIVE".equalsIgnoreCase(status)) {
                minStock = 11;
            }
        }

        return productRepository.findProductsBySellerAndFilters(seller.getId(), search, category, minStock, maxStock, pageable)
                .map(ProductResponse::fromEntity);
    }

    @Override
    public ProductResponse getProduct(UUID sellerId, UUID id) throws CoreThrowHandler {
        Seller seller = getSellerById(sellerId);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CoreThrowHandler(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Product not found", null));

        if (!product.getStore().getSeller().getId().equals(seller.getId()) || product.getDeletedAt() != null) {
            throw new CoreThrowHandler(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Product not found", null);
        }
        return ProductResponse.fromEntity(product);
    }

    @Override
    public ProductResponse createProduct(UUID sellerId, ProductCreateRequest request) throws CoreThrowHandler {
        Seller seller = getSellerById(sellerId);
        Store store = getStoreBySeller(seller);

        ProductCategory category = productCategoryRepository.findById(request.getProductCategoryId())
                .orElseThrow(() -> new CoreThrowHandler(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Category not found", null));

        Product product = new Product();
        product.setStore(store);
        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImage(request.getImage());
        product.setCreatedAt(ZonedDateTime.now());
        product.setUpdatedAt(ZonedDateTime.now());

        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(UUID sellerId, UUID id, ProductUpdateRequest request) throws CoreThrowHandler {
        Seller seller = getSellerById(sellerId);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CoreThrowHandler(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Product not found", null));

        if (!product.getStore().getSeller().getId().equals(seller.getId()) || product.getDeletedAt() != null) {
            throw new CoreThrowHandler(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Product not found", null);
        }

        if (request.getProductCategoryId() != null) {
            ProductCategory category = productCategoryRepository.findById(request.getProductCategoryId())
                    .orElseThrow(() -> new CoreThrowHandler(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Category not found", null));
            product.setCategory(category);
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            product.setImage(request.getImage());
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        product.setUpdatedAt(ZonedDateTime.now());

        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    @Override
    public void deleteProduct(UUID sellerId, UUID id) throws CoreThrowHandler {
        Seller seller = getSellerById(sellerId);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CoreThrowHandler(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Product not found", null));

        if (!product.getStore().getSeller().getId().equals(seller.getId()) || product.getDeletedAt() != null) {
            throw new CoreThrowHandler(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Product not found", null);
        }

        product.setDeletedAt(ZonedDateTime.now());
        productRepository.save(product);
    }
}
