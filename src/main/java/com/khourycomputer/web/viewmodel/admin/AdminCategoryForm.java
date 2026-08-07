package com.khourycomputer.web.viewmodel.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminCategoryForm {

    @NotBlank(message = "Category name is required.")
    @Size(
            max = 100,
            message = "Category name cannot exceed 100 characters."
    )
    private String name;

    @Size(
            max = 1000,
            message = "Description cannot exceed 1000 characters."
    )
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}