package com.khourycomputer.application.dto.product;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateProductRequest(
        String name,
        String description,
        String specifications,
        BigDecimal price,
        String brand,
        int stockQuantity,
        Long categoryId,
        Set<String> tags,
        boolean newArrival,
        boolean bestSeller
) {
}