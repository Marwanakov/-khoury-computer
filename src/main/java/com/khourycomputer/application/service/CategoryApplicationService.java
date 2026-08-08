package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.dto.category.CreateCategoryRequest;
import com.khourycomputer.application.dto.category.UpdateCategoryRequest;
import com.khourycomputer.application.port.storage.ImageStorage;
import com.khourycomputer.application.port.storage.ImageStorageFolder;
import com.khourycomputer.application.port.storage.ImageUpload;
import com.khourycomputer.application.repository.CategoryRepository;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.domain.exception.CategoryNotFoundException;
import com.khourycomputer.domain.model.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

@Service
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ImageStorage imageStorage;

    public CategoryApplicationService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ImageStorage imageStorage
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.imageStorage = imageStorage;
    }

    @Transactional
    public CategoryResponse createCategory(
            CreateCategoryRequest request,
            ImageUpload image
    ) {
        String name = normalizeName(request.name());

        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Category name already exists."
            );
        }

        String imageUrl = storeImageIfPresent(image);

        registerImageDeletionOnRollback(imageUrl);

        Category category = new Category(
                null,
                name,
                request.description(),
                imageUrl
        );

        Category savedCategory =
                categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    /*
     * Compatibility overload for application callers that create a category
     * without an image.
     */
    @Transactional
    public CategoryResponse createCategory(
            CreateCategoryRequest request
    ) {
        return createCategory(
                request,
                ImageUpload.empty()
        );
    }

    @Transactional
    public CategoryResponse updateCategory(
            Long categoryId,
            UpdateCategoryRequest request,
            ImageUpload newImage,
            boolean removeImage
    ) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(
                        () -> new CategoryNotFoundException(
                                categoryId
                        )
                );

        String newName = normalizeName(request.name());

        categoryRepository.findByName(newName)
                .filter(existingCategory ->
                        !existingCategory
                                .getId()
                                .equals(categoryId)
                )
                .ifPresent(existingCategory -> {
                    throw new IllegalArgumentException(
                            "Category name already exists."
                    );
                });

        String oldImageUrl = category.getImageUrl();

        String resultingImageUrl =
                determineResultingImageUrl(
                        oldImageUrl,
                        newImage,
                        removeImage
                );

        category.rename(newName);
        category.updateDescription(request.description());
        category.changeImageUrl(resultingImageUrl);

        Category savedCategory =
                categoryRepository.save(category);

        if (!Objects.equals(
                oldImageUrl,
                resultingImageUrl
        )) {
            registerImageDeletionAfterCommit(
                    oldImageUrl
            );
        }

        return toResponse(savedCategory);
    }

    /*
     * Compatibility overload that preserves the current image.
     */
    @Transactional
    public CategoryResponse updateCategory(
            Long categoryId,
            UpdateCategoryRequest request
    ) {
        return updateCategory(
                categoryId,
                request,
                ImageUpload.empty(),
                false
        );
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(
            Long categoryId
    ) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(
                        () -> new CategoryNotFoundException(
                                categoryId
                        )
                );

        return toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(
                        () -> new CategoryNotFoundException(
                                categoryId
                        )
                );

        if (!productRepository
                .findByCategoryId(categoryId)
                .isEmpty()) {
            throw new IllegalStateException(
                    "This category cannot be deleted because "
                            + "it still contains products."
            );
        }

        categoryRepository.deleteById(categoryId);

        registerImageDeletionAfterCommit(
                category.getImageUrl()
        );
    }

    private String storeImageIfPresent(
            ImageUpload image
    ) {
        if (image == null || !image.isPresent()) {
            return "";
        }

        return imageStorage.store(
                image,
                ImageStorageFolder.CATEGORIES
        );
    }

    private String determineResultingImageUrl(
            String oldImageUrl,
            ImageUpload newImage,
            boolean removeImage
    ) {
        if (newImage != null && newImage.isPresent()) {
            String newImageUrl = imageStorage.store(
                    newImage,
                    ImageStorageFolder.CATEGORIES
            );

            registerImageDeletionOnRollback(
                    newImageUrl
            );

            return newImageUrl;
        }

        if (removeImage) {
            return "";
        }

        return oldImageUrl;
    }

    /*
     * If the database transaction fails, a newly stored image must not remain
     * orphaned on disk.
     */
    private void registerImageDeletionOnRollback(
            String imageUrl
    ) {
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
                                    int status
                            ) {
                                if (status
                                        == STATUS_ROLLED_BACK) {
                                    imageStorage.delete(
                                            imageUrl
                                    );
                                }
                            }
                        }
                );
    }

    /*
     * Old images are deleted only after the database successfully commits.
     * This prevents the old image from disappearing if the update rolls back.
     */
    private void registerImageDeletionAfterCommit(
            String imageUrl
    ) {
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
                                        imageUrl
                                );
                            }
                        }
                );
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Category name cannot be empty."
            );
        }

        return name.trim();
    }

    private CategoryResponse toResponse(
            Category category
    ) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl()
        );
    }
}