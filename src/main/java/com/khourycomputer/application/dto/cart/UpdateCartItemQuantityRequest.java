package com.khourycomputer.application.dto.cart;

public record UpdateCartItemQuantityRequest(
        Long productId,
        int quantity
) {
}