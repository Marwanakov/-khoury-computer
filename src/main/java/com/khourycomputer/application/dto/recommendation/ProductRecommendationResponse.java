package com.khourycomputer.application.dto.recommendation;

import com.khourycomputer.domain.enums.ProductAvailabilityStatus;

import java.math.BigDecimal;

public record ProductRecommendationResponse(
        Long productId,
        String name,
        String brand,
        String imageUrl,
        ProductAvailabilityStatus availabilityStatus,
        BigDecimal regularPrice,
        BigDecimal effectivePrice,
        BigDecimal priceDifference,
        ProductRecommendationGroup group,
        String similarityReason
) {

    public boolean hasActiveDeal() {
        return effectivePrice.compareTo(regularPrice) < 0;
    }
}