package com.khourycomputer.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("order_items")
public record OrderItemEntity(
        @Id Long id,

        @Column("product_id")
        Long productId,

        @Column("product_name")
        String productName,

        @Column("unit_price")
        BigDecimal unitPrice,

        int quantity
) {}