package com.khourycomputer.domain.model;

import com.khourycomputer.domain.enums.DealStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ProductDeal {

    private Long id;
    private Long productId;
    private BigDecimal dealPrice;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private boolean featured;
    private LocalDateTime createdAt;

    public ProductDeal(
            Long id,
            Long productId,
            BigDecimal dealPrice,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            boolean featured,
            LocalDateTime createdAt
    ) {
        setId(id);
        setProductId(productId);
        setDealPrice(dealPrice);
        setSchedule(startsAt, endsAt);
        setFeatured(featured);
        setCreatedAt(createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public BigDecimal getDealPrice() {
        return dealPrice;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public boolean isFeatured() {
        return featured;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public DealStatus getStatus(LocalDateTime currentTime) {
        LocalDateTime checkedTime = Objects.requireNonNull(
                currentTime,
                "Current time cannot be null."
        );

        if (checkedTime.isBefore(startsAt)) {
            return DealStatus.SCHEDULED;
        }

        if (!checkedTime.isBefore(endsAt)) {
            return DealStatus.EXPIRED;
        }

        return DealStatus.ACTIVE;
    }

    public boolean isActiveAt(LocalDateTime currentTime) {
        return getStatus(currentTime) == DealStatus.ACTIVE;
    }

    public void changeDealPrice(BigDecimal dealPrice) {
        setDealPrice(dealPrice);
    }

    public void changeSchedule(
            LocalDateTime startsAt,
            LocalDateTime endsAt
    ) {
        setSchedule(startsAt, endsAt);
    }

    public void changeFeatured(boolean featured) {
        setFeatured(featured);
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException(
                    "Deal must belong to a product."
            );
        }

        this.productId = productId;
    }

    private void setDealPrice(BigDecimal dealPrice) {
        if (dealPrice == null
                || dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Deal price must be greater than zero."
            );
        }

        this.dealPrice = dealPrice;
    }

    private void setSchedule(
            LocalDateTime startsAt,
            LocalDateTime endsAt
    ) {
        if (startsAt == null || endsAt == null) {
            throw new IllegalArgumentException(
                    "Deal start and end times are required."
            );
        }

        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException(
                    "Deal end time must be after its start time."
            );
        }

        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    private void setFeatured(boolean featured) {
        this.featured = featured;
    }

    private void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNullElseGet(
                createdAt,
                LocalDateTime::now
        );
    }
}