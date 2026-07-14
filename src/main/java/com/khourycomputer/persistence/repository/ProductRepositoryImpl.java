package com.khourycomputer.persistence.repository;

import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import com.khourycomputer.domain.model.Product;
import com.khourycomputer.persistence.mapper.ProductMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final SpringDataProductRepository springDataProductRepository;
    private final ProductMapper productMapper;

    public ProductRepositoryImpl(
            SpringDataProductRepository springDataProductRepository,
            ProductMapper productMapper
    ) {
        this.springDataProductRepository = springDataProductRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<Product> findAll() {
        return StreamSupport.stream(springDataProductRepository.findAll().spliterator(), false)
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return springDataProductRepository.findById(id)
                .map(productMapper::toDomain);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return springDataProductRepository.findByCategoryId(categoryId)
                .stream()
                .map(productMapper::toDomain)
                .toList();
    }

    // Custom searches return product IDs first because the result must be reloaded
    // through findAllById(...) so Spring Data JDBC can load the full Product aggregate,
    // including its child product tags.
    @Override
    public List<Product> findByNameContaining(String keyword) {
        List<Long> productIds = springDataProductRepository.findIdsByNameContaining(keyword);

        return StreamSupport.stream(springDataProductRepository.findAllById(productIds).spliterator(), false)
                .map(productMapper::toDomain)
                .toList();
    }

    // Product tags are stored in a separate child table, so we first search product_tags
    // for matching product IDs, then load the complete ProductEntity objects.
    @Override
    public List<Product> findByTag(String tag) {
        List<Long> productIds = springDataProductRepository.findIdsByTag(tag);

        return StreamSupport.stream(springDataProductRepository.findAllById(productIds).spliterator(), false)
                .map(productMapper::toDomain)
                .toList();
    }

    // Brand filter returns product IDs first, then loads complete Product aggregates with tags.
    @Override
    public List<Product> findByBrand(String brand) {
    List<Long> productIds = springDataProductRepository.findIdsByBrand(brand);

    return StreamSupport.stream(springDataProductRepository.findAllById(productIds).spliterator(), false)
            .map(productMapper::toDomain)
            .toList();
    }

    // Availability filter returns product IDs first, then loads complete Product aggregates with tags.
    @Override
    public List<Product> findByAvailabilityStatus(ProductAvailabilityStatus availabilityStatus) {
    List<Long> productIds = springDataProductRepository.findIdsByAvailabilityStatus(availabilityStatus);

    return StreamSupport.stream(springDataProductRepository.findAllById(productIds).spliterator(), false)
            .map(productMapper::toDomain)
            .toList();
    }

    // Price filter returns product IDs first, then loads complete Product aggregates with tags.
    @Override
    public List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
    List<Long> productIds = springDataProductRepository.findIdsByPriceBetween(minPrice, maxPrice);

    return StreamSupport.stream(springDataProductRepository.findAllById(productIds).spliterator(), false)
            .map(productMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return springDataProductRepository.existsById(id);
    }

    // Domain Product -> ProductEntity -> save in database -> saved ProductEntity with generated ID -> Domain Product
    @Override
    public Product save(Product product) {
        return productMapper.toDomain(
                springDataProductRepository.save(productMapper.toEntity(product))
        );
    }

    @Override
    public void deleteById(Long id) {
        springDataProductRepository.deleteById(id);
    }
}