package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.ProductImage;
import com.khourycomputer.persistence.entity.ProductImageEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {

    public ProductImage toDomain(
            ProductImageEntity entity
    ) {
        if (entity == null) {
            return null;
        }

        return new ProductImage(
                entity.id(),
                entity.productId(),
                entity.imageUrl(),
                entity.displayOrder(),
                entity.primary()
        );
    }

    public ProductImageEntity toEntity(
            ProductImage image
    ) {
        if (image == null) {
            return null;
        }

        return new ProductImageEntity(
                image.getId(),
                image.getProductId(),
                image.getImageUrl(),
                image.getDisplayOrder(),
                image.isPrimary()
        );
    }
}