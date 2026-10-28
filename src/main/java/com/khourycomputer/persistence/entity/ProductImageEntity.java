package com.khourycomputer.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("product_images")
public record ProductImageEntity(
        @Id Long id,

        @Column("product_id")
        Long productId,

        @Column("image_url")
        String imageUrl,

        @Column("display_order")
        int displayOrder,

        @Column("is_primary")
        boolean primary
) {
}