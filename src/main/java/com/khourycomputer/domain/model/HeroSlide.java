package com.khourycomputer.domain.model;

import com.khourycomputer.domain.enums.HeroLinkType;

import java.util.Objects;

public class HeroSlide {

    private Long id;
    private String desktopImageUrl;
    private String mobileImageUrl;
    private String altText;
    private HeroLinkType linkType;
    private Long linkedProductId;
    private Long linkedCategoryId;
    private int displayOrder;
    private boolean active;

    public HeroSlide(
            Long id,
            String desktopImageUrl,
            String mobileImageUrl,
            String altText,
            HeroLinkType linkType,
            Long linkedProductId,
            Long linkedCategoryId,
            int displayOrder,
            boolean active
    ) {
        setId(id);
        setDesktopImageUrl(desktopImageUrl);
        setMobileImageUrl(mobileImageUrl);
        setAltText(altText);

        setLink(
                linkType,
                linkedProductId,
                linkedCategoryId
        );

        setDisplayOrder(displayOrder);
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getDesktopImageUrl() {
        return desktopImageUrl;
    }

    public String getMobileImageUrl() {
        return mobileImageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public HeroLinkType getLinkType() {
        return linkType;
    }

    public Long getLinkedProductId() {
        return linkedProductId;
    }

    public Long getLinkedCategoryId() {
        return linkedCategoryId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void updatePresentation(
            String desktopImageUrl,
            String mobileImageUrl,
            String altText,
            HeroLinkType linkType,
            Long linkedProductId,
            Long linkedCategoryId
    ) {
        setDesktopImageUrl(desktopImageUrl);
        setMobileImageUrl(mobileImageUrl);
        setAltText(altText);

        setLink(
                linkType,
                linkedProductId,
                linkedCategoryId
        );
    }

    public void changeDisplayOrder(int displayOrder) {
        setDisplayOrder(displayOrder);
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setDesktopImageUrl(
            String desktopImageUrl
    ) {
        if (desktopImageUrl == null
                || desktopImageUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Desktop hero image is required."
            );
        }

        this.desktopImageUrl =
                desktopImageUrl.trim();
    }

    private void setMobileImageUrl(
            String mobileImageUrl
    ) {
        if (mobileImageUrl == null
                || mobileImageUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Mobile hero image is required."
            );
        }

        this.mobileImageUrl =
                mobileImageUrl.trim();
    }

    private void setAltText(String altText) {
        if (altText == null || altText.isBlank()) {
            throw new IllegalArgumentException(
                    "Hero image description is required."
            );
        }

        String normalizedAltText = altText.trim();

        if (normalizedAltText.length() > 160) {
            throw new IllegalArgumentException(
                    "Hero image description cannot exceed 160 characters."
            );
        }

        this.altText = normalizedAltText;
    }

    private void setLink(
            HeroLinkType linkType,
            Long linkedProductId,
            Long linkedCategoryId
    ) {
        this.linkType = Objects.requireNonNullElse(
                linkType,
                HeroLinkType.NONE
        );

        this.linkedProductId =
                this.linkType == HeroLinkType.PRODUCT
                        ? linkedProductId
                        : null;

        this.linkedCategoryId =
                this.linkType == HeroLinkType.CATEGORY
                        ? linkedCategoryId
                        : null;
    }

    private void setDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException(
                    "Hero display order cannot be negative."
            );
        }

        this.displayOrder = displayOrder;
    }
}