package com.khourycomputer.application.exception;

import java.math.BigDecimal;

public class CartPriceChangedException
        extends IllegalArgumentException {

    public CartPriceChangedException(
            String productName,
            BigDecimal previousPrice,
            BigDecimal currentPrice
    ) {
        super(
                "The price of \""
                        + productName
                        + "\" changed from ₪"
                        + formatPrice(previousPrice)
                        + " to ₪"
                        + formatPrice(currentPrice)
                        + ". Your cart has been updated. "
                        + "Please review the new total before submitting again."
        );
    }

    private static String formatPrice(
            BigDecimal price
    ) {
        return price.stripTrailingZeros()
                .toPlainString();
    }
}