package com.khourycomputer.application.repository;

import com.khourycomputer.domain.model.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository {

    List<ProductImage> findByProductId(Long productId);

    Optional<ProductImage> findById(Long imageId);

    ProductImage save(ProductImage productImage);

    void deleteById(Long imageId);

    void deleteByProductId(Long productId);
}