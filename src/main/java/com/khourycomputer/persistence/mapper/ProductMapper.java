package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.Product;
import com.khourycomputer.persistence.entity.ProductEntity;
import com.khourycomputer.persistence.entity.ProductTagEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public Product toDomain(ProductEntity productEntity) {
        if (productEntity == null) {
            return null;
        }

        return new Product(
                productEntity.id(),
                productEntity.name(),
                productEntity.description(),
                productEntity.price(),
                productEntity.brand(),
                productEntity.stockQuantity(),
                productEntity.availabilityStatus(),
                productEntity.imageUrl(),
                productEntity.categoryId(),
                mapTagsToDomain(productEntity.tags())
        );
    }

    public ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductEntity(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getBrand(),
                product.getStockQuantity(),
                product.getAvailabilityStatus(),
                product.getImageUrl(),
                product.getCategoryId(),
                mapTagsToEntity(product.getTags())
        );
    }

    // The following two methods handles tag conversion only. (SLAP-Principle)
    private Set<String> mapTagsToDomain(Set<ProductTagEntity> tags) {
        if (tags == null) {
            return Set.of();
        }

        return tags.stream()
                .map(ProductTagEntity::tag)
                .collect(Collectors.toSet());
    }

    private Set<ProductTagEntity> mapTagsToEntity(Set<String> tags) {
        if (tags == null) {
            return Set.of();
        }

        return tags.stream()
                .map(ProductTagEntity::new)
                .collect(Collectors.toSet());
    }
}