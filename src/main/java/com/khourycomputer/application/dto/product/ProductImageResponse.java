package com.khourycomputer.application.dto.product;

public record ProductImageResponse(
        Long id,
        String imageUrl,
        int displayOrder,
        boolean primary
) {
}