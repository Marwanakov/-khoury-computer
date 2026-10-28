package com.khourycomputer.persistence.repository;

import com.khourycomputer.persistence.entity.ProductImageEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpringDataProductImageRepository
        extends CrudRepository<ProductImageEntity, Long> {

    List<ProductImageEntity>
            findByProductIdOrderByDisplayOrderAsc(
                    Long productId
            );

    void deleteByProductId(Long productId);
}