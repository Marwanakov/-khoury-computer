package com.khourycomputer.web.viewmodel.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminProductDealForm {

    @NotNull(message = "Product is required.")
    private Long productId;

    @NotNull(message = "Deal price is required.")
    @DecimalMin(
            value = "0.01",
            message = "Deal price must be greater than zero."
    )
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Deal price must contain at most 8 whole digits and 2 decimal places."
    )
    private BigDecimal dealPrice;

    @NotNull(message = "Start date and time are required.")
    @DateTimeFormat(
            pattern = "yyyy-MM-dd'T'HH:mm"
    )
    private LocalDateTime startsAt;

    @NotNull(message = "End date and time are required.")
    @DateTimeFormat(
            pattern = "yyyy-MM-dd'T'HH:mm"
    )
    private LocalDateTime endsAt;

    private boolean featured;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getDealPrice() {
        return dealPrice;
    }

    public void setDealPrice(BigDecimal dealPrice) {
        this.dealPrice = dealPrice;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}