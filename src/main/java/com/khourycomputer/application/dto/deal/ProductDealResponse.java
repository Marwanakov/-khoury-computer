package com.khourycomputer.application.dto.deal;

import com.khourycomputer.domain.enums.DealStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDealResponse(
        Long id,
        Long productId,
        String productName,
        String imageUrl,
        BigDecimal regularPrice,
        BigDecimal dealPrice,
        BigDecimal savingsAmount,
        int discountPercentage,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        boolean featured,
        DealStatus status,
        LocalDateTime createdAt
) {
}