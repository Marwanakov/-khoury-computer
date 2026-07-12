package com.khourycomputer.domain.model;

import java.util.Objects;

public class Category {

    private Long id;
    private String name;
    private String description;

    public Category(Long id, String name, String description) {
        setId(id);
        setName(name);
        setDescription(description);
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

    public void rename(String name) {
        setName(name);
    }

    public void updateDescription(String description) {
        setDescription(description);
    }

    private void setId(Long id) {
        this.id = id;
    }

    // name cannot be empty because a category without a name makes no sense.
    private void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        this.name = name.trim();
    }

    // description can be empty, so we safely convert null to "".
    private void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "").trim();
    }
}