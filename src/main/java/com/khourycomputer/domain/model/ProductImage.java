package com.khourycomputer.domain.model;

import java.util.Objects;

public class ProductImage {

    private Long id;
    private Long productId;
    private String imageUrl;
    private int displayOrder;
    private boolean primary;

    public ProductImage(
            Long id,
            Long productId,
            String imageUrl,
            int displayOrder,
            boolean primary) {
        setId(id);
        setProductId(productId);
        setImageUrl(imageUrl);
        setDisplayOrder(displayOrder);
        this.primary = primary;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void changeImageUrl(String imageUrl) {
        setImageUrl(imageUrl);
    }

    public void changeDisplayOrder(int displayOrder) {
        setDisplayOrder(displayOrder);
    }

    public void markAsPrimary() {
        this.primary = true;
    }

    public void removePrimaryStatus() {
        this.primary = false;
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product image must belong to a product.");
        }

        this.productId = productId;
    }

    private void setImageUrl(String imageUrl) {
        String normalizedImageUrl = Objects
                .requireNonNullElse(imageUrl, "")
                .trim();

        if (normalizedImageUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Product image URL cannot be empty.");
        }

        this.imageUrl = normalizedImageUrl;
    }

    private void setDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException(
                    "Product image order cannot be negative.");
        }

        this.displayOrder = displayOrder;
    }
}