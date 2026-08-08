package com.khourycomputer.application.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String imageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
) {
}