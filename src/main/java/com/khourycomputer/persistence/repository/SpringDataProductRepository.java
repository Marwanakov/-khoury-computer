package com.khourycomputer.persistence.repository;

import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import com.khourycomputer.persistence.entity.ProductEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.List;

public interface SpringDataProductRepository extends CrudRepository<ProductEntity, Long> {

        List<ProductEntity> findByCategoryId(Long categoryId);

    @Query("""
            SELECT id
            FROM products
            WHERE LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
        List<Long> findIdsByNameContaining(String keyword);

    @Query("""
            SELECT product_id
            FROM product_tags
            WHERE LOWER(tag) = LOWER(:tag)
            """)
        List<Long> findIdsByTag(String tag);

    @Query("""
        SELECT id
        FROM products
        WHERE LOWER(brand) = LOWER(:brand)
        """)
        List<Long> findIdsByBrand(String brand);

    @Query("""
        SELECT id
        FROM products
        WHERE availability_status = :availabilityStatus
        """)
        List<Long> findIdsByAvailabilityStatus(ProductAvailabilityStatus availabilityStatus);

    @Query("""
        SELECT id
        FROM products
        WHERE price BETWEEN :minPrice AND :maxPrice
        """)
        List<Long> findIdsByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);


}