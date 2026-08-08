package com.khourycomputer.web.viewmodel.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

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

    /*
     * MultipartFile stays exclusively in the web layer.
     */
    private MultipartFile image;

    private boolean removeImage;

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

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public boolean isRemoveImage() {
        return removeImage;
    }

    public void setRemoveImage(boolean removeImage) {
        this.removeImage = removeImage;
    }
}