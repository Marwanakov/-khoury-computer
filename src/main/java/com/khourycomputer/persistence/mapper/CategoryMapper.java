package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.Category;
import com.khourycomputer.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toDomain(
            CategoryEntity categoryEntity
    ) {
        if (categoryEntity == null) {
            return null;
        }

        return new Category(
                categoryEntity.id(),
                categoryEntity.name(),
                categoryEntity.description(),
                categoryEntity.imageUrl()
        );
    }

    public CategoryEntity toEntity(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryEntity(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl()
        );
    }
}