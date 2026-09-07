package com.khourycomputer.web.viewmodel.product;

import com.khourycomputer.application.dto.recommendation.ProductRecommendationGroup;
import com.khourycomputer.application.dto.recommendation.ProductRecommendationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductRecommendationSectionFactory {

    public List<ProductRecommendationSectionViewModel>
            createSections(
                    List<ProductRecommendationResponse>
                            recommendations
            ) {

        if (recommendations == null
                || recommendations.isEmpty()) {
            return List.of();
        }

        return List.of(
                        createSection(
                                ProductRecommendationGroup.SAME_BRAND,
                                "Same brand",
                                "More options from a brand "
                                        + "you are already considering.",
                                recommendations
                        ),
                        createSection(
                                ProductRecommendationGroup.CHEAPER,
                                "Cheaper alternatives",
                                "Similar options at a lower price.",
                                recommendations
                        ),
                        createSection(
                                ProductRecommendationGroup.SIMILAR_PRICE,
                                "Similar price",
                                "Closely related products within "
                                        + "a similar budget.",
                                recommendations
                        ),
                        createSection(
                                ProductRecommendationGroup.HIGHER_PRICE,
                                "For a little more",
                                "Explore related options with a "
                                        + "slightly higher price.",
                                recommendations
                        )
                )
                .stream()
                .filter(section ->
                        !section.products().isEmpty()
                )
                .toList();
    }

    private ProductRecommendationSectionViewModel
            createSection(
                    ProductRecommendationGroup group,
                    String title,
                    String description,
                    List<ProductRecommendationResponse>
                            recommendations
            ) {

        List<ProductRecommendationResponse> products =
                recommendations.stream()
                        .filter(recommendation ->
                                recommendation.group() == group
                        )
                        .toList();

        return new ProductRecommendationSectionViewModel(
                title,
                description,
                products
        );
    }
}