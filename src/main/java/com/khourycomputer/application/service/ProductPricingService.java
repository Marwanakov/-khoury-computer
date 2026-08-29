package com.khourycomputer.application.service;

import com.khourycomputer.application.repository.ProductDealRepository;
import com.khourycomputer.domain.model.Product;
import com.khourycomputer.domain.model.ProductDeal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ProductPricingService {

    private final ProductDealRepository productDealRepository;

    public ProductPricingService(
            ProductDealRepository productDealRepository
    ) {
        this.productDealRepository =
                productDealRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal getEffectiveUnitPrice(
            Product product
    ) {
        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null."
            );
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        return productDealRepository
                .findByProductId(product.getId())
                .stream()
                .filter(deal ->
                        deal.isActiveAt(currentTime)
                )
                .findFirst()
                .map(ProductDeal::getDealPrice)
                .orElse(product.getPrice());
    }

    @Transactional(readOnly = true)
    public void validateRegularPriceForExistingDeals(
            Long productId,
            BigDecimal newRegularPrice
    ) {
        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product id cannot be null."
            );
        }

        if (newRegularPrice == null
                || newRegularPrice.compareTo(
                        BigDecimal.ZERO
                ) < 0) {

            throw new IllegalArgumentException(
                    "Product price cannot be negative."
            );
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        productDealRepository
                .findByProductId(productId)
                .stream()
                .filter(deal ->
                        deal.getEndsAt()
                                .isAfter(currentTime)
                )
                .filter(deal ->
                        deal.getDealPrice()
                                .compareTo(newRegularPrice)
                                >= 0
                )
                .findFirst()
                .ifPresent(deal -> {
                    throw new IllegalArgumentException(
                            createInvalidRegularPriceMessage(
                                    deal
                            )
                    );
                });
    }

    private String createInvalidRegularPriceMessage(
            ProductDeal deal
    ) {
        return "The product price must remain greater than "
                + "the active or scheduled deal price of ₪"
                + formatPrice(deal.getDealPrice())
                + ". Edit or end the deal before lowering "
                + "the product price.";
    }

    private String formatPrice(
            BigDecimal price
    ) {
        return price.stripTrailingZeros()
                .toPlainString();
    }
}