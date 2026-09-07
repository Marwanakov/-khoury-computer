package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.recommendation.ProductRecommendationGroup;
import com.khourycomputer.application.dto.recommendation.ProductRecommendationResponse;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import com.khourycomputer.domain.exception.ProductNotFoundException;
import com.khourycomputer.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductRecommendationService {

    private static final int MAXIMUM_PER_GROUP = 2;

    private static final BigDecimal CHEAPER_MINIMUM_RATIO =
            new BigDecimal("0.70");

    private static final BigDecimal SIMILAR_LOWER_RATIO =
            new BigDecimal("0.95");

    private static final BigDecimal SIMILAR_UPPER_RATIO =
            new BigDecimal("1.05");

    private static final BigDecimal HIGHER_MAXIMUM_RATIO =
            new BigDecimal("1.30");

    private final ProductRepository productRepository;
    private final ProductPricingService productPricingService;
    private final ProductSimilarityScorer similarityScorer;

    public ProductRecommendationService(
            ProductRepository productRepository,
            ProductPricingService productPricingService,
            ProductSimilarityScorer similarityScorer
    ) {
        this.productRepository = productRepository;
        this.productPricingService =
                productPricingService;
        this.similarityScorer = similarityScorer;
    }

    @Transactional(readOnly = true)
    public List<ProductRecommendationResponse>
            recommendAlternatives(Long productId) {

        Product viewedProduct = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                productId
                        )
                );

        BigDecimal viewedPrice =
                productPricingService
                        .getEffectiveUnitPrice(
                                viewedProduct
                        );

        List<RecommendationCandidate> candidates =
                productRepository
                        .findByCategoryId(
                                viewedProduct
                                        .getCategoryId()
                        )
                        .stream()
                        .filter(candidate ->
                                !candidate.getId().equals(
                                        viewedProduct.getId()
                                )
                        )
                        .filter(candidate ->
                                candidate
                                        .getAvailabilityStatus()
                                        != ProductAvailabilityStatus
                                        .SOLD_OUT
                        )
                        .map(candidate ->
                                createCandidate(
                                        viewedProduct,
                                        viewedPrice,
                                        candidate
                                )
                        )
                        .filter(candidate ->
                                candidate.similarity()
                                        .meaningfulMatch()
                        )
                        .sorted(candidateComparator())
                        .toList();

        Map<ProductRecommendationGroup,
                List<RecommendationCandidate>> groupedCandidates =
                groupCandidates(
                        viewedProduct,
                        viewedPrice,
                        candidates
                );

        return createResponses(groupedCandidates);
    }

    private RecommendationCandidate createCandidate(
            Product viewedProduct,
            BigDecimal viewedPrice,
            Product candidate
    ) {
        BigDecimal effectivePrice =
                productPricingService
                        .getEffectiveUnitPrice(candidate);

        ProductSimilarityScorer.SimilarityScore similarity =
                similarityScorer.calculate(
                        viewedProduct,
                        candidate
                );

        return new RecommendationCandidate(
                candidate,
                effectivePrice,
                effectivePrice
                        .subtract(viewedPrice)
                        .abs(),
                similarity
        );
    }

    private Map<ProductRecommendationGroup,
            List<RecommendationCandidate>> groupCandidates(
            Product viewedProduct,
            BigDecimal viewedPrice,
            List<RecommendationCandidate> candidates
    ) {
        Map<ProductRecommendationGroup,
                List<RecommendationCandidate>> result =
                new EnumMap<>(
                        ProductRecommendationGroup.class
                );

        Set<Long> selectedProductIds =
                new HashSet<>();

        selectCandidates(
                result,
                selectedProductIds,
                ProductRecommendationGroup.SAME_BRAND,
                candidates.stream()
                        .filter(candidate ->
                                sameBrand(
                                        viewedProduct,
                                        candidate.product()
                                )
                        )
                        .toList()
        );

        selectCandidates(
                result,
                selectedProductIds,
                ProductRecommendationGroup.CHEAPER,
                candidates.stream()
                        .filter(candidate ->
                                isCheaperAlternative(
                                        viewedPrice,
                                        candidate.effectivePrice()
                                )
                        )
                        .toList()
        );

        selectCandidates(
                result,
                selectedProductIds,
                ProductRecommendationGroup.SIMILAR_PRICE,
                candidates.stream()
                        .filter(candidate ->
                                isSimilarPrice(
                                        viewedPrice,
                                        candidate.effectivePrice()
                                )
                        )
                        .toList()
        );

        selectCandidates(
                result,
                selectedProductIds,
                ProductRecommendationGroup.HIGHER_PRICE,
                candidates.stream()
                        .filter(candidate ->
                                isHigherPriceAlternative(
                                        viewedPrice,
                                        candidate.effectivePrice()
                                )
                        )
                        .toList()
        );

        return result;
    }

    private void selectCandidates(
            Map<ProductRecommendationGroup,
                    List<RecommendationCandidate>> result,
            Set<Long> selectedProductIds,
            ProductRecommendationGroup group,
            List<RecommendationCandidate> candidates
    ) {
        List<RecommendationCandidate> selected =
                candidates.stream()
                        .filter(candidate ->
                                !selectedProductIds.contains(
                                        candidate.product()
                                                .getId()
                                )
                        )
                        .limit(MAXIMUM_PER_GROUP)
                        .toList();

        selected.forEach(candidate ->
                selectedProductIds.add(
                        candidate.product().getId()
                )
        );

        if (!selected.isEmpty()) {
            result.put(group, selected);
        }
    }

    private List<ProductRecommendationResponse> createResponses(
            Map<ProductRecommendationGroup,
                    List<RecommendationCandidate>> groupedCandidates
    ) {
        List<ProductRecommendationResponse> responses =
                new ArrayList<>();

        for (ProductRecommendationGroup group
                : ProductRecommendationGroup.values()) {

            groupedCandidates
                    .getOrDefault(group, List.of())
                    .stream()
                    .map(candidate ->
                            toResponse(group, candidate)
                    )
                    .forEach(responses::add);
        }

        return List.copyOf(responses);
    }

    private ProductRecommendationResponse toResponse(
            ProductRecommendationGroup group,
            RecommendationCandidate candidate
    ) {
        Product product = candidate.product();

        return new ProductRecommendationResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getImageUrl(),
                product.getAvailabilityStatus(),
                product.getPrice(),
                candidate.effectivePrice(),
                candidate.priceDistance(),
                group,
                candidate.similarity().reason()
        );
    }

    private Comparator<RecommendationCandidate>
            candidateComparator() {

        return Comparator
                .comparingInt(
                        (RecommendationCandidate candidate) ->
                                candidate.similarity()
                                        .score()
                )
                .reversed()
                .thenComparing(
                        RecommendationCandidate::priceDistance
                )
                .thenComparing(
                        candidate ->
                                candidate.product()
                                        .getName(),
                        String.CASE_INSENSITIVE_ORDER
                );
    }

    private boolean sameBrand(
            Product firstProduct,
            Product secondProduct
    ) {
        String firstBrand = firstProduct.getBrand();
        String secondBrand = secondProduct.getBrand();

        return firstBrand != null
                && !firstBrand.isBlank()
                && secondBrand != null
                && firstBrand.equalsIgnoreCase(
                        secondBrand
                );
    }

    private boolean isCheaperAlternative(
            BigDecimal viewedPrice,
            BigDecimal candidatePrice
    ) {
        if (viewedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal ratio =
                candidatePrice.divide(
                        viewedPrice,
                        4,
                        java.math.RoundingMode.HALF_UP
                );

        return ratio.compareTo(
                CHEAPER_MINIMUM_RATIO
        ) >= 0
                && ratio.compareTo(
                        SIMILAR_LOWER_RATIO
                ) < 0;
    }

    private boolean isSimilarPrice(
            BigDecimal viewedPrice,
            BigDecimal candidatePrice
    ) {
        if (viewedPrice.compareTo(BigDecimal.ZERO) == 0) {
            return candidatePrice.compareTo(
                    BigDecimal.ZERO
            ) == 0;
        }

        BigDecimal ratio =
                candidatePrice.divide(
                        viewedPrice,
                        4,
                        java.math.RoundingMode.HALF_UP
                );

        return ratio.compareTo(
                SIMILAR_LOWER_RATIO
        ) >= 0
                && ratio.compareTo(
                        SIMILAR_UPPER_RATIO
                ) <= 0;
    }

    private boolean isHigherPriceAlternative(
            BigDecimal viewedPrice,
            BigDecimal candidatePrice
    ) {
        if (viewedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal ratio =
                candidatePrice.divide(
                        viewedPrice,
                        4,
                        java.math.RoundingMode.HALF_UP
                );

        return ratio.compareTo(
                SIMILAR_UPPER_RATIO
        ) > 0
                && ratio.compareTo(
                        HIGHER_MAXIMUM_RATIO
                ) <= 0;
    }

    private record RecommendationCandidate(
            Product product,
            BigDecimal effectivePrice,
            BigDecimal priceDistance,
            ProductSimilarityScorer.SimilarityScore similarity
    ) {
    }
}