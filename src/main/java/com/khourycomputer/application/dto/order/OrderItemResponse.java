package com.khourycomputer.application.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
) {
}