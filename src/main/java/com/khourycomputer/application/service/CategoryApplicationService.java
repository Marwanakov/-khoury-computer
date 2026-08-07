package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.dto.category.CreateCategoryRequest;
import com.khourycomputer.application.dto.category.UpdateCategoryRequest;
import com.khourycomputer.application.repository.CategoryRepository;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.domain.exception.CategoryNotFoundException;
import com.khourycomputer.domain.model.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryApplicationService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String name = request.name().trim();

        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category name already exists.");
        }

        Category category = new Category(
                null,
                name,
                request.description());

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        String newName = request.name().trim();

        categoryRepository.findByName(newName)
                .filter(existingCategory -> !existingCategory.getId().equals(categoryId))
                .ifPresent(existingCategory -> {
                    throw new IllegalArgumentException("Category name already exists.");
                });

        category.rename(newName);
        category.updateDescription(request.description());

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

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
    if (!categoryRepository.existsById(categoryId)) {
        throw new CategoryNotFoundException(categoryId);
    }

    if (!productRepository.findByCategoryId(categoryId).isEmpty()) {
        throw new IllegalStateException(
                "This category cannot be deleted because it still contains products."
        );
    }

    categoryRepository.deleteById(categoryId);
}

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription());
    }
}