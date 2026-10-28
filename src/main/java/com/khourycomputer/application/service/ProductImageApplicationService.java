package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.product.ProductImageResponse;
import com.khourycomputer.application.port.storage.ImageStorage;
import com.khourycomputer.application.port.storage.ImageStorageFolder;
import com.khourycomputer.application.port.storage.ImageUpload;
import com.khourycomputer.application.repository.ProductImageRepository;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.domain.exception.ProductNotFoundException;
import com.khourycomputer.domain.model.Product;
import com.khourycomputer.domain.model.ProductImage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ProductImageApplicationService {

        public static final int MAXIMUM_PRODUCT_IMAGES = 8;

        private final ProductImageRepository productImageRepository;
        private final ProductRepository productRepository;
        private final ImageStorage imageStorage;

        public ProductImageApplicationService(
                        ProductImageRepository productImageRepository,
                        ProductRepository productRepository,
                        ImageStorage imageStorage) {
                this.productImageRepository = productImageRepository;

                this.productRepository = productRepository;

                this.imageStorage = imageStorage;
        }

        @Transactional(readOnly = true)
        public List<ProductImageResponse> listProductImages(
                        Long productId) {
                validateProductExists(productId);

                return productImageRepository
                                .findByProductId(productId)
                                .stream()
                                .sorted(imageComparator())
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional
        public List<ProductImageResponse> addImages(
                        Long productId,
                        List<ImageUpload> uploads) {
                return addImages(
                                productId,
                                uploads,
                                null);
        }

        @Transactional
        public List<ProductImageResponse> addImages(
                        Long productId,
                        List<ImageUpload> uploads,
                        Integer requestedPrimaryIndex) {
                Product product = findProduct(productId);

                List<ImageUpload> presentUploads = uploads == null
                                ? List.of()
                                : uploads.stream()
                                                .filter(Objects::nonNull)
                                                .filter(ImageUpload::isPresent)
                                                .toList();

                if (presentUploads.isEmpty()) {
                        return listResponses(productId);
                }

                List<ProductImage> existingImages = productImageRepository
                                .findByProductId(productId);

                if (existingImages.size()
                                + presentUploads.size() > MAXIMUM_PRODUCT_IMAGES) {
                        throw new IllegalArgumentException(
                                        "A product can have at most "
                                                        + MAXIMUM_PRODUCT_IMAGES
                                                        + " images.");
                }

                int nextDisplayOrder = existingImages
                                .stream()
                                .mapToInt(ProductImage::getDisplayOrder)
                                .max()
                                .orElse(-1)
                                + 1;

                boolean primaryExists = existingImages
                                .stream()
                                .anyMatch(ProductImage::isPrimary);

                List<ProductImage> newlySavedImages = new java.util.ArrayList<>();

                for (ImageUpload upload : presentUploads) {
                        String imageUrl = imageStorage.store(
                                        upload,
                                        ImageStorageFolder.PRODUCTS);

                        registerImageDeletionOnRollback(imageUrl);

                        boolean primary = !primaryExists;

                        ProductImage savedImage = productImageRepository.save(
                                        new ProductImage(
                                                        null,
                                                        productId,
                                                        imageUrl,
                                                        nextDisplayOrder,
                                                        primary));

                        newlySavedImages.add(savedImage);

                        nextDisplayOrder++;

                        if (primary) {
                                primaryExists = true;

                                product.changeImageUrl(
                                                savedImage.getImageUrl());

                                productRepository.save(product);
                        }
                }

                if (requestedPrimaryIndex != null) {
                        if (requestedPrimaryIndex < 0
                                        || requestedPrimaryIndex >= newlySavedImages.size()) {
                                throw new IllegalArgumentException(
                                                "The selected primary image is invalid.");
                        }

                        ProductImage requestedPrimary = newlySavedImages.get(
                                        requestedPrimaryIndex);

                        if (!requestedPrimary.isPrimary()) {
                                productImageRepository
                                                .findByProductId(productId)
                                                .stream()
                                                .filter(ProductImage::isPrimary)
                                                .filter(image -> !image.getId().equals(
                                                                requestedPrimary.getId()))
                                                .findFirst()
                                                .ifPresent(currentPrimary -> {
                                                        currentPrimary.removePrimaryStatus();

                                                        productImageRepository.save(
                                                                        currentPrimary);
                                                });

                                requestedPrimary.markAsPrimary();

                                productImageRepository.save(
                                                requestedPrimary);

                                product.changeImageUrl(
                                                requestedPrimary.getImageUrl());

                                productRepository.save(product);
                        }
                }

                return listResponses(productId);
        }

        @Transactional
        public List<ProductImageResponse> setPrimaryImage(
                        Long productId,
                        Long imageId) {
                Product product = findProduct(productId);

                List<ProductImage> images = productImageRepository
                                .findByProductId(productId);

                ProductImage targetImage = images.stream()
                                .filter(image -> image.getId().equals(imageId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Product image not found."));

                if (targetImage.isPrimary()) {
                        return listResponses(productId);
                }

                images.stream()
                                .filter(ProductImage::isPrimary)
                                .findFirst()
                                .ifPresent(currentPrimary -> {
                                        currentPrimary.removePrimaryStatus();
                                        productImageRepository.save(
                                                        currentPrimary);
                                });

                targetImage.markAsPrimary();
                productImageRepository.save(targetImage);

                product.changeImageUrl(
                                targetImage.getImageUrl());

                productRepository.save(product);

                return listResponses(productId);
        }

        @Transactional
        public List<ProductImageResponse> deleteImage(
                        Long productId,
                        Long imageId) {
                Product product = findProduct(productId);

                List<ProductImage> images = productImageRepository
                                .findByProductId(productId);

                ProductImage imageToDelete = images.stream()
                                .filter(image -> image.getId().equals(imageId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Product image not found."));

                boolean deletingPrimary = imageToDelete.isPrimary();

                productImageRepository.deleteById(imageId);

                List<ProductImage> remainingImages = images.stream()
                                .filter(image -> !image.getId().equals(imageId))
                                .sorted(
                                                Comparator.comparingInt(
                                                                ProductImage::getDisplayOrder))
                                .toList();

                if (deletingPrimary) {
                        if (remainingImages.isEmpty()) {
                                product.changeImageUrl("");
                        } else {
                                ProductImage newPrimary = remainingImages.get(0);

                                newPrimary.markAsPrimary();

                                productImageRepository.save(
                                                newPrimary);

                                product.changeImageUrl(
                                                newPrimary.getImageUrl());
                        }

                        productRepository.save(product);
                }

                registerImageDeletionAfterCommit(
                                imageToDelete.getImageUrl());

                return listResponses(productId);
        }

        /*
         * Keeps the compatibility products.image_url column and the
         * product_images primary row synchronized while the existing
         * single-image admin workflow is still present.
         */
        @Transactional
        public void synchronizePrimaryImage(
                        Long productId,
                        String primaryImageUrl) {
                Product product = findProduct(productId);

                List<ProductImage> images = productImageRepository
                                .findByProductId(productId);

                ProductImage currentPrimary = images.stream()
                                .filter(ProductImage::isPrimary)
                                .findFirst()
                                .orElse(null);

                if (primaryImageUrl == null
                                || primaryImageUrl.isBlank()) {
                        if (currentPrimary != null) {
                                productImageRepository.deleteById(
                                                currentPrimary.getId());
                        }

                        ProductImage replacement = images.stream()
                                        .filter(image -> currentPrimary == null
                                                        || !image.getId().equals(
                                                                        currentPrimary.getId()))
                                        .min(
                                                        Comparator.comparingInt(
                                                                        ProductImage::getDisplayOrder))
                                        .orElse(null);

                        if (replacement == null) {
                                product.changeImageUrl("");
                        } else {
                                replacement.markAsPrimary();
                                productImageRepository.save(replacement);

                                product.changeImageUrl(
                                                replacement.getImageUrl());
                        }

                        productRepository.save(product);
                        return;
                }

                if (currentPrimary != null) {
                        currentPrimary.changeImageUrl(
                                        primaryImageUrl);

                        productImageRepository.save(
                                        currentPrimary);

                        if (!primaryImageUrl.equals(
                                        product.getImageUrl())) {
                                product.changeImageUrl(
                                                primaryImageUrl);

                                productRepository.save(product);
                        }

                        return;
                }

                int nextDisplayOrder = images.stream()
                                .mapToInt(ProductImage::getDisplayOrder)
                                .max()
                                .orElse(-1)
                                + 1;

                productImageRepository.save(
                                new ProductImage(
                                                null,
                                                productId,
                                                primaryImageUrl,
                                                nextDisplayOrder,
                                                true));

                if (!primaryImageUrl.equals(
                                product.getImageUrl())) {
                        product.changeImageUrl(primaryImageUrl);
                        productRepository.save(product);
                }
        }

        @Transactional(readOnly = true)
        public List<String> listStoredImageUrls(
                        Long productId) {
                return productImageRepository
                                .findByProductId(productId)
                                .stream()
                                .map(ProductImage::getImageUrl)
                                .filter(url -> url != null && !url.isBlank())
                                .distinct()
                                .toList();
        }

        private List<ProductImageResponse> listResponses(
                        Long productId) {
                return productImageRepository
                                .findByProductId(productId)
                                .stream()
                                .sorted(imageComparator())
                                .map(this::toResponse)
                                .toList();
        }

        private Comparator<ProductImage> imageComparator() {
                return Comparator
                                .comparing(ProductImage::isPrimary)
                                .reversed()
                                .thenComparingInt(
                                                ProductImage::getDisplayOrder);
        }

        private Product findProduct(Long productId) {
                if (productId == null) {
                        throw new IllegalArgumentException(
                                        "Product ID is required.");
                }

                return productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotFoundException(
                                                productId));
        }

        private void validateProductExists(
                        Long productId) {
                findProduct(productId);
        }

        private ProductImageResponse toResponse(
                        ProductImage image) {
                return new ProductImageResponse(
                                image.getId(),
                                image.getImageUrl(),
                                image.getDisplayOrder(),
                                image.isPrimary());
        }

        private void registerImageDeletionOnRollback(
                        String imageUrl) {
                if (imageUrl == null || imageUrl.isBlank()) {
                        return;
                }

                if (!TransactionSynchronizationManager
                                .isSynchronizationActive()) {
                        return;
                }

                TransactionSynchronizationManager
                                .registerSynchronization(
                                                new TransactionSynchronization() {
                                                        @Override
                                                        public void afterCompletion(
                                                                        int status) {
                                                                if (status == STATUS_ROLLED_BACK) {
                                                                        imageStorage.delete(
                                                                                        imageUrl);
                                                                }
                                                        }
                                                });
        }

        private void registerImageDeletionAfterCommit(
                        String imageUrl) {
                if (imageUrl == null || imageUrl.isBlank()) {
                        return;
                }

                if (!TransactionSynchronizationManager
                                .isSynchronizationActive()) {
                        imageStorage.delete(imageUrl);
                        return;
                }

                TransactionSynchronizationManager
                                .registerSynchronization(
                                                new TransactionSynchronization() {
                                                        @Override
                                                        public void afterCommit() {
                                                                imageStorage.delete(
                                                                                imageUrl);
                                                        }
                                                });
        }
}