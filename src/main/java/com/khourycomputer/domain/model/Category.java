package com.khourycomputer.domain.model;

import java.util.Objects;

public class Category {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;

    public Category(
            Long id,
            String name,
            String description,
            String imageUrl
    ) {
        setId(id);
        setName(name);
        setDescription(description);
        setImageUrl(imageUrl);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void rename(String name) {
        setName(name);
    }

    public void updateDescription(String description) {
        setDescription(description);
    }

    public void changeImageUrl(String imageUrl) {
        setImageUrl(imageUrl);
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Category name cannot be empty."
            );
        }

        this.name = name.trim();
    }

    private void setDescription(String description) {
        this.description = Objects
                .requireNonNullElse(description, "")
                .trim();
    }

    private void setImageUrl(String imageUrl) {
        this.imageUrl = Objects
                .requireNonNullElse(imageUrl, "")
                .trim();
    }
}