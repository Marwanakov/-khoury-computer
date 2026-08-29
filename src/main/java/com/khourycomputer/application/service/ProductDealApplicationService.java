package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.deal.CreateProductDealRequest;
import com.khourycomputer.application.dto.deal.ProductDealResponse;
import com.khourycomputer.application.dto.deal.UpdateProductDealRequest;
import com.khourycomputer.application.repository.ProductDealRepository;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.domain.enums.DealStatus;
import com.khourycomputer.domain.exception.ProductDealNotFoundException;
import com.khourycomputer.domain.exception.ProductNotFoundException;
import com.khourycomputer.domain.model.Product;
import com.khourycomputer.domain.model.ProductDeal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ProductDealApplicationService {

        private final ProductDealRepository productDealRepository;
        private final ProductRepository productRepository;

        public ProductDealApplicationService(
                        ProductDealRepository productDealRepository,
                        ProductRepository productRepository) {
                this.productDealRepository = productDealRepository;
                this.productRepository = productRepository;
        }

        @Transactional
        public ProductDealResponse createDeal(
                        CreateProductDealRequest request) {
                validateCreateRequest(request);

                Product product = getProduct(request.productId());

                validateDealPrice(
                                request.dealPrice(),
                                product.getPrice());

                validateNoOverlap(
                                request.productId(),
                                request.startsAt(),
                                request.endsAt());

                ProductDeal productDeal = new ProductDeal(
                                null,
                                request.productId(),
                                request.dealPrice(),
                                request.startsAt(),
                                request.endsAt(),
                                request.featured(),
                                LocalDateTime.now());

                ProductDeal savedDeal = productDealRepository.save(productDeal);

                return toResponse(
                                savedDeal,
                                product,
                                LocalDateTime.now());
        }

        @Transactional
        public ProductDealResponse updateDeal(
                        Long dealId,
                        UpdateProductDealRequest request) {
                validateDealId(dealId);
                validateUpdateRequest(request);

                ProductDeal existingDeal = findDeal(dealId);
                Product product = getProduct(
                                existingDeal.getProductId());

                validateDealPrice(
                                request.dealPrice(),
                                product.getPrice());

                validateNoOverlapExcludingDeal(
                                existingDeal.getProductId(),
                                request.startsAt(),
                                request.endsAt(),
                                dealId);

                existingDeal.changeDealPrice(
                                request.dealPrice());

                existingDeal.changeSchedule(
                                request.startsAt(),
                                request.endsAt());

                existingDeal.changeFeatured(
                                request.featured());

                ProductDeal savedDeal = productDealRepository.save(existingDeal);

                return toResponse(
                                savedDeal,
                                product,
                                LocalDateTime.now());
        }

        @Transactional(readOnly = true)
        public ProductDealResponse getDealById(Long dealId) {
                validateDealId(dealId);

                ProductDeal deal = findDeal(dealId);
                Product product = getProduct(deal.getProductId());

                return toResponse(
                                deal,
                                product,
                                LocalDateTime.now());
        }

        @Transactional(readOnly = true)
        public List<ProductDealResponse> listDeals() {
                LocalDateTime currentTime = LocalDateTime.now();

                return productDealRepository.findAll()
                                .stream()
                                .sorted(
                                                Comparator.comparing(
                                                                ProductDeal::getCreatedAt).reversed())
                                .map(deal -> toResponse(
                                                deal,
                                                getProduct(deal.getProductId()),
                                                currentTime))
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ProductDealResponse> listActiveDeals() {
                LocalDateTime currentTime = LocalDateTime.now();

                return productDealRepository
                                .findActiveAt(currentTime)
                                .stream()
                                .map(deal -> toResponse(
                                                deal,
                                                getProduct(deal.getProductId()),
                                                currentTime))
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ProductDealResponse> listFeaturedActiveDeals(int maximumResults) {

                if (maximumResults <= 0) {
                        throw new IllegalArgumentException(
                                        "Maximum deal results must be greater than zero.");
                }

                LocalDateTime currentTime = LocalDateTime.now();

                return productDealRepository
                                .findFeaturedActiveAt(currentTime)
                                .stream()
                                .limit(maximumResults)
                                .map(deal -> toResponse(
                                                deal,
                                                getProduct(deal.getProductId()),
                                                currentTime))
                                .toList();
        }

        @Transactional
        public ProductDealResponse endDeal(Long dealId) {
                validateDealId(dealId);

                ProductDeal deal = findDeal(dealId);
                LocalDateTime currentTime = LocalDateTime.now();

                if (deal.getStatus(currentTime) != DealStatus.ACTIVE) {
                        throw new IllegalStateException(
                                        "Only active deals can be ended.");
                }

                deal.changeSchedule(
                                deal.getStartsAt(),
                                currentTime);

                ProductDeal savedDeal = productDealRepository.save(deal);

                Product product = getProduct(
                                savedDeal.getProductId());

                return toResponse(
                                savedDeal,
                                product,
                                currentTime);
        }

        @Transactional
        public void deleteDeal(Long dealId) {
                validateDealId(dealId);

                if (productDealRepository
                                .findById(dealId)
                                .isEmpty()) {
                        throw new ProductDealNotFoundException(
                                        dealId);
                }

                productDealRepository.deleteById(dealId);
        }

        @Transactional(readOnly = true)
        public ProductDealResponse findActiveDealByProductId(
                        Long productId) {
                if (productId == null) {
                        throw new IllegalArgumentException(
                                        "Product id cannot be null.");
                }

                LocalDateTime currentTime = LocalDateTime.now();

                return productDealRepository
                                .findActiveAt(currentTime)
                                .stream()
                                .filter(deal -> deal.getProductId()
                                                .equals(productId))
                                .findFirst()
                                .map(deal -> toResponse(
                                                deal,
                                                getProduct(productId),
                                                currentTime))
                                .orElse(null);
        }

        private ProductDeal findDeal(Long dealId) {
                return productDealRepository.findById(dealId)
                                .orElseThrow(() -> new ProductDealNotFoundException(
                                                dealId));
        }

        private Product getProduct(Long productId) {
                return productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotFoundException(
                                                productId));
        }

        private void validateCreateRequest(
                        CreateProductDealRequest request) {
                if (request == null) {
                        throw new IllegalArgumentException(
                                        "Create deal request cannot be null.");
                }

                if (request.productId() == null) {
                        throw new IllegalArgumentException(
                                        "A product must be selected.");
                }

                validateRequestValues(
                                request.dealPrice(),
                                request.startsAt(),
                                request.endsAt());
        }

        private void validateUpdateRequest(
                        UpdateProductDealRequest request) {
                if (request == null) {
                        throw new IllegalArgumentException(
                                        "Update deal request cannot be null.");
                }

                validateRequestValues(
                                request.dealPrice(),
                                request.startsAt(),
                                request.endsAt());
        }

        private void validateRequestValues(
                        BigDecimal dealPrice,
                        LocalDateTime startsAt,
                        LocalDateTime endsAt) {
                if (dealPrice == null
                                || dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException(
                                        "Deal price must be greater than zero.");
                }

                if (startsAt == null || endsAt == null) {
                        throw new IllegalArgumentException(
                                        "Deal start and end times are required.");
                }

                if (!endsAt.isAfter(startsAt)) {
                        throw new IllegalArgumentException(
                                        "Deal end time must be after its start time.");
                }
        }

        private void validateDealPrice(
                        BigDecimal dealPrice,
                        BigDecimal regularPrice) {
                if (dealPrice.compareTo(regularPrice) >= 0) {
                        throw new IllegalArgumentException(
                                        "Deal price must be lower than "
                                                        + "the regular product price.");
                }
        }

        private void validateNoOverlap(
                        Long productId,
                        LocalDateTime startsAt,
                        LocalDateTime endsAt) {
                boolean overlapping = productDealRepository.existsOverlapping(
                                productId,
                                startsAt,
                                endsAt);

                if (overlapping) {
                        throw new IllegalArgumentException(
                                        "This product already has a deal "
                                                        + "during the selected period.");
                }
        }

        private void validateNoOverlapExcludingDeal(
                        Long productId,
                        LocalDateTime startsAt,
                        LocalDateTime endsAt,
                        Long excludedDealId) {
                boolean overlapping = productDealRepository
                                .existsOverlappingExcludingId(
                                                productId,
                                                startsAt,
                                                endsAt,
                                                excludedDealId);

                if (overlapping) {
                        throw new IllegalArgumentException(
                                        "This product already has a deal "
                                                        + "during the selected period.");
                }
        }

        private void validateDealId(Long dealId) {
                if (dealId == null) {
                        throw new IllegalArgumentException(
                                        "Deal id cannot be null.");
                }
        }

        private ProductDealResponse toResponse(
                        ProductDeal deal,
                        Product product,
                        LocalDateTime currentTime) {
                BigDecimal savingsAmount = product.getPrice()
                                .subtract(deal.getDealPrice());

                int discountPercentage = calculateDiscountPercentage(
                                savingsAmount,
                                product.getPrice());

                return new ProductDealResponse(
                                deal.getId(),
                                deal.getProductId(),
                                product.getName(),
                                product.getImageUrl(),
                                product.getPrice(),
                                deal.getDealPrice(),
                                savingsAmount,
                                discountPercentage,
                                deal.getStartsAt(),
                                deal.getEndsAt(),
                                deal.isFeatured(),
                                deal.getStatus(currentTime),
                                deal.getCreatedAt());
        }

        private int calculateDiscountPercentage(
                        BigDecimal savingsAmount,
                        BigDecimal regularPrice) {
                if (regularPrice.compareTo(BigDecimal.ZERO) <= 0) {
                        return 0;
                }

                return savingsAmount
                                .multiply(BigDecimal.valueOf(100))
                                .divide(
                                                regularPrice,
                                                0,
                                                RoundingMode.HALF_UP)
                                .intValue();
        }
}