package com.khourycomputer.domain.model;

import java.math.BigDecimal;

public class OrderItem {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;

    public OrderItem(Long id, Long productId, String productName, BigDecimal unitPrice, int quantity) {
        setId(id);
        setProductId(productId);
        setProductName(productName);
        setUnitPrice(unitPrice);
        setQuantity(quantity);
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product id cannot be null.");
        }

        this.productId = productId;
    }

    private void setProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }

        this.productName = productName.trim();
    }

    private void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative.");
        }

        this.unitPrice = unitPrice;
    }

    private void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        this.quantity = quantity;
    }
}