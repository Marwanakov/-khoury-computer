package com.khourycomputer.application.dto.product;

import com.khourycomputer.domain.enums.ProductAvailabilityStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String specifications,
        BigDecimal price,
        String brand,
        int stockQuantity,
        ProductAvailabilityStatus availabilityStatus,
        String imageUrl,
        Long categoryId,
        Set<String> tags,
        boolean newArrival,
        LocalDateTime newArrivalMarkedAt,
        boolean bestSeller,
        LocalDateTime bestSellerMarkedAt) {

    public List<ProductSpecificationResponse> specificationItems() {
        if (specifications == null || specifications.isBlank()) {
            return List.of();
        }

        return Arrays.stream(specifications.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(ProductResponse::parseSpecification)
                .filter(item -> item != null)
                .toList();
    }

    private static ProductSpecificationResponse parseSpecification(
            String line) {
        int separatorIndex = line.indexOf(':');

        if (separatorIndex <= 0
                || separatorIndex >= line.length() - 1) {
            return null;
        }

        String label = line
                .substring(0, separatorIndex)
                .trim();

        String value = line
                .substring(separatorIndex + 1)
                .trim();

        if (label.isBlank() || value.isBlank()) {
            return null;
        }

        return new ProductSpecificationResponse(label, value);
    }
}