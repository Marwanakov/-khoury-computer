package com.khourycomputer.persistence.entity;

import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Set;

@Table("products")
public record ProductEntity(
        @Id Long id,

        String name,

        String description,

        BigDecimal price,

        String brand,

        @Column("stock_quantity")
        int stockQuantity,

        @Column("availability_status")
        ProductAvailabilityStatus availabilityStatus,

        @Column("image_url")
        String imageUrl,

        @Column("category_id")
        Long categoryId,

        @MappedCollection(idColumn = "product_id")
        Set<ProductTagEntity> tags
){}