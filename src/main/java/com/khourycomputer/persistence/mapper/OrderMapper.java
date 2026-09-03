package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.Order;
import com.khourycomputer.domain.model.OrderItem;
import com.khourycomputer.persistence.entity.OrderEntity;
import com.khourycomputer.persistence.entity.OrderItemEntity;
import com.khourycomputer.domain.model.OrderDiscount;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderMapper {

    private final CustomerInfoMapper customerInfoMapper;

    public OrderMapper(CustomerInfoMapper customerInfoMapper) {
        this.customerInfoMapper = customerInfoMapper;
    }

    public Order toDomain(OrderEntity orderEntity) {
        if (orderEntity == null) {
            return null;
        }

        return new Order(
                orderEntity.id(),
                orderEntity.userId(),
                customerInfoMapper.toDomain(
                        orderEntity.customerInfo()),
                mapItemsToDomain(orderEntity.items()),
                orderEntity.status(),
                orderEntity.createdAt(),
                mapDiscountToDomain(
                        orderEntity.customDiscountAmount(),
                        orderEntity.customDiscountAppliedAt()));
    }

    public OrderEntity toEntity(Order order) {
        if (order == null) {
            return null;
        }

        return new OrderEntity(
                order.getId(),
                order.getUserId(),
                customerInfoMapper.toEntity(
                        order.getCustomerInfo()),
                mapItemsToEntity(order.getItems()),
                order.getStatus(),
                order.getCreatedAt(),
                order.getCustomDiscountAmount(),
                order.getCustomDiscountAppliedAt());
    }

    private List<OrderItem> mapItemsToDomain(List<OrderItemEntity> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(this::mapItemToDomain)
                .toList();
    }

    private List<OrderItemEntity> mapItemsToEntity(List<OrderItem> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(this::mapItemToEntity)
                .toList();
    }

    // OrderItem does not have its own mapper because it belongs inside the Order
    // aggregate.
    // These helper methods map one single order item between domain and
    // persistence.
    private OrderItem mapItemToDomain(OrderItemEntity itemEntity) {
        return new OrderItem(
                itemEntity.id(),
                itemEntity.productId(),
                itemEntity.productName(),
                itemEntity.unitPrice(),
                itemEntity.quantity());
    }

    private OrderItemEntity mapItemToEntity(OrderItem item) {
        return new OrderItemEntity(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity());
    }

    private OrderDiscount mapDiscountToDomain(
            BigDecimal amount,
            LocalDateTime appliedAt) {

        if (amount == null
                || amount.compareTo(
                        BigDecimal.ZERO) == 0) {

            return null;
        }

        return new OrderDiscount(
                amount,
                appliedAt);
    }
}