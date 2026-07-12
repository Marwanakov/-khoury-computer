package com.khourycomputer.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart {

    private Long id;
    private Long userId;
    private List<CartItem> items;

    public Cart(Long id, Long userId, List<CartItem> items) {
        setId(id);
        setUserId(userId);
        setItems(items);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void addItem(CartItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cart item cannot be null.");
        }

        items.add(item);
    }

    public void removeItem(Long cartItemId) {
        if (cartItemId == null) {
            throw new IllegalArgumentException("Cart item id cannot be null.");
        }

        items.removeIf(item -> item.getId().equals(cartItemId));
    }

    public void clear() {
        items.clear();
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Cart must belong to a user.");
        }

        this.userId = userId;
    }

    private void setItems(List<CartItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }
}