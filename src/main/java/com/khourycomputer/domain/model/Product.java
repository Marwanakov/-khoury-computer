package com.khourycomputer.domain.model;

import com.khourycomputer.domain.enums.ProductAvailabilityStatus;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Product {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String brand;
    private int stockQuantity;
    private ProductAvailabilityStatus availabilityStatus;
    private String imageUrl;
    private Long categoryId;
    private Set<String> tags;

    public Product(
            Long id,
            String name,
            String description,
            BigDecimal price,
            String brand,
            int stockQuantity,
            ProductAvailabilityStatus availabilityStatus,
            String imageUrl,
            Long categoryId,
            Set<String> tags
    ) {
        setId(id);
        setName(name);
        setDescription(description);
        setPrice(price);
        setBrand(brand);
        setStockQuantity(stockQuantity);
        setAvailabilityStatus(availabilityStatus);
        setImageUrl(imageUrl);
        setCategoryId(categoryId);
        setTags(tags);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getBrand() {
        return brand;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public ProductAvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Set<String> getTags() {
        return Set.copyOf(tags);
    }

    public void changePrice(BigDecimal price) {
        setPrice(price);
    }

    public void changeStockQuantity(int stockQuantity) {
        setStockQuantity(stockQuantity);
    }

    public void markAsSoldOut() {
        this.availabilityStatus = ProductAvailabilityStatus.SOLD_OUT;
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }

        this.name = name.trim();
    }

    private void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "").trim();
    }

    private void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price cannot be negative.");
        }

        this.price = price;
    }

    private void setBrand(String brand) {
        this.brand = Objects.requireNonNullElse(brand, "").trim();
    }

    private void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }

        this.stockQuantity = stockQuantity;
    }

    private void setAvailabilityStatus(ProductAvailabilityStatus availabilityStatus) {
        this.availabilityStatus = Objects.requireNonNullElse(
                availabilityStatus,
                ProductAvailabilityStatus.AVAILABLE
        );
    }

    private void setImageUrl(String imageUrl) {
        this.imageUrl = Objects.requireNonNullElse(imageUrl, "").trim();
    }

    private void setCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Product must belong to a category.");
        }

        this.categoryId = categoryId;
    }

    private void setTags(Set<String> tags) {
        this.tags = tags == null ? new HashSet<>() : new HashSet<>(tags);
    }
}