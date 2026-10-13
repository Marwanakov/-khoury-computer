package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.HeroSlide;
import com.khourycomputer.persistence.entity.HeroSlideEntity;
import org.springframework.stereotype.Component;

@Component
public class HeroSlideMapper {

    public HeroSlide toDomain(
            HeroSlideEntity entity
    ) {
        if (entity == null) {
            return null;
        }

        return new HeroSlide(
                entity.id(),
                entity.desktopImageUrl(),
                entity.mobileImageUrl(),
                entity.altText(),
                entity.linkType(),
                entity.linkedProductId(),
                entity.linkedCategoryId(),
                entity.displayOrder(),
                entity.active()
        );
    }

    public HeroSlideEntity toEntity(
            HeroSlide heroSlide
    ) {
        if (heroSlide == null) {
            return null;
        }

        return new HeroSlideEntity(
                heroSlide.getId(),
                heroSlide.getDesktopImageUrl(),
                heroSlide.getMobileImageUrl(),
                heroSlide.getAltText(),
                heroSlide.getLinkType(),
                heroSlide.getLinkedProductId(),
                heroSlide.getLinkedCategoryId(),
                heroSlide.getDisplayOrder(),
                heroSlide.isActive()
        );
    }
}