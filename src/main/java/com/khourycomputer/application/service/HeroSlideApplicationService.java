package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.hero.HeroSlideResponse;
import com.khourycomputer.application.dto.hero.SaveHeroSlideRequest;
import com.khourycomputer.application.port.storage.ImageStorage;
import com.khourycomputer.application.port.storage.ImageStorageFolder;
import com.khourycomputer.application.port.storage.ImageUpload;
import com.khourycomputer.application.repository.HeroSlideRepository;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.domain.enums.HeroLinkType;
import com.khourycomputer.domain.model.HeroSlide;
import com.khourycomputer.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.khourycomputer.application.repository.CategoryRepository;
import com.khourycomputer.domain.model.Category;

import java.util.List;

@Service
public class HeroSlideApplicationService {

        private static final int MAX_ACTIVE_SLIDES = 5;

        private final HeroSlideRepository heroSlideRepository;
        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;
        private final ImageStorage imageStorage;

        public HeroSlideApplicationService(
                        HeroSlideRepository heroSlideRepository,
                        ProductRepository productRepository,
                        CategoryRepository categoryRepository,
                        ImageStorage imageStorage) {
                this.heroSlideRepository = heroSlideRepository;
                this.productRepository = productRepository;
                this.categoryRepository = categoryRepository;
                this.imageStorage = imageStorage;
        }

        @Transactional(readOnly = true)
        public List<HeroSlideResponse> listAllSlides() {
                return heroSlideRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<HeroSlideResponse> listActiveSlides() {
                return heroSlideRepository.findActive()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public HeroSlideResponse getSlideById(Long slideId) {
                return toResponse(findSlide(slideId));
        }

        @Transactional
        public HeroSlideResponse createSlide(
                        SaveHeroSlideRequest request,
                        ImageUpload desktopImage,
                        ImageUpload mobileImage) {
                validateRequest(request);
                validateRequiredImage(
                                desktopImage,
                                "Desktop hero image is required.");
                validateRequiredImage(
                                mobileImage,
                                "Mobile hero image is required.");
                validateLink(
                                request.linkType(),
                                request.linkedProductId(),
                                request.linkedCategoryId());

                if (request.active()) {
                        validateActiveSlideLimit(null);
                }

                String desktopImageUrl = imageStorage.store(
                                desktopImage,
                                ImageStorageFolder.HERO_SLIDES);

                registerImageDeletionOnRollback(desktopImageUrl);

                String mobileImageUrl = imageStorage.store(
                                mobileImage,
                                ImageStorageFolder.HERO_SLIDES);

                registerImageDeletionOnRollback(mobileImageUrl);

                HeroSlide heroSlide = new HeroSlide(
                                null,
                                desktopImageUrl,
                                mobileImageUrl,
                                request.altText(),
                                request.linkType(),
                                normalizeLinkedProductId(request),
                                normalizeLinkedCategoryId(request),
                                request.displayOrder(),
                                request.active());

                return toResponse(
                                heroSlideRepository.save(heroSlide));
        }

        @Transactional
        public HeroSlideResponse updateSlide(
                        Long slideId,
                        SaveHeroSlideRequest request,
                        ImageUpload newDesktopImage,
                        ImageUpload newMobileImage) {
                validateRequest(request);
                validateLink(
                                request.linkType(),
                                request.linkedProductId(),
                                request.linkedCategoryId());

                HeroSlide heroSlide = findSlide(slideId);

                if (request.active() && !heroSlide.isActive()) {
                        validateActiveSlideLimit(slideId);
                }

                String oldDesktopImageUrl = heroSlide.getDesktopImageUrl();

                String oldMobileImageUrl = heroSlide.getMobileImageUrl();

                String resultingDesktopImageUrl = storeReplacementIfPresent(
                                newDesktopImage,
                                oldDesktopImageUrl);

                String resultingMobileImageUrl = storeReplacementIfPresent(
                                newMobileImage,
                                oldMobileImageUrl);

                heroSlide.updatePresentation(
                                resultingDesktopImageUrl,
                                resultingMobileImageUrl,
                                request.altText(),
                                request.linkType(),
                                normalizeLinkedProductId(request),
                                normalizeLinkedCategoryId(request));

                heroSlide.changeDisplayOrder(
                                request.displayOrder());

                if (request.active()) {
                        heroSlide.activate();
                } else {
                        heroSlide.deactivate();
                }

                HeroSlide savedSlide = heroSlideRepository.save(heroSlide);

                if (!oldDesktopImageUrl.equals(
                                resultingDesktopImageUrl)) {
                        registerImageDeletionAfterCommit(
                                        oldDesktopImageUrl);
                }

                if (!oldMobileImageUrl.equals(
                                resultingMobileImageUrl)) {
                        registerImageDeletionAfterCommit(
                                        oldMobileImageUrl);
                }

                return toResponse(savedSlide);
        }

        @Transactional
        public void deleteSlide(Long slideId) {
                HeroSlide heroSlide = findSlide(slideId);

                heroSlideRepository.deleteById(slideId);

                registerImageDeletionAfterCommit(
                                heroSlide.getDesktopImageUrl());

                registerImageDeletionAfterCommit(
                                heroSlide.getMobileImageUrl());
        }

        private HeroSlide findSlide(Long slideId) {
                if (slideId == null) {
                        throw new IllegalArgumentException(
                                        "Hero slide id is required.");
                }

                return heroSlideRepository.findById(slideId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Hero slide not found."));
        }

        private void validateRequest(
                        SaveHeroSlideRequest request) {
                if (request == null) {
                        throw new IllegalArgumentException(
                                        "Hero slide information is required.");
                }

                if (request.altText() == null
                                || request.altText().isBlank()) {
                        throw new IllegalArgumentException(
                                        "Hero image description is required.");
                }

                if (request.altText().trim().length() > 160) {
                        throw new IllegalArgumentException(
                                        "Hero image description cannot exceed 160 characters.");
                }

                if (request.displayOrder() < 0) {
                        throw new IllegalArgumentException(
                                        "Hero display order cannot be negative.");
                }
        }

        private void validateRequiredImage(
                        ImageUpload image,
                        String message) {
                if (image == null || !image.isPresent()) {
                        throw new IllegalArgumentException(message);
                }
        }

        private void validateLink(
                        HeroLinkType linkType,
                        Long linkedProductId,
                        Long linkedCategoryId) {
                HeroLinkType normalizedLinkType = linkType == null
                                ? HeroLinkType.NONE
                                : linkType;

                if (normalizedLinkType == HeroLinkType.PRODUCT) {
                        if (linkedProductId == null) {
                                throw new IllegalArgumentException(
                                                "Select a product for this hero slide.");
                        }

                        if (!productRepository.existsById(
                                        linkedProductId)) {
                                throw new IllegalArgumentException(
                                                "The selected product does not exist.");
                        }
                }

                if (normalizedLinkType == HeroLinkType.CATEGORY) {
                        if (linkedCategoryId == null) {
                                throw new IllegalArgumentException(
                                                "Select a category for this hero slide.");
                        }

                        if (!categoryRepository.existsById(
                                        linkedCategoryId)) {
                                throw new IllegalArgumentException(
                                                "The selected category does not exist.");
                        }
                }
        }

        private Long normalizeLinkedProductId(
                        SaveHeroSlideRequest request) {
                if (request.linkType() == HeroLinkType.PRODUCT) {
                        return request.linkedProductId();
                }

                return null;
        }

        private void validateActiveSlideLimit(
                        Long excludedSlideId) {
                long activeSlideCount = heroSlideRepository.findActive()
                                .stream()
                                .filter(slide -> excludedSlideId == null
                                                || !slide.getId()
                                                                .equals(excludedSlideId))
                                .count();

                if (activeSlideCount >= MAX_ACTIVE_SLIDES) {
                        throw new IllegalStateException(
                                        "A maximum of five hero slides can be active.");
                }
        }

        private String storeReplacementIfPresent(
                        ImageUpload newImage,
                        String existingImageUrl) {
                if (newImage == null || !newImage.isPresent()) {
                        return existingImageUrl;
                }

                String newImageUrl = imageStorage.store(
                                newImage,
                                ImageStorageFolder.HERO_SLIDES);

                registerImageDeletionOnRollback(newImageUrl);

                return newImageUrl;
        }

        private void registerImageDeletionOnRollback(
                        String imageUrl) {
                if (imageUrl == null || imageUrl.isBlank()) {
                        return;
                }

                if (!TransactionSynchronizationManager
                                .isSynchronizationActive()) {
                        return;
                }

                TransactionSynchronizationManager
                                .registerSynchronization(
                                                new TransactionSynchronization() {
                                                        @Override
                                                        public void afterCompletion(
                                                                        int status) {
                                                                if (status == STATUS_ROLLED_BACK) {
                                                                        imageStorage.delete(
                                                                                        imageUrl);
                                                                }
                                                        }
                                                });
        }

        private void registerImageDeletionAfterCommit(
                        String imageUrl) {
                if (imageUrl == null || imageUrl.isBlank()) {
                        return;
                }

                if (!TransactionSynchronizationManager
                                .isSynchronizationActive()) {
                        imageStorage.delete(imageUrl);
                        return;
                }

                TransactionSynchronizationManager
                                .registerSynchronization(
                                                new TransactionSynchronization() {
                                                        @Override
                                                        public void afterCommit() {
                                                                imageStorage.delete(
                                                                                imageUrl);
                                                        }
                                                });
        }

        private HeroSlideResponse toResponse(
                        HeroSlide heroSlide) {
                String linkedProductName = findLinkedProductName(heroSlide);

                String linkedCategoryName = findLinkedCategoryName(heroSlide);

                return new HeroSlideResponse(
                                heroSlide.getId(),
                                heroSlide.getDesktopImageUrl(),
                                heroSlide.getMobileImageUrl(),
                                heroSlide.getAltText(),
                                heroSlide.getLinkType(),
                                heroSlide.getLinkedProductId(),
                                linkedProductName,
                                heroSlide.getLinkedCategoryId(),
                                linkedCategoryName,
                                resolveTargetUrl(heroSlide),
                                heroSlide.getDisplayOrder(),
                                heroSlide.isActive());
        }

        private String findLinkedProductName(
                        HeroSlide heroSlide) {
                if (heroSlide.getLinkType() != HeroLinkType.PRODUCT
                                || heroSlide.getLinkedProductId() == null) {
                        return "";
                }

                return productRepository
                                .findById(heroSlide.getLinkedProductId())
                                .map(Product::getName)
                                .orElse("");
        }

        private String resolveTargetUrl(
                        HeroSlide heroSlide) {
                return switch (heroSlide.getLinkType()) {
                        case NONE -> "";

                        case PRODUCT ->
                                resolveProductTarget(heroSlide);

                        case CATEGORY ->
                                resolveCategoryTarget(heroSlide);

                        case PRODUCTS -> "/products";
                        case DEALS -> "/deals";
                        case NEW_ARRIVALS -> "/new-arrivals";
                        case BEST_SELLERS -> "/best-sellers";
                        case CONTACT -> "/contact";
                };
        }

        private String resolveProductTarget(
                        HeroSlide heroSlide) {
                if (heroSlide.getLinkedProductId() == null) {
                        return "";
                }

                boolean productExists = productRepository.existsById(
                                heroSlide.getLinkedProductId());

                if (!productExists) {
                        return "";
                }

                return "/products/"
                                + heroSlide.getLinkedProductId();
        }

        private Long normalizeLinkedCategoryId(
                        SaveHeroSlideRequest request) {
                if (request.linkType() == HeroLinkType.CATEGORY) {
                        return request.linkedCategoryId();
                }

                return null;
        }

        private String findLinkedCategoryName(
                        HeroSlide heroSlide) {
                if (heroSlide.getLinkType() != HeroLinkType.CATEGORY
                                || heroSlide.getLinkedCategoryId() == null) {
                        return "";
                }

                return categoryRepository
                                .findById(heroSlide.getLinkedCategoryId())
                                .map(Category::getName)
                                .orElse("");
        }

        private String resolveCategoryTarget(
                        HeroSlide heroSlide) {
                if (heroSlide.getLinkedCategoryId() == null) {
                        return "";
                }

                boolean categoryExists = categoryRepository.existsById(
                                heroSlide.getLinkedCategoryId());

                if (!categoryExists) {
                        return "";
                }

                return "/products?categoryId="
                                + heroSlide.getLinkedCategoryId();
        }
}