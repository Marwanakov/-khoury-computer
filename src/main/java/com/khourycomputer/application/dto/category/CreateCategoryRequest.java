package com.khourycomputer.application.dto.category;

public record CreateCategoryRequest(
        String name,
        String description
) {
}