package com.khourycomputer.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("categories")
public record CategoryEntity(
        @Id Long id,
        String name,
        String description,

        @Column("image_url")
        String imageUrl
) {
}