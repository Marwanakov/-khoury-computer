package com.khourycomputer.domain.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long categoryId) {
        super(
                "Category with ID "
                        + categoryId
                        + " was not found."
        );
    }
}