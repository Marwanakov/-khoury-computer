package com.khourycomputer.persistence.entity;

import com.khourycomputer.domain.enums.OrderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Table("orders")
public record OrderEntity(
        @Id Long id,

        @Column("user_id")
        Long userId,

        @Embedded.Nullable(prefix = "customer_")
        CustomerInfoEntity customerInfo,

        @MappedCollection(idColumn = "order_id", keyColumn = "order_item_order")
        List<OrderItemEntity> items,

        OrderStatus status,

        @Column("created_at")
        LocalDateTime createdAt
) {}