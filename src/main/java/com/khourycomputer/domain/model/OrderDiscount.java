package com.khourycomputer.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OrderDiscount {

    private final BigDecimal amount;
    private final LocalDateTime appliedAt;

    public OrderDiscount(
            BigDecimal amount,
            LocalDateTime appliedAt) {

        validateAmount(amount);
        validateAppliedAt(appliedAt);

        this.amount = amount;
        this.appliedAt = appliedAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public BigDecimal applyTo(BigDecimal subtotal) {
        if (subtotal == null) {
            throw new IllegalArgumentException(
                    "Order subtotal cannot be null.");
        }

        if (amount.compareTo(subtotal) > 0) {
            throw new IllegalStateException(
                    "Order discount cannot exceed "
                            + "the order subtotal.");
        }

        return subtotal.subtract(amount);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null
                || amount.compareTo(
                        BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Order discount must be "
                            + "greater than zero.");
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "Order discount cannot have "
                            + "more than two decimal places.");
        }
    }

    private void validateAppliedAt(
            LocalDateTime appliedAt) {

        if (appliedAt == null) {
            throw new IllegalArgumentException(
                    "Order discount must have "
                            + "an applied time.");
        }
    }
}