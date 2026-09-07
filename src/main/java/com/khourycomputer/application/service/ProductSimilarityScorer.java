package com.khourycomputer.application.service;

import com.khourycomputer.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductSimilarityScorer {

    private static final int SAME_BRAND_SCORE = 100;
    private static final int SHARED_NAME_TERM_SCORE = 35;
    private static final int SHARED_TAG_SCORE = 30;
    private static final int EXACT_SPECIFICATION_SCORE = 25;
    private static final int SHARED_SPECIFICATION_TERM_SCORE = 6;

    private static final Set<String> IGNORED_TERMS =
            Set.of(
                    "a",
                    "an",
                    "and",
                    "the",
                    "with",
                    "for",
                    "from",
                    "new",
                    "product",
                    "device",
                    "computer",
                    "laptop",
                    "desktop",
                    "gaming",
                    "black",
                    "white"
            );

    public SimilarityScore calculate(
            Product viewedProduct,
            Product candidate
    ) {
        if (viewedProduct == null || candidate == null) {
            throw new IllegalArgumentException(
                    "Products cannot be null."
            );
        }

        boolean sameBrand = sameNonBlankText(
                viewedProduct.getBrand(),
                candidate.getBrand()
        );

        int sharedNameTerms = countSharedTerms(
                viewedProduct.getName(),
                candidate.getName()
        );

        int sharedTags = countSharedTags(
                viewedProduct.getTags(),
                candidate.getTags()
        );

        SpecificationSimilarity specificationSimilarity =
                compareSpecifications(
                        viewedProduct.getSpecifications(),
                        candidate.getSpecifications()
                );

        int score = 0;

        if (sameBrand) {
            score += SAME_BRAND_SCORE;
        }

        score += sharedNameTerms
                * SHARED_NAME_TERM_SCORE;

        score += sharedTags
                * SHARED_TAG_SCORE;

        score += specificationSimilarity
                .exactValues()
                * EXACT_SPECIFICATION_SCORE;

        score += specificationSimilarity
                .sharedTerms()
                * SHARED_SPECIFICATION_TERM_SCORE;

        boolean meaningfulMatch =
                sameBrand
                        || sharedNameTerms > 0
                        || sharedTags > 0
                        || specificationSimilarity.exactValues() > 0
                        || specificationSimilarity.sharedTerms() >= 2;

        return new SimilarityScore(
                score,
                meaningfulMatch,
                determineReason(
                        sameBrand,
                        sharedNameTerms,
                        sharedTags,
                        specificationSimilarity
                )
        );
    }

    private String determineReason(
            boolean sameBrand,
            int sharedNameTerms,
            int sharedTags,
            SpecificationSimilarity specificationSimilarity
    ) {
        if (sameBrand
                && specificationSimilarity.hasSpecificationMatch()) {
            return "Same brand with similar specifications";
        }

        if (sameBrand) {
            return "Another option from the same brand";
        }

        if (sharedNameTerms > 0
                && specificationSimilarity.hasSpecificationMatch()) {
            return "Similar model and specifications";
        }

        if (sharedTags > 0
                && specificationSimilarity.hasSpecificationMatch()) {
            return "Similar features and specifications";
        }

        if (sharedNameTerms > 0) {
            return "Similar product model";
        }

        if (sharedTags > 0) {
            return "Similar product features";
        }

        if (specificationSimilarity.hasSpecificationMatch()) {
            return "Similar technical specifications";
        }

        return "Similar product";
    }

    private int countSharedTerms(
            String firstValue,
            String secondValue
    ) {
        Set<String> firstTerms =
                meaningfulTerms(firstValue);

        Set<String> secondTerms =
                meaningfulTerms(secondValue);

        firstTerms.retainAll(secondTerms);

        return firstTerms.size();
    }

    private int countSharedTags(
            Set<String> firstTags,
            Set<String> secondTags
    ) {
        if (firstTags == null
                || secondTags == null
                || firstTags.isEmpty()
                || secondTags.isEmpty()) {
            return 0;
        }

        Set<String> normalizedFirstTags = firstTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(this::normalize)
                .collect(Collectors.toCollection(
                        LinkedHashSet::new
                ));

        Set<String> normalizedSecondTags = secondTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(this::normalize)
                .collect(Collectors.toSet());

        normalizedFirstTags.retainAll(
                normalizedSecondTags
        );

        return normalizedFirstTags.size();
    }

    private SpecificationSimilarity compareSpecifications(
            String firstSpecifications,
            String secondSpecifications
    ) {
        Map<String, String> firstValues =
                parseSpecifications(firstSpecifications);

        Map<String, String> secondValues =
                parseSpecifications(secondSpecifications);

        int exactValues = 0;
        int sharedTerms = 0;

        for (Map.Entry<String, String> firstEntry
                : firstValues.entrySet()) {

            String secondValue = secondValues.get(
                    firstEntry.getKey()
            );

            if (secondValue == null) {
                continue;
            }

            String firstValue = firstEntry.getValue();

            if (firstValue.equals(secondValue)) {
                if (isMeaningfulSpecificationValue(
                        firstValue
                )) {
                    exactValues++;
                }

                continue;
            }

            sharedTerms += countSharedTerms(
                    firstValue,
                    secondValue
            );
        }

        return new SpecificationSimilarity(
                exactValues,
                Math.min(sharedTerms, 5)
        );
    }

    private Map<String, String> parseSpecifications(
            String specifications
    ) {
        if (specifications == null
                || specifications.isBlank()) {
            return Map.of();
        }

        Map<String, String> parsedSpecifications =
                new HashMap<>();

        specifications.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .forEach(line -> {
                    int separatorIndex =
                            line.indexOf(':');

                    if (separatorIndex <= 0
                            || separatorIndex
                            >= line.length() - 1) {
                        return;
                    }

                    String label = normalize(
                            line.substring(
                                    0,
                                    separatorIndex
                            )
                    );

                    String value = normalize(
                            line.substring(
                                    separatorIndex + 1
                            )
                    );

                    if (!label.isBlank()
                            && !value.isBlank()) {
                        parsedSpecifications.put(
                                label,
                                value
                        );
                    }
                });

        return parsedSpecifications;
    }

    private boolean isMeaningfulSpecificationValue(
            String value
    ) {
        Set<String> terms = meaningfulTerms(value);

        if (terms.isEmpty()) {
            return false;
        }

        return terms.stream()
                .anyMatch(term ->
                        term.chars()
                                .anyMatch(
                                        Character::isDigit
                                )
                                || term.length() >= 3
                );
    }

    private Set<String> meaningfulTerms(String value) {
        if (value == null || value.isBlank()) {
            return new HashSet<>();
        }

        return Arrays.stream(
                        normalize(value).split(" ")
                )
                .filter(term -> !term.isBlank())
                .filter(term ->
                        !IGNORED_TERMS.contains(term)
                )
                .collect(Collectors.toCollection(
                        HashSet::new
                ));
    }

    private boolean sameNonBlankText(
            String firstValue,
            String secondValue
    ) {
        String normalizedFirst =
                normalize(firstValue);

        String normalizedSecond =
                normalize(secondValue);

        return !normalizedFirst.isBlank()
                && normalizedFirst.equals(
                        normalizedSecond
                );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        boolean previousWasSpace = false;

        for (char character
                : value.toLowerCase(Locale.ROOT)
                .trim()
                .toCharArray()) {

            if (Character.isLetterOrDigit(character)) {
                result.append(character);
                previousWasSpace = false;
                continue;
            }

            if (!previousWasSpace && !result.isEmpty()) {
                result.append(' ');
                previousWasSpace = true;
            }
        }

        return result.toString().trim();
    }

    public record SimilarityScore(
            int score,
            boolean meaningfulMatch,
            String reason
    ) {
    }

    private record SpecificationSimilarity(
            int exactValues,
            int sharedTerms
    ) {

        private boolean hasSpecificationMatch() {
            return exactValues > 0 || sharedTerms >= 2;
        }
    }
}