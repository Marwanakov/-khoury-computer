package com.khourycomputer.application.dto.cart;

public record AddCartItemRequest(
        Long productId,
        int quantity
) {
}