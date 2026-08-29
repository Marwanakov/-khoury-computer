package com.khourycomputer.application.dto.deal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateProductDealRequest(
        BigDecimal dealPrice,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        boolean featured
) {
}