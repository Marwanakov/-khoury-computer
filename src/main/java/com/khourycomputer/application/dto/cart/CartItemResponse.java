package com.khourycomputer.application.dto.cart;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String imageUrl,
        BigDecimal unitPrice,
        BigDecimal regularUnitPrice,
        int quantity,
        BigDecimal subtotal,
        int stockQuantity
) {

    public boolean hasActiveDeal() {
        return unitPrice.compareTo(
                regularUnitPrice
        ) < 0;
    }

    public BigDecimal savingsPerUnit() {
        if (!hasActiveDeal()) {
            return BigDecimal.ZERO;
        }

        return regularUnitPrice.subtract(
                unitPrice
        );
    }

    public BigDecimal totalSavings() {
        return savingsPerUnit().multiply(
                BigDecimal.valueOf(quantity)
        );
    }
}