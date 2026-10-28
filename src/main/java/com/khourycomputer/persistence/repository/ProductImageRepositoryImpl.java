package com.khourycomputer.persistence.repository;

import com.khourycomputer.application.repository.ProductImageRepository;
import com.khourycomputer.domain.model.ProductImage;
import com.khourycomputer.persistence.mapper.ProductImageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductImageRepositoryImpl
        implements ProductImageRepository {

    private final SpringDataProductImageRepository
            springDataProductImageRepository;

    private final ProductImageMapper productImageMapper;

    public ProductImageRepositoryImpl(
            SpringDataProductImageRepository
                    springDataProductImageRepository,
            ProductImageMapper productImageMapper
    ) {
        this.springDataProductImageRepository =
                springDataProductImageRepository;

        this.productImageMapper = productImageMapper;
    }

    @Override
    public List<ProductImage> findByProductId(
            Long productId
    ) {
        return springDataProductImageRepository
                .findByProductIdOrderByDisplayOrderAsc(
                        productId
                )
                .stream()
                .map(productImageMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProductImage> findById(
            Long imageId
    ) {
        return springDataProductImageRepository
                .findById(imageId)
                .map(productImageMapper::toDomain);
    }

    @Override
    public ProductImage save(
            ProductImage productImage
    ) {
        return productImageMapper.toDomain(
                springDataProductImageRepository.save(
                        productImageMapper.toEntity(
                                productImage
                        )
                )
        );
    }

    @Override
    public void deleteById(Long imageId) {
        springDataProductImageRepository
                .deleteById(imageId);
    }

    @Override
    public void deleteByProductId(Long productId) {
        springDataProductImageRepository
                .deleteByProductId(productId);
    }
}