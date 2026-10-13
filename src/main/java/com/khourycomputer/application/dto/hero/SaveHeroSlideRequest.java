package com.khourycomputer.application.dto.hero;

import com.khourycomputer.domain.enums.HeroLinkType;

public record SaveHeroSlideRequest(
        String altText,
        HeroLinkType linkType,
        Long linkedProductId,
        Long linkedCategoryId,
        int displayOrder,
        boolean active
) {
}