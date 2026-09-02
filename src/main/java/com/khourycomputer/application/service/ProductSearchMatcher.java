package com.khourycomputer.application.service;

import com.khourycomputer.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class ProductSearchMatcher {

    public static final int NO_MATCH = -1;

    private static final int EXACT_MATCH = 2;
    private static final int APPROXIMATE_MATCH = 1;

    public boolean matches(
            Product product,
            String categoryName,
            String query) {

        return calculateRelevance(
                product,
                categoryName,
                query) != NO_MATCH;
    }

    public boolean matchesExactly(
            Product product,
            String categoryName,
            String query) {

        return calculateRelevance(
                product,
                categoryName,
                query,
                false) != NO_MATCH;
    }

    public int calculateRelevance(
            Product product,
            String categoryName,
            String query) {

        return calculateRelevance(
                product,
                categoryName,
                query,
                true);
    }

    private int calculateRelevance(
            Product product,
            String categoryName,
            String query,
            boolean allowApproximateMatching) {

        NormalizedText normalizedQuery = normalize(query);
        List<String> queryTerms = extractTerms(normalizedQuery);

        if (queryTerms.isEmpty()) {
            return 0;
        }

        SearchDocument document = createSearchDocument(
                product,
                categoryName);

        int relevanceScore = 0;

        for (String term : queryTerms) {
            int termScore = document.scoreTerm(
                    term,
                    allowApproximateMatching);

            if (termScore == NO_MATCH) {
                return NO_MATCH;
            }

            relevanceScore += termScore;
        }

        relevanceScore += document.scoreCompleteQuery(
                normalizedQuery);

        return relevanceScore;
    }

    public boolean hasQuery(String query) {
        return !extractTerms(normalize(query)).isEmpty();
    }

    private SearchDocument createSearchDocument(
            Product product,
            String categoryName) {

        List<NormalizedText> normalizedTags = normalizeTags(
                product.getTags());

        return new SearchDocument(
                normalize(product.getName()),
                normalize(product.getBrand()),
                normalize(categoryName),
                normalizedTags,
                normalize(product.getSpecifications()),
                normalize(product.getDescription()));
    }

    private List<NormalizedText> normalizeTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        List<NormalizedText> normalizedTags = new ArrayList<>();

        for (String tag : tags) {
            NormalizedText normalizedTag = normalize(tag);

            if (!normalizedTag.isBlank()) {
                normalizedTags.add(normalizedTag);
            }
        }

        return List.copyOf(normalizedTags);
    }

    private List<String> extractTerms(
            NormalizedText normalizedQuery) {

        if (normalizedQuery.isBlank()) {
            return List.of();
        }

        return normalizedQuery.tokens();
    }

    private NormalizedText normalize(String value) {
        if (value == null || value.isBlank()) {
            return new NormalizedText("", "");
        }

        String lowerCaseValue = value
                .toLowerCase(Locale.ROOT)
                .trim();

        StringBuilder spacedBuilder = new StringBuilder();
        boolean previousCharacterWasSpace = false;

        for (int index = 0;
             index < lowerCaseValue.length();
             index++) {

            char currentCharacter =
                    lowerCaseValue.charAt(index);

            if (Character.isLetterOrDigit(
                    currentCharacter)) {

                spacedBuilder.append(currentCharacter);
                previousCharacterWasSpace = false;
                continue;
            }

            if (!previousCharacterWasSpace
                    && !spacedBuilder.isEmpty()) {

                spacedBuilder.append(' ');
                previousCharacterWasSpace = true;
            }
        }

        String spacedText = spacedBuilder
                .toString()
                .trim();

        String compactText = spacedText.replace(" ", "");

        return new NormalizedText(
                spacedText,
                compactText);
    }

    private static int determineMatchQuality(
            NormalizedText field,
            String queryTerm,
            boolean allowApproximateMatching) {

        if (queryTerm == null || queryTerm.isBlank()) {
            return NO_MATCH;
        }

        if (containsDigit(queryTerm)) {
            return matchesTechnicalTerm(
                    field,
                    queryTerm)
                    ? EXACT_MATCH
                    : NO_MATCH;
        }

        if (matchesExactTextTerm(
                field,
                queryTerm)) {

            return EXACT_MATCH;
        }

        if (!allowApproximateMatching) {
            return NO_MATCH;
        }

        for (String fieldToken : field.tokens()) {
            if (hasEquivalentWordForm(
                    queryTerm,
                    fieldToken)) {

                return APPROXIMATE_MATCH;
            }
        }

        if (!isEligibleForFuzzyMatching(queryTerm)) {
            return NO_MATCH;
        }

        int allowedDistance =
                allowedEditDistance(queryTerm);

        for (String fieldToken : field.tokens()) {
            if (!isEligibleForFuzzyMatching(
                    fieldToken)) {

                continue;
            }

            if (Math.abs(
                    queryTerm.length()
                            - fieldToken.length())
                    > allowedDistance) {

                continue;
            }

            int distance =
                    damerauLevenshteinDistance(
                            queryTerm,
                            fieldToken);

            if (distance <= allowedDistance) {
                return APPROXIMATE_MATCH;
            }
        }

        return NO_MATCH;
    }

    private static boolean matchesExactTextTerm(
            NormalizedText field,
            String queryTerm) {

        return field.tokens()
                .stream()
                .anyMatch(fieldToken ->
                        fieldToken.equals(queryTerm)
                                || matchesWordPrefix(
                                        fieldToken,
                                        queryTerm)
                                || matchesUnitSuffix(
                                        fieldToken,
                                        queryTerm));
    }

    private static boolean matchesWordPrefix(
            String fieldToken,
            String queryTerm) {

        return queryTerm.length() >= 3
                && fieldToken.chars()
                        .allMatch(Character::isLetter)
                && fieldToken.startsWith(queryTerm);
    }

    private static boolean matchesUnitSuffix(
            String fieldToken,
            String queryTerm) {

        if (!fieldToken.endsWith(queryTerm)) {
            return false;
        }

        String prefix = fieldToken.substring(
                0,
                fieldToken.length()
                        - queryTerm.length());

        return !prefix.isBlank()
                && prefix.chars()
                        .allMatch(Character::isDigit);
    }

    private static boolean matchesTechnicalTerm(
            NormalizedText field,
            String queryTerm) {

        List<String> fieldTokens = field.tokens();

        if (fieldTokens.stream()
                .anyMatch(queryTerm::equals)) {

            return true;
        }

        if (containsOnlyDigits(queryTerm)) {
            return fieldTokens.stream()
                    .anyMatch(token ->
                            matchesLetterPrefixedNumber(
                                    token,
                                    queryTerm)
                                    || matchesNumberWithUnit(
                                            token,
                                            queryTerm));
        }

        for (int index = 0;
             index < fieldTokens.size() - 1;
             index++) {

            String joinedTokens =
                    fieldTokens.get(index)
                            + fieldTokens.get(index + 1);

            if (joinedTokens.equals(queryTerm)) {
                return true;
            }
        }

        return false;
    }

    private static boolean matchesLetterPrefixedNumber(
            String fieldToken,
            String numericQuery) {

        if (!fieldToken.endsWith(numericQuery)) {
            return false;
        }

        String prefix = fieldToken.substring(
                0,
                fieldToken.length()
                        - numericQuery.length());

        return !prefix.isBlank()
                && prefix.chars()
                        .allMatch(Character::isLetter);
    }

    private static boolean matchesNumberWithUnit(
            String fieldToken,
            String numericQuery) {

        if (!fieldToken.startsWith(numericQuery)) {
            return false;
        }

        String suffix = fieldToken.substring(
                numericQuery.length());

        return !suffix.isBlank()
                && suffix.chars()
                        .allMatch(Character::isLetter);
    }

    private static boolean hasEquivalentWordForm(
            String firstWord,
            String secondWord) {

        Set<String> firstForms =
                createWordForms(firstWord);

        Set<String> secondForms =
                createWordForms(secondWord);

        return firstForms.stream()
                .anyMatch(secondForms::contains);
    }

    private static Set<String> createWordForms(
            String word) {

        LinkedHashSet<String> forms =
                new LinkedHashSet<>();

        if (word == null || word.isBlank()) {
            return forms;
        }

        forms.add(word);

        if (word.endsWith("ies")
                && word.length() > 4) {

            forms.add(
                    word.substring(
                            0,
                            word.length() - 3)
                            + "y");
        }

        if (word.endsWith("es")
                && word.length() > 4) {

            forms.add(
                    word.substring(
                            0,
                            word.length() - 2));
        }

        if (word.endsWith("s")
                && word.length() > 3
                && !word.endsWith("ss")) {

            forms.add(
                    word.substring(
                            0,
                            word.length() - 1));
        }

        return forms;
    }

    private static boolean isEligibleForFuzzyMatching(
            String term) {

        return term.length() >= 4
                && term.chars()
                        .allMatch(Character::isLetter);
    }

    private static int allowedEditDistance(String term) {
        return term.length() >= 8 ? 2 : 1;
    }

    private static boolean containsDigit(String value) {
        return value.chars()
                .anyMatch(Character::isDigit);
    }

    private static boolean containsOnlyDigits(
            String value) {

        return !value.isBlank()
                && value.chars()
                        .allMatch(Character::isDigit);
    }

    private static int damerauLevenshteinDistance(
            String first,
            String second) {

        int[][] distances = new int[
                first.length() + 1][
                second.length() + 1];

        for (int firstIndex = 0;
             firstIndex <= first.length();
             firstIndex++) {

            distances[firstIndex][0] = firstIndex;
        }

        for (int secondIndex = 0;
             secondIndex <= second.length();
             secondIndex++) {

            distances[0][secondIndex] = secondIndex;
        }

        for (int firstIndex = 1;
             firstIndex <= first.length();
             firstIndex++) {

            for (int secondIndex = 1;
                 secondIndex <= second.length();
                 secondIndex++) {

                int substitutionCost =
                        first.charAt(firstIndex - 1)
                                == second.charAt(
                                        secondIndex - 1)
                                ? 0
                                : 1;

                int deletion = distances[
                        firstIndex - 1][
                        secondIndex] + 1;

                int insertion = distances[
                        firstIndex][
                        secondIndex - 1] + 1;

                int substitution = distances[
                        firstIndex - 1][
                        secondIndex - 1]
                        + substitutionCost;

                distances[firstIndex][secondIndex] =
                        Math.min(
                                Math.min(
                                        deletion,
                                        insertion),
                                substitution);

                if (firstIndex > 1
                        && secondIndex > 1
                        && first.charAt(firstIndex - 1)
                                == second.charAt(
                                        secondIndex - 2)
                        && first.charAt(firstIndex - 2)
                                == second.charAt(
                                        secondIndex - 1)) {

                    distances[firstIndex][secondIndex] =
                            Math.min(
                                    distances[firstIndex][
                                            secondIndex],
                                    distances[
                                            firstIndex - 2][
                                            secondIndex - 2]
                                            + 1);
                }
            }
        }

        return distances[
                first.length()][
                second.length()];
    }

    private record NormalizedText(
            String spaced,
            String compact) {

        private boolean isBlank() {
            return spaced.isBlank();
        }

        private List<String> tokens() {
            if (isBlank()) {
                return List.of();
            }

            return List.of(spaced.split(" "));
        }

        private boolean equalsText(
                NormalizedText other) {

            return !other.isBlank()
                    && (spaced.equals(other.spaced())
                    || compact.equals(other.compact()));
        }

        private boolean startsWith(
                NormalizedText other) {

            return !other.isBlank()
                    && (spaced.startsWith(other.spaced())
                    || compact.startsWith(
                            other.compact()));
        }

        private boolean contains(
                NormalizedText other) {

            return !other.isBlank()
                    && (spaced.contains(other.spaced())
                    || compact.contains(
                            other.compact()));
        }
    }

    private record SearchDocument(
            NormalizedText name,
            NormalizedText brand,
            NormalizedText category,
            List<NormalizedText> tags,
            NormalizedText specifications,
            NormalizedText description) {

        private int scoreTerm(
                String term,
                boolean allowApproximateMatching) {

            int nameMatch = determineMatchQuality(
                    name,
                    term,
                    allowApproximateMatching);

            if (nameMatch == EXACT_MATCH) {
                return 120;
            }

            if (nameMatch == APPROXIMATE_MATCH) {
                return 85;
            }

            int brandMatch = determineMatchQuality(
                    brand,
                    term,
                    allowApproximateMatching);

            if (brandMatch == EXACT_MATCH) {
                return 90;
            }

            if (brandMatch == APPROXIMATE_MATCH) {
                return 65;
            }

            int categoryMatch = determineMatchQuality(
                    category,
                    term,
                    allowApproximateMatching);

            if (categoryMatch == EXACT_MATCH) {
                return 80;
            }

            if (categoryMatch == APPROXIMATE_MATCH) {
                return 55;
            }

            int tagMatch = bestTagMatch(
                    term,
                    allowApproximateMatching);

            if (tagMatch == EXACT_MATCH) {
                return 75;
            }

            if (tagMatch == APPROXIMATE_MATCH) {
                return 50;
            }

            int specificationMatch =
                    determineMatchQuality(
                            specifications,
                            term,
                            allowApproximateMatching);

            if (specificationMatch == EXACT_MATCH) {
                return 45;
            }

            if (specificationMatch
                    == APPROXIMATE_MATCH) {

                return 25;
            }

            int descriptionMatch =
                    determineMatchQuality(
                            description,
                            term,
                            allowApproximateMatching);

            if (descriptionMatch == EXACT_MATCH) {
                return 20;
            }

            if (descriptionMatch
                    == APPROXIMATE_MATCH) {

                return 10;
            }

            return NO_MATCH;
        }

        private int bestTagMatch(
                String term,
                boolean allowApproximateMatching) {

            int bestMatch = NO_MATCH;

            for (NormalizedText tag : tags) {
                int matchQuality =
                        determineMatchQuality(
                                tag,
                                term,
                                allowApproximateMatching);

                bestMatch = Math.max(
                        bestMatch,
                        matchQuality);
            }

            return bestMatch;
        }

        private int scoreCompleteQuery(
                NormalizedText query) {

            if (name.equalsText(query)) {
                return 1000;
            }

            if (name.startsWith(query)) {
                return 700;
            }

            if (name.contains(query)) {
                return 500;
            }

            if (brand.equalsText(query)) {
                return 350;
            }

            if (brand.contains(query)) {
                return 250;
            }

            if (category.equalsText(query)) {
                return 300;
            }

            if (category.contains(query)) {
                return 200;
            }

            if (tags.stream()
                    .anyMatch(tag ->
                            tag.equalsText(query))) {

                return 180;
            }

            if (tags.stream()
                    .anyMatch(tag ->
                            tag.contains(query))) {

                return 140;
            }

            if (specifications.contains(query)) {
                return 100;
            }

            if (description.contains(query)) {
                return 40;
            }

            return 0;
        }
    }
}