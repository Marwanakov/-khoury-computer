package com.khourycomputer.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("carts")
public record CartEntity(
        @Id Long id,

        @Column("user_id")
        Long userId,

        @MappedCollection(idColumn = "cart_id", keyColumn = "cart_item_order")
        List<CartItemEntity> items
) {}