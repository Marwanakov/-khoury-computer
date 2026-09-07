package com.khourycomputer.web.viewmodel.product;

import com.khourycomputer.application.dto.recommendation.ProductRecommendationResponse;

import java.util.List;

public record ProductRecommendationSectionViewModel(
        String title,
        String description,
        List<ProductRecommendationResponse> products
) {
}