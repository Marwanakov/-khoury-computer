package com.khourycomputer.domain.model;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class PalestinianPhoneNumber {

    public static final String PALESTINE_COUNTRY_CODE = "+970";
    public static final String ISRAEL_COUNTRY_CODE = "+972";

    private static final Set<String> SUPPORTED_COUNTRY_CODES = Set.of(
            PALESTINE_COUNTRY_CODE,
            ISRAEL_COUNTRY_CODE);

    private static final Pattern LOCAL_MOBILE_NUMBER_PATTERN = Pattern.compile("^5\\d{8}$");

    private final String countryCode;
    private final String localNumber;

    private PalestinianPhoneNumber(
            String countryCode,
            String localNumber) {
        this.countryCode = countryCode;
        this.localNumber = localNumber;
    }

    public static PalestinianPhoneNumber fromParts(
            String countryCode,
            String localNumber) {
        String normalizedCountryCode = normalizeCountryCode(countryCode);

        String normalizedLocalNumber = normalizeLocalNumber(localNumber);

        return new PalestinianPhoneNumber(
                normalizedCountryCode,
                normalizedLocalNumber);
    }

    public static PalestinianPhoneNumber fromInternationalNumber(
            String internationalNumber) {
        if (internationalNumber == null
                || internationalNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number cannot be empty.");
        }

        String normalizedNumber = removeFormatting(internationalNumber);

        String countryCode = findCountryCode(normalizedNumber);

        String localNumber = normalizedNumber.substring(countryCode.length());

        return fromParts(countryCode, localNumber);
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getLocalNumber() {
        return localNumber;
    }

    public String getInternationalNumber() {
        return countryCode + localNumber;
    }

    private static String normalizeCountryCode(
            String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Country code is required.");
        }

        String normalizedCountryCode = countryCode.trim();

        if (!SUPPORTED_COUNTRY_CODES.contains(
                normalizedCountryCode)) {
            throw new IllegalArgumentException(
                    "Country code must be +970 or +972.");
        }

        return normalizedCountryCode;
    }

    private static String normalizeLocalNumber(
            String localNumber) {
        if (localNumber == null || localNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number is required.");
        }

        String normalizedLocalNumber = removeFormatting(localNumber);

        // Customers commonly enter the local trunk prefix.
        // 0599123456 becomes 599123456 before storage.
        if (normalizedLocalNumber.startsWith("0")) {
            normalizedLocalNumber = normalizedLocalNumber.substring(1);
        }

        if (!LOCAL_MOBILE_NUMBER_PATTERN
                .matcher(normalizedLocalNumber)
                .matches()) {
            throw new IllegalArgumentException(
                    "Enter a valid mobile number, such as 059 123 4567.");
        }

        return normalizedLocalNumber;
    }

    private static String findCountryCode(
            String internationalNumber) {
        return SUPPORTED_COUNTRY_CODES.stream()
                .filter(internationalNumber::startsWith)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Phone number must begin with +970 or +972."));
    }

    private static String removeFormatting(String value) {
        String normalizedValue = value
                .trim()
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");

        if (!normalizedValue.matches("\\+?\\d+")) {
            throw new IllegalArgumentException(
                    "Phone number may contain only numbers, "
                            + "spaces, parentheses, and hyphens.");
        }

        return normalizedValue;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PalestinianPhoneNumber other)) {
            return false;
        }

        return countryCode.equals(other.countryCode)
                && localNumber.equals(other.localNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, localNumber);
    }

    @Override
    public String toString() {
        return getInternationalNumber();
    }
}