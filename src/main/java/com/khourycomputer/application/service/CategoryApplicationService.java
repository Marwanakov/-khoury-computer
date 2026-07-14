package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.dto.category.CreateCategoryRequest;
import com.khourycomputer.application.dto.category.UpdateCategoryRequest;
import com.khourycomputer.application.repository.CategoryRepository;
import com.khourycomputer.domain.model.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;

    public CategoryApplicationService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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
                request.description()
        );

        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

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
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

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
            throw new IllegalArgumentException("Category not found.");
        }

        categoryRepository.deleteById(categoryId);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}