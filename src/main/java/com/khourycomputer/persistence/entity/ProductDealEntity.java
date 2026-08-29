package com.khourycomputer.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("product_deals")
public record ProductDealEntity(
        @Id
        Long id,

        @Column("product_id")
        Long productId,

        @Column("deal_price")
        BigDecimal dealPrice,

        @Column("starts_at")
        LocalDateTime startsAt,

        @Column("ends_at")
        LocalDateTime endsAt,

        boolean featured,

        @Column("created_at")
        LocalDateTime createdAt
) {
}