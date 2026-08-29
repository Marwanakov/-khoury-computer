package com.khourycomputer.persistence.repository;

import com.khourycomputer.persistence.entity.ProductDealEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataProductDealRepository
        extends CrudRepository<ProductDealEntity, Long> {

    List<ProductDealEntity> findByProductId(
            Long productId
    );

    @Query("""
            SELECT *
            FROM product_deals
            WHERE starts_at <= :currentTime
              AND ends_at > :currentTime
            ORDER BY ends_at ASC
            """)
    List<ProductDealEntity> findActiveAt(
            LocalDateTime currentTime
    );

    @Query("""
            SELECT *
            FROM product_deals
            WHERE featured = TRUE
              AND starts_at <= :currentTime
              AND ends_at > :currentTime
            ORDER BY ends_at ASC
            """)
    List<ProductDealEntity> findFeaturedActiveAt(
            LocalDateTime currentTime
    );

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM product_deals
                WHERE product_id = :productId
                  AND starts_at < :endsAt
                  AND ends_at > :startsAt
            )
            """)
    boolean existsOverlapping(
            Long productId,
            LocalDateTime startsAt,
            LocalDateTime endsAt
    );

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM product_deals
                WHERE product_id = :productId
                  AND starts_at < :endsAt
                  AND ends_at > :startsAt
                  AND id <> :excludedDealId
            )
            """)
    boolean existsOverlappingExcludingId(
            Long productId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            Long excludedDealId
    );
}