package com.khourycomputer.persistence.repository;

import com.khourycomputer.application.repository.ProductDealRepository;
import com.khourycomputer.domain.model.ProductDeal;
import com.khourycomputer.persistence.entity.ProductDealEntity;
import com.khourycomputer.persistence.mapper.ProductDealMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class ProductDealRepositoryImpl
        implements ProductDealRepository {

    private final SpringDataProductDealRepository
            springDataProductDealRepository;

    private final ProductDealMapper productDealMapper;

    public ProductDealRepositoryImpl(
            SpringDataProductDealRepository
                    springDataProductDealRepository,
            ProductDealMapper productDealMapper
    ) {
        this.springDataProductDealRepository =
                springDataProductDealRepository;

        this.productDealMapper = productDealMapper;
    }

    @Override
    public List<ProductDeal> findAll() {
        return StreamSupport.stream(
                        springDataProductDealRepository
                                .findAll()
                                .spliterator(),
                        false
                )
                .map(productDealMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProductDeal> findById(Long id) {
        return springDataProductDealRepository
                .findById(id)
                .map(productDealMapper::toDomain);
    }

    @Override
    public List<ProductDeal> findByProductId(
            Long productId
    ) {
        return springDataProductDealRepository
                .findByProductId(productId)
                .stream()
                .map(productDealMapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductDeal> findActiveAt(
            LocalDateTime currentTime
    ) {
        return springDataProductDealRepository
                .findActiveAt(currentTime)
                .stream()
                .map(productDealMapper::toDomain)
                .toList();
    }

    @Override
    public List<ProductDeal> findFeaturedActiveAt(
            LocalDateTime currentTime
    ) {
        return springDataProductDealRepository
                .findFeaturedActiveAt(currentTime)
                .stream()
                .map(productDealMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsOverlapping(
            Long productId,
            LocalDateTime startsAt,
            LocalDateTime endsAt
    ) {
        return springDataProductDealRepository
                .existsOverlapping(
                        productId,
                        startsAt,
                        endsAt
                );
    }

    @Override
    public boolean existsOverlappingExcludingId(
            Long productId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            Long excludedDealId
    ) {
        return springDataProductDealRepository
                .existsOverlappingExcludingId(
                        productId,
                        startsAt,
                        endsAt,
                        excludedDealId
                );
    }

    @Override
    public ProductDeal save(ProductDeal productDeal) {
        ProductDealEntity savedEntity =
                springDataProductDealRepository.save(
                        productDealMapper.toEntity(productDeal)
                );

        return productDealMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        springDataProductDealRepository.deleteById(id);
    }
}