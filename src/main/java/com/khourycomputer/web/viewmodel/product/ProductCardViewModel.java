package com.khourycomputer.web.viewmodel.product;

import com.khourycomputer.application.dto.deal.ProductDealResponse;
import com.khourycomputer.application.dto.product.ProductResponse;

import java.math.BigDecimal;

public record ProductCardViewModel(
        ProductResponse product,
        ProductDealResponse activeDeal
) {

    public boolean hasActiveDeal() {
        return activeDeal != null;
    }

    public BigDecimal displayPrice() {
        return hasActiveDeal()
                ? activeDeal.dealPrice()
                : product.price();
    }
}