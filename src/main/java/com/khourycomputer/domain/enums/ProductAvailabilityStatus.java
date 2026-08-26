package com.khourycomputer.domain.enums;

public enum ProductAvailabilityStatus {
    AVAILABLE("Available"),
    LOW_STOCK("Low stock"),
    SOLD_OUT("Sold out");

    private final String displayName;

    ProductAvailabilityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}