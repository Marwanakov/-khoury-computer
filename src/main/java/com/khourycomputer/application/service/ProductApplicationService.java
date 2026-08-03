package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.product.CreateProductRequest;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.dto.product.UpdateProductRequest;
import com.khourycomputer.application.repository.CategoryRepository;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import com.khourycomputer.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductApplicationService {

    private static final int LOW_STOCK_LIMIT = 5;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductApplicationService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        validateCategoryExists(request.categoryId());

        Product product = new Product(
                null,
                request.name(),
                request.description(),
                request.price(),
                request.brand(),
                request.stockQuantity(),
                calculateAvailabilityStatus(request.stockQuantity()),
                request.imageUrl(),
                request.categoryId(),
                cleanTags(request.tags()));

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        validateCategoryExists(request.categoryId());

        Product updatedProduct = new Product(
                existingProduct.getId(),
                request.name(),
                request.description(),
                request.price(),
                request.brand(),
                request.stockQuantity(),
                calculateAvailabilityStatus(request.stockQuantity()),
                request.imageUrl(),
                request.categoryId(),
                cleanTags(request.tags()));

        Product savedProduct = productRepository.save(updatedProduct);

        return toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

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
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found.");
        }

        productRepository.deleteById(productId);
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
                product.getPrice(),
                product.getBrand(),
                product.getStockQuantity(),
                product.getAvailabilityStatus(),
                product.getImageUrl(),
                product.getCategoryId(),
                product.getTags());
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
}