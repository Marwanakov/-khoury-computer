package com.khourycomputer.persistence.repository;

import com.khourycomputer.persistence.entity.ProductEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

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
}