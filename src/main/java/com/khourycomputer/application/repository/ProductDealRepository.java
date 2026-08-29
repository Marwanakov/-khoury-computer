package com.khourycomputer.application.repository;

import com.khourycomputer.domain.model.ProductDeal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductDealRepository {

    List<ProductDeal> findAll();

    Optional<ProductDeal> findById(Long id);

    List<ProductDeal> findByProductId(Long productId);

    List<ProductDeal> findActiveAt(LocalDateTime currentTime);

    List<ProductDeal> findFeaturedActiveAt(
            LocalDateTime currentTime
    );

    boolean existsOverlapping(
            Long productId,
            LocalDateTime startsAt,
            LocalDateTime endsAt
    );

    boolean existsOverlappingExcludingId(
            Long productId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            Long excludedDealId
    );

    ProductDeal save(ProductDeal productDeal);

    void deleteById(Long id);
}