package com.khourycomputer.persistence.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("product_tags")
public record ProductTagEntity(
        @Column("tag")
        String tag
) {}