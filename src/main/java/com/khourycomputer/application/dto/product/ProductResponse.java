package com.khourycomputer.application.dto.product;

import com.khourycomputer.domain.enums.ProductAvailabilityStatus;

import java.math.BigDecimal;
import java.util.Set;

public record ProductResponse(
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
}