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
    private OrderDiscount customDiscount;

    public Order(
            Long id,
            Long userId,
            CustomerInfo customerInfo,
            List<OrderItem> items,
            OrderStatus status,
            LocalDateTime createdAt,
            OrderDiscount customDiscount) {

        setId(id);
        setUserId(userId);
        setCustomerInfo(customerInfo);
        setItems(items);
        setStatus(status);
        setCreatedAt(createdAt);
        setCustomDiscount(customDiscount);
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

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }

    public BigDecimal getCustomDiscountAmount() {
        if (customDiscount == null) {
            return BigDecimal.ZERO;
        }

        return customDiscount.getAmount();
    }

    public LocalDateTime getCustomDiscountAppliedAt() {
        if (customDiscount == null) {
            return null;
        }

        return customDiscount.getAppliedAt();
    }

    public boolean hasCustomDiscount() {
        return customDiscount != null;
    }

    public BigDecimal getTotalPrice() {
        if (customDiscount == null) {
            return getSubtotal();
        }

        return customDiscount.applyTo(
                getSubtotal());
    }

    public void applyAgreedFinalTotal(
            BigDecimal agreedFinalTotal,
            LocalDateTime appliedAt) {

        validateCustomDiscountCanBeChanged();

        if (agreedFinalTotal == null) {
            throw new IllegalArgumentException(
                    "Agreed final total cannot be empty.");
        }

        if (agreedFinalTotal.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Agreed final total must be "
                            + "greater than zero.");
        }

        if (agreedFinalTotal
                .stripTrailingZeros()
                .scale() > 2) {

            throw new IllegalArgumentException(
                    "Agreed final total cannot have "
                            + "more than two decimal places.");
        }

        BigDecimal subtotal = getSubtotal();

        if (agreedFinalTotal.compareTo(subtotal) >= 0) {
            throw new IllegalArgumentException(
                    "Agreed final total must be lower "
                            + "than the order subtotal.");
        }

        BigDecimal discountAmount = subtotal.subtract(agreedFinalTotal);

        customDiscount = new OrderDiscount(
                discountAmount,
                appliedAt);
    }

    public void removeCustomDiscount() {
        validateCustomDiscountCanBeChanged();

        if (!hasCustomDiscount()) {
            throw new IllegalStateException(
                    "This order does not have "
                            + "a custom discount.");
        }

        customDiscount = null;
    }

    private void validateCustomDiscountCanBeChanged() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Custom discounts can only be changed "
                            + "while an order is pending.");
        }
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
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "This order has already been cancelled.");
        }

        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Completed orders cannot be cancelled.");
        }

        if (status != OrderStatus.PENDING
                && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only pending or confirmed orders can be cancelled.");
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

    private void setCustomDiscount(
            OrderDiscount customDiscount) {

        if (customDiscount != null
                && customDiscount.getAmount()
                        .compareTo(getSubtotal()) > 0) {

            throw new IllegalArgumentException(
                    "Custom discount cannot exceed "
                            + "the order subtotal.");
        }

        this.customDiscount = customDiscount;
    }
}