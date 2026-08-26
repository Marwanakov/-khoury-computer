package com.khourycomputer.application.dto.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        List<CartItemResponse> items,
        BigDecimal totalPrice
) {

    public boolean hasStockIssues() {
        return items.stream()
                .anyMatch(item ->
                        item.quantity() > item.stockQuantity()
                                || item.stockQuantity() <= 0);
    }
}