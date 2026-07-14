package com.khourycomputer.application.dto.category;

public record UpdateCategoryRequest(
        String name,
        String description
) {
}