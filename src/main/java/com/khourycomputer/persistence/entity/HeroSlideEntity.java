package com.khourycomputer.persistence.entity;

import com.khourycomputer.domain.enums.HeroLinkType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("hero_slides")
public record HeroSlideEntity(
        @Id Long id,

        @Column("desktop_image_url")
        String desktopImageUrl,

        @Column("mobile_image_url")
        String mobileImageUrl,

        @Column("alt_text")
        String altText,

        @Column("link_type")
        HeroLinkType linkType,

        @Column("linked_product_id")
        Long linkedProductId,

        @Column("linked_category_id")
        Long linkedCategoryId,

        @Column("display_order")
        int displayOrder,

        boolean active
) {
}