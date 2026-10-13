package com.khourycomputer.application.dto.hero;

import com.khourycomputer.domain.enums.HeroLinkType;

public record HeroSlideResponse(
        Long id,
        String desktopImageUrl,
        String mobileImageUrl,
        String altText,
        HeroLinkType linkType,
        Long linkedProductId,
        String linkedProductName,
        Long linkedCategoryId,
        String linkedCategoryName,
        String targetUrl,
        int displayOrder,
        boolean active
) {
    public boolean hasTarget() {
        return targetUrl != null
                && !targetUrl.isBlank();
    }
}