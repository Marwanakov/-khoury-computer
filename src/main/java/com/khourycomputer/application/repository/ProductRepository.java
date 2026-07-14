package com.khourycomputer.application.repository;

import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import com.khourycomputer.domain.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findAll();

    Optional<Product> findById(Long id);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByNameContaining(String keyword);

    List<Product> findByTag(String tag);

    List<Product> findByBrand(String brand);

    List<Product> findByAvailabilityStatus(ProductAvailabilityStatus availabilityStatus);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    boolean existsById(Long id);

    Product save(Product product);

    void deleteById(Long id);
}