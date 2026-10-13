package com.khourycomputer.web.viewmodel.admin;

import com.khourycomputer.domain.enums.HeroLinkType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class AdminHeroSlideForm {

    private MultipartFile desktopImage;
    private MultipartFile mobileImage;

    @NotBlank(message = "Image description is required.")
    @Size(max = 160, message = "Image description cannot exceed 160 characters.")
    private String altText;

    @NotNull(message = "Link destination is required.")
    private HeroLinkType linkType = HeroLinkType.NONE;

    private Long linkedProductId;
    private Long linkedCategoryId;

    @NotNull(message = "Display order is required.")
    @Min(value = 0, message = "Display order cannot be negative.")
    private Integer displayOrder = 0;

    private boolean active = true;

    public MultipartFile getDesktopImage() {
        return desktopImage;
    }

    public void setDesktopImage(
            MultipartFile desktopImage) {
        this.desktopImage = desktopImage;
    }

    public MultipartFile getMobileImage() {
        return mobileImage;
    }

    public void setMobileImage(
            MultipartFile mobileImage) {
        this.mobileImage = mobileImage;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public HeroLinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(
            HeroLinkType linkType) {
        this.linkType = linkType;
    }

    public Long getLinkedCategoryId() {
        return linkedCategoryId;
    }

    public void setLinkedCategoryId(
            Long linkedCategoryId) {
        this.linkedCategoryId = linkedCategoryId;
    }

    public Long getLinkedProductId() {
        return linkedProductId;
    }

    public void setLinkedProductId(
            Long linkedProductId) {
        this.linkedProductId = linkedProductId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(
            Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}