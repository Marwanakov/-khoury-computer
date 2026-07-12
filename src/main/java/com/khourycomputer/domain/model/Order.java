package com.khourycomputer.domain.model;

import com.khourycomputer.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {

    private Long id;
    private Long userId;
    private CustomerInfo customerInfo;
    private List<OrderItem> items;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public Order(
            Long id,
            Long userId,
            String customerName,
            String customerEmail,
            String customerPhoneNumber,
            Address deliveryAddress,
            List<OrderItem> items,
            OrderStatus status,
            LocalDateTime createdAt
    ) {
        setId(id);
        setUserId(userId);
        setCustomerInfo(customerInfo);
        setItems(items);
        setStatus(status);
        setCreatedAt(createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed.");
        }

        status = OrderStatus.CONFIRMED;
    }

    public void complete() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be completed.");
        }

        status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be cancelled.");
        }

        status = OrderStatus.CANCELLED;
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Order must belong to a user.");
        }

        this.userId = userId;
    }

    private void setCustomerInfo(CustomerInfo customerInfo) {
        if (customerInfo == null) {
            throw new IllegalArgumentException("Customer info cannot be null.");
        }

        this.customerInfo = customerInfo;
    }

    private void setItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        if (items.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Order items cannot contain null values.");
        }

        this.items = new ArrayList<>(items);
    }

    private void setStatus(OrderStatus status) {
        this.status = Objects.requireNonNullElse(status, OrderStatus.PENDING);
    }

    private void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNullElse(createdAt, LocalDateTime.now());
    }
}