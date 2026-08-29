package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.ProductDeal;
import com.khourycomputer.persistence.entity.ProductDealEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductDealMapper {

    public ProductDeal toDomain(
            ProductDealEntity productDealEntity
    ) {
        if (productDealEntity == null) {
            return null;
        }

        return new ProductDeal(
                productDealEntity.id(),
                productDealEntity.productId(),
                productDealEntity.dealPrice(),
                productDealEntity.startsAt(),
                productDealEntity.endsAt(),
                productDealEntity.featured(),
                productDealEntity.createdAt()
        );
    }

    public ProductDealEntity toEntity(
            ProductDeal productDeal
    ) {
        if (productDeal == null) {
            return null;
        }

        return new ProductDealEntity(
                productDeal.getId(),
                productDeal.getProductId(),
                productDeal.getDealPrice(),
                productDeal.getStartsAt(),
                productDeal.getEndsAt(),
                productDeal.isFeatured(),
                productDeal.getCreatedAt()
        );
    }
}