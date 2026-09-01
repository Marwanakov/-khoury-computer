package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.product.CreateProductRequest;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.dto.product.UpdateProductRequest;
import com.khourycomputer.application.repository.CategoryRepository;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import com.khourycomputer.domain.exception.ProductNotFoundException;
import com.khourycomputer.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khourycomputer.application.port.storage.ImageStorage;
import com.khourycomputer.application.port.storage.ImageStorageFolder;
import com.khourycomputer.application.port.storage.ImageUpload;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;

import java.util.Objects;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductApplicationService {

    private static final int LOW_STOCK_LIMIT = 5;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductPricingService productPricingService;
    private final ImageStorage imageStorage;

    public ProductApplicationService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ImageStorage imageStorage,
            ProductPricingService productPricingService) {
        this.productRepository = productRepository;

        this.categoryRepository = categoryRepository;

        this.imageStorage = imageStorage;

        this.productPricingService = productPricingService;
    }

    @Transactional
    public ProductResponse createProduct(
            CreateProductRequest request,
            ImageUpload image) {
        validateCategoryExists(request.categoryId());

        String imageUrl = storeImageIfPresent(image);

        registerImageDeletionOnRollback(imageUrl);

        LocalDateTime currentTime = LocalDateTime.now();

        Product product = new Product(
                null,
                request.name(),
                request.description(),
                cleanSpecifications(request.specifications()),
                request.price(),
                request.brand(),
                request.stockQuantity(),
                calculateAvailabilityStatus(request.stockQuantity()),
                imageUrl,
                request.categoryId(),
                cleanTags(request.tags()),
                request.newArrival(),
                request.newArrival()
                        ? currentTime
                        : null,
                request.bestSeller(),
                request.bestSeller()
                        ? currentTime
                        : null);

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse createProduct(
            CreateProductRequest request) {
        return createProduct(
                request,
                ImageUpload.empty());
    }

    @Transactional
    public ProductResponse updateProduct(
            Long productId,
            UpdateProductRequest request,
            ImageUpload newImage,
            boolean removeImage) {

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        validateCategoryExists(
                request.categoryId());

        productPricingService
                .validateRegularPriceForExistingDeals(
                        productId,
                        request.price());

        String oldImageUrl = existingProduct.getImageUrl();

        String resultingImageUrl = determineResultingImageUrl(
                oldImageUrl,
                newImage,
                removeImage);
        LocalDateTime newArrivalMarkedAt = determineNewArrivalMarkedAt(
                existingProduct,
                request.newArrival());

        LocalDateTime bestSellerMarkedAt = determineBestSellerMarkedAt(
                existingProduct,
                request.bestSeller());

        Product updatedProduct = new Product(
                existingProduct.getId(),
                request.name(),
                request.description(),
                cleanSpecifications(request.specifications()),
                request.price(),
                request.brand(),
                request.stockQuantity(),
                calculateAvailabilityStatus(request.stockQuantity()),
                resultingImageUrl,
                request.categoryId(),
                cleanTags(request.tags()),
                request.newArrival(),
                newArrivalMarkedAt,
                request.bestSeller(),
                bestSellerMarkedAt);

        Product savedProduct = productRepository.save(updatedProduct);

        if (!Objects.equals(oldImageUrl, resultingImageUrl)) {
            registerImageDeletionAfterCommit(oldImageUrl);
        }

        return toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(
            Long productId,
            UpdateProductRequest request) {
        return updateProduct(
                productId,
                request,
                ImageUpload.empty(),
                false);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> filterProducts(
            String keyword,
            Long categoryId,
            String brand,
            ProductAvailabilityStatus availabilityStatus,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        validatePriceFilter(minPrice, maxPrice);

        String normalizedKeyword = normalizeFilterText(keyword);
        String normalizedBrand = normalizeFilterText(brand);

        return productRepository.findAll()
                .stream()
                .filter(product -> matchesKeyword(product, normalizedKeyword))
                .filter(product -> categoryId == null || product.getCategoryId().equals(categoryId))
                .filter(product -> matchesBrand(product, normalizedBrand))
                .filter(product -> availabilityStatus == null || product.getAvailabilityStatus() == availabilityStatus)
                .filter(product -> minPrice == null || product.getPrice().compareTo(minPrice) >= 0)
                .filter(product -> maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProductsByCategory(Long categoryId) {
        validateCategoryExists(categoryId);

        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProductsByName(String keyword) {
        String cleanedKeyword = keyword == null ? "" : keyword.trim();

        return productRepository.findByNameContaining(cleanedKeyword)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProductsByTag(String tag) {
        String cleanedTag = tag == null ? "" : tag.trim().toLowerCase();

        return productRepository.findByTag(cleanedTag)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProductsByBrand(String brand) {
        String cleanedBrand = brand == null ? "" : brand.trim();

        return productRepository.findByBrand(cleanedBrand)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProductsByAvailability(ProductAvailabilityStatus availabilityStatus) {
        if (availabilityStatus == null) {
            throw new IllegalArgumentException("Availability status cannot be null.");
        }

        return productRepository.findByAvailabilityStatus(availabilityStatus)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        validatePriceRange(minPrice, maxPrice);

        return productRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        productRepository.deleteById(productId);

        registerImageDeletionAfterCommit(product.getImageUrl());
    }

    private void validateCategoryExists(Long categoryId) {
        if (categoryId == null || !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found.");
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException("Price range cannot be empty.");
        }

        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price range cannot be negative.");
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price.");
        }
    }

    private ProductAvailabilityStatus calculateAvailabilityStatus(int stockQuantity) {
        if (stockQuantity == 0) {
            return ProductAvailabilityStatus.SOLD_OUT;
        }

        if (stockQuantity <= LOW_STOCK_LIMIT) {
            return ProductAvailabilityStatus.LOW_STOCK;
        }

        return ProductAvailabilityStatus.AVAILABLE;
    }

    private Set<String> cleanTags(Set<String> tags) {
        if (tags == null) {
            return Set.of();
        }

        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase())
                .collect(Collectors.toSet());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSpecifications(),
                product.getPrice(),
                product.getBrand(),
                product.getStockQuantity(),
                product.getAvailabilityStatus(),
                product.getImageUrl(),
                product.getCategoryId(),
                product.getTags(),
                product.isNewArrival(),
                product.getNewArrivalMarkedAt(),
                product.isBestSeller(),
                product.getBestSellerMarkedAt());
    }

    private boolean matchesKeyword(Product product, String keyword) {
        if (keyword == null) {
            return true;
        }

        return containsIgnoreCase(product.getName(), keyword)
                || containsIgnoreCase(product.getBrand(), keyword)
                || product.getTags()
                        .stream()
                        .anyMatch(tag -> containsIgnoreCase(tag, keyword));
    }

    private boolean matchesBrand(Product product, String brand) {
        if (brand == null) {
            return true;
        }

        return containsIgnoreCase(product.getBrand(), brand);
    }

    private boolean containsIgnoreCase(String value, String searchText) {
        return value != null && value.toLowerCase().contains(searchText);
    }

    private String normalizeFilterText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    private void validatePriceFilter(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative.");
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Maximum price cannot be negative.");
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price.");
        }
    }

    private String storeImageIfPresent(ImageUpload image) {
        if (image == null || !image.isPresent()) {
            return "";
        }

        return imageStorage.store(
                image,
                ImageStorageFolder.PRODUCTS);
    }

    private String determineResultingImageUrl(
            String oldImageUrl,
            ImageUpload newImage,
            boolean removeImage) {
        if (newImage != null && newImage.isPresent()) {
            String newImageUrl = imageStorage.store(
                    newImage,
                    ImageStorageFolder.PRODUCTS);

            registerImageDeletionOnRollback(newImageUrl);

            return newImageUrl;
        }

        if (removeImage) {
            return "";
        }

        return oldImageUrl;
    }

    private void registerImageDeletionOnRollback(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            imageStorage.delete(imageUrl);
                        }
                    }
                });
    }

    private void registerImageDeletionAfterCommit(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            imageStorage.delete(imageUrl);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        imageStorage.delete(imageUrl);
                    }
                });
    }

    private String cleanSpecifications(String specifications) {
        if (specifications == null || specifications.isBlank()) {
            throw new IllegalArgumentException(
                    "Technical specifications are required.");
        }

        List<String> cleanedLines = specifications
                .lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        for (String line : cleanedLines) {
            int separatorIndex = line.indexOf(':');

            if (separatorIndex <= 0
                    || separatorIndex >= line.length() - 1) {
                throw new IllegalArgumentException(
                        "Each specification must use the format "
                                + "\"Label: Value\". Invalid line: "
                                + line);
            }

            String label = line
                    .substring(0, separatorIndex)
                    .trim();

            String value = line
                    .substring(separatorIndex + 1)
                    .trim();

            if (label.isBlank() || value.isBlank()) {
                throw new IllegalArgumentException(
                        "Each specification must include both "
                                + "a label and a value.");
            }
        }

        return String.join(System.lineSeparator(), cleanedLines);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> filterStorefrontProducts(
            String keyword,
            Long categoryId,
            String brand,
            ProductAvailabilityStatus availabilityStatus,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            boolean newArrivalsOnly,
            boolean bestSellersOnly) {
        validatePriceFilter(minPrice, maxPrice);

        String normalizedKeyword = normalizeFilterText(keyword);

        String normalizedBrand = normalizeFilterText(brand);

        return productRepository.findAll()
                .stream()
                .filter(product -> matchesKeyword(
                        product,
                        normalizedKeyword))
                .filter(product -> categoryId == null
                        || product.getCategoryId()
                                .equals(categoryId))
                .filter(product -> matchesBrand(
                        product,
                        normalizedBrand))
                .filter(product -> availabilityStatus == null
                        || product.getAvailabilityStatus() == availabilityStatus)
                .filter(product -> !newArrivalsOnly
                        || product.isNewArrival())
                .filter(product -> !bestSellersOnly
                        || product.isBestSeller())
                .filter(product -> matchesEffectivePrice(
                        product,
                        minPrice,
                        maxPrice))
                .sorted(
                        newArrivalsOnly
                                ? Comparator.comparing(
                                        Product::getNewArrivalMarkedAt).reversed()
                                : bestSellersOnly
                                        ? Comparator.comparing(
                                                Product::getBestSellerMarkedAt).reversed()
                                        : (first, second) -> 0)
                .map(this::toResponse)
                .toList();
    }

    private boolean matchesEffectivePrice(
            Product product,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        BigDecimal effectivePrice = productPricingService
                .getEffectiveUnitPrice(product);

        if (minPrice != null
                && effectivePrice.compareTo(minPrice) < 0) {
            return false;
        }

        return maxPrice == null
                || effectivePrice.compareTo(maxPrice) <= 0;
    }

    private LocalDateTime determineNewArrivalMarkedAt(
            Product existingProduct,
            boolean newArrival) {
        if (!newArrival) {
            return null;
        }

        if (existingProduct.isNewArrival()) {
            return existingProduct.getNewArrivalMarkedAt();
        }

        return LocalDateTime.now();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listNewArrivals() {
        return productRepository.findAll()
                .stream()
                .filter(Product::isNewArrival)
                .sorted(
                        Comparator.comparing(
                                Product::getNewArrivalMarkedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listBestSellers() {
        return productRepository.findAll()
                .stream()
                .filter(Product::isBestSeller)
                .sorted(
                        Comparator.comparing(
                                Product::getBestSellerMarkedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private LocalDateTime determineBestSellerMarkedAt(
            Product existingProduct,
            boolean bestSeller) {
        if (!bestSeller) {
            return null;
        }

        if (existingProduct.isBestSeller()) {
            return existingProduct.getBestSellerMarkedAt();
        }

        return LocalDateTime.now();
    }
}