"use strict";

class AdminProductImageUpload {
  constructor() {
    this.input = document.getElementById(
      "additionalImages",
    );

    this.primaryIndexInput = document.getElementById(
      "primaryImageIndex",
    );

    this.previewContainer = document.getElementById(
      "selectedProductImages",
    );

    this.previewGrid = document.getElementById(
      "selectedProductImageGrid",
    );

    this.previewCount = document.getElementById(
      "selectedProductImageCount",
    );

    this.remainingSlotsElement =
      document.getElementById(
        "remainingProductImageSlots",
      );

    this.savedImageCountElement =
      document.getElementById(
        "savedProductImageCount",
      );

    this.savedGallery = document.querySelector(
      ".admin-product-gallery-grid",
    );

    this.productForm = this.input?.closest("form");

    this.initialRemainingSlots = Number(
      this.input?.dataset.remainingSlots || 0,
    );

    this.hasSavedImages =
      this.input?.dataset.hasSavedImages === "true";

    this.selectedFiles = [];
    this.previewUrls = [];
    this.primaryIndex = null;
    this.galleryRequestRunning = false;
  }

  initialize() {
    if (
      this.input &&
      this.primaryIndexInput &&
      this.previewContainer &&
      this.previewGrid &&
      this.previewCount
    ) {
      this.initializeNewImageSelection();
    }

    if (this.savedGallery) {
      this.initializeSavedGallery();
    }

    window.addEventListener("beforeunload", () => {
      this.releasePreviewUrls();
    });
  }

  initializeNewImageSelection() {
    this.input.addEventListener("change", () => {
      this.addSelectedFiles(
        Array.from(this.input.files),
      );
    });

    if (this.productForm) {
      this.productForm.addEventListener(
        "submit",
        () => {
          this.synchronizeInputFiles();
          this.synchronizePrimaryIndex();
        },
      );
    }

    this.synchronizePrimaryIndex();
    this.updateRemainingSlots();
  }

  initializeSavedGallery() {
    this.savedGallery.addEventListener(
      "click",
      (event) => {
        const removeButton = event.target.closest(
          ".admin-saved-image-remove",
        );

        if (removeButton) {
          event.preventDefault();
          event.stopPropagation();

          this.deleteSavedImage(removeButton);
          return;
        }

        const primaryButton = event.target.closest(
          ".admin-saved-image-primary-button",
        );

        if (primaryButton) {
          event.preventDefault();

          this.makeSavedImagePrimary(
            primaryButton,
          );
        }
      },
    );
  }

  addSelectedFiles(newFiles) {
    if (newFiles.length === 0) {
      this.synchronizeInputFiles();
      return;
    }

    const uniqueNewFiles = newFiles.filter(
      (newFile) => {
        return !this.selectedFiles.some(
          (selectedFile) => {
            return this.filesMatch(
              selectedFile,
              newFile,
            );
          },
        );
      },
    );

    const availableSlots = Math.max(
      0,
      this.initialRemainingSlots -
        this.selectedFiles.length,
    );

    if (uniqueNewFiles.length > availableSlots) {
      this.showImageLimitMessage(availableSlots);
      this.synchronizeInputFiles();
      return;
    }

    this.selectedFiles.push(...uniqueNewFiles);

    if (
      !this.hasSavedImages &&
      this.primaryIndex === null &&
      this.selectedFiles.length > 0
    ) {
      this.primaryIndex = 0;
    }

    this.synchronizeInputFiles();
    this.synchronizePrimaryIndex();
    this.renderPreviews();
  }

  filesMatch(firstFile, secondFile) {
    return (
      firstFile.name === secondFile.name &&
      firstFile.size === secondFile.size &&
      firstFile.type === secondFile.type &&
      firstFile.lastModified ===
        secondFile.lastModified
    );
  }

  selectPrimaryImage(index) {
    if (
      index < 0 ||
      index >= this.selectedFiles.length
    ) {
      return;
    }

    this.primaryIndex = index;

    this.synchronizePrimaryIndex();
    this.renderPreviews();
  }

  removeImage(index) {
    if (
      index < 0 ||
      index >= this.selectedFiles.length
    ) {
      return;
    }

    this.selectedFiles.splice(index, 1);

    if (this.selectedFiles.length === 0) {
      this.primaryIndex = null;
    } else if (this.primaryIndex === index) {
      this.primaryIndex = this.hasSavedImages
        ? null
        : 0;
    } else if (
      this.primaryIndex !== null &&
      this.primaryIndex > index
    ) {
      this.primaryIndex -= 1;
    }

    this.synchronizeInputFiles();
    this.synchronizePrimaryIndex();
    this.renderPreviews();
  }

  renderPreviews() {
    this.releasePreviewUrls();
    this.previewGrid.replaceChildren();

    if (this.selectedFiles.length === 0) {
      this.previewContainer.hidden = true;

      this.previewCount.textContent =
        "0 images selected";

      this.updateRemainingSlots();
      return;
    }

    this.selectedFiles.forEach((file, index) => {
      this.createPreview(file, index);
    });

    this.previewCount.textContent =
      `${this.selectedFiles.length} image${
        this.selectedFiles.length === 1
          ? ""
          : "s"
      } selected`;

    this.previewContainer.hidden = false;

    this.updateRemainingSlots();
  }

  createPreview(file, index) {
    const previewUrl = URL.createObjectURL(file);

    this.previewUrls.push(previewUrl);

    const previewCard =
      document.createElement("article");

    previewCard.className =
      "admin-selected-image-card";

    previewCard.tabIndex = 0;
    previewCard.setAttribute("role", "button");

    previewCard.setAttribute(
      "aria-label",
      `Make image ${index + 1} primary`,
    );

    if (index === this.primaryIndex) {
      previewCard.classList.add("is-primary");

      previewCard.setAttribute(
        "aria-pressed",
        "true",
      );
    } else {
      previewCard.setAttribute(
        "aria-pressed",
        "false",
      );
    }

    previewCard.addEventListener("click", () => {
      this.selectPrimaryImage(index);
    });

    previewCard.addEventListener(
      "keydown",
      (event) => {
        if (
          event.key === "Enter" ||
          event.key === " "
        ) {
          event.preventDefault();
          this.selectPrimaryImage(index);
        }
      },
    );

    const imageWrapper =
      document.createElement("div");

    imageWrapper.className =
      "admin-selected-image-preview";

    const image = document.createElement("img");

    image.src = previewUrl;

    image.alt =
      `Selected product image ${index + 1}`;

    const removeButton =
      document.createElement("button");

    removeButton.type = "button";

    removeButton.className =
      "admin-selected-image-remove";

    removeButton.setAttribute(
      "aria-label",
      `Remove image ${index + 1}`,
    );

    removeButton.addEventListener(
      "click",
      (event) => {
        event.stopPropagation();
        this.removeImage(index);
      },
    );

    imageWrapper.append(image, removeButton);

    if (index === this.primaryIndex) {
      const primaryBadge =
        document.createElement("span");

      primaryBadge.className =
        "admin-selected-primary-badge";

      primaryBadge.textContent = "Primary";

      imageWrapper.append(primaryBadge);
    }

    const information =
      document.createElement("div");

    information.className =
      "admin-selected-image-information";

    const position =
      document.createElement("strong");

    position.textContent = `Image ${index + 1}`;

    const filename =
      document.createElement("span");

    filename.textContent = file.name;
    filename.title = file.name;

    information.append(position, filename);

    previewCard.append(
      imageWrapper,
      information,
    );

    this.previewGrid.append(previewCard);
  }

  async makeSavedImagePrimary(button) {
    if (
      this.galleryRequestRunning ||
      button.getAttribute("aria-pressed") ===
        "true"
    ) {
      return;
    }

    const requestUrl =
      button.dataset.primaryUrl;

    if (!requestUrl) {
      return;
    }

    this.galleryRequestRunning = true;
    this.setGalleryBusy(true);

    try {
      const images =
        await this.sendGalleryRequest(requestUrl);

      this.updateSavedGallery(images);
    } catch (error) {
      window.alert(
        error.message ||
          "The primary image could not be changed.",
      );
    } finally {
      this.galleryRequestRunning = false;
      this.setGalleryBusy(false);
    }
  }

  async deleteSavedImage(button) {
    if (this.galleryRequestRunning) {
      return;
    }

    const confirmed = window.confirm(
      "Remove this product image?",
    );

    if (!confirmed) {
      return;
    }

    const requestUrl =
      button.dataset.deleteUrl;

    if (!requestUrl) {
      return;
    }

    this.galleryRequestRunning = true;
    this.setGalleryBusy(true);

    try {
      const images =
        await this.sendGalleryRequest(requestUrl);

      this.updateSavedGallery(images);
    } catch (error) {
      window.alert(
        error.message ||
          "The product image could not be removed.",
      );
    } finally {
      this.galleryRequestRunning = false;
      this.setGalleryBusy(false);
    }
  }

  async sendGalleryRequest(requestUrl) {
    const requestBody = new FormData();

    const csrfInput = this.productForm?.querySelector(
      'input[name="_csrf"]',
    );

    if (csrfInput) {
      requestBody.append(
        csrfInput.name,
        csrfInput.value,
      );
    }

    const response = await fetch(requestUrl, {
      method: "POST",
      body: requestBody,
      headers: {
        Accept: "application/json",
      },
    });

    if (!response.ok) {
      throw new Error(
        "The image operation could not be completed.",
      );
    }

    return response.json();
  }

  updateSavedGallery(images) {
    const imagesById = new Map(
      images.map((image) => {
        return [String(image.id), image];
      }),
    );

    const savedCards = Array.from(
      this.savedGallery.querySelectorAll(
        ".admin-product-gallery-item",
      ),
    );

    savedCards.forEach((card) => {
      const imageId =
        card.dataset.productImageId;

      const imageData =
        imagesById.get(imageId);

      if (!imageData) {
        card.remove();
        return;
      }

      card.classList.toggle(
        "is-primary",
        imageData.primary,
      );

      const primaryButton = card.querySelector(
        ".admin-saved-image-primary-button",
      );

      if (primaryButton) {
        primaryButton.setAttribute(
          "aria-pressed",
          String(imageData.primary),
        );
      }

      const existingBadge = card.querySelector(
        ".admin-product-primary-badge",
      );

      if (imageData.primary && !existingBadge) {
        const badge =
          document.createElement("span");

        badge.className =
          "admin-product-primary-badge";

        badge.textContent = "Primary";

        card
          .querySelector(
            ".admin-product-gallery-image",
          )
          ?.append(badge);
      }

      if (!imageData.primary && existingBadge) {
        existingBadge.remove();
      }
    });

    this.hasSavedImages = images.length > 0;

    this.initialRemainingSlots = Math.max(
      0,
      this.getMaximumImages() -
        images.length,
    );

    this.input.dataset.remainingSlots = String(
      this.initialRemainingSlots,
    );

    this.input.dataset.hasSavedImages = String(
      this.hasSavedImages,
    );

    this.input.disabled =
      this.initialRemainingSlots === 0;

    if (this.savedImageCountElement) {
      this.savedImageCountElement.textContent =
        String(images.length);
    }

    this.updateRemainingSlots();
  }

  getMaximumImages() {
    return Number(
      this.savedGallery?.dataset.maximumImages ||
        8,
    );
  }

  setGalleryBusy(busy) {
    this.savedGallery.classList.toggle(
      "is-busy",
      busy,
    );

    const buttons =
      this.savedGallery.querySelectorAll("button");

    buttons.forEach((button) => {
      button.disabled = busy;
    });
  }

  synchronizeInputFiles() {
    const transfer = new DataTransfer();

    this.selectedFiles.forEach((file) => {
      transfer.items.add(file);
    });

    this.input.files = transfer.files;
  }

  synchronizePrimaryIndex() {
    this.primaryIndexInput.value =
      this.primaryIndex === null
        ? ""
        : String(this.primaryIndex);
  }

  updateRemainingSlots() {
    if (!this.remainingSlotsElement) {
      return;
    }

    const remainingSlots = Math.max(
      0,
      this.initialRemainingSlots -
        this.selectedFiles.length,
    );

    this.remainingSlotsElement.textContent =
      String(remainingSlots);
  }

  releasePreviewUrls() {
    this.previewUrls.forEach((previewUrl) => {
      URL.revokeObjectURL(previewUrl);
    });

    this.previewUrls = [];
  }

  showImageLimitMessage(availableSlots) {
    if (availableSlots === 0) {
      window.alert(
        "This product already has the maximum number of images.",
      );

      return;
    }

    window.alert(
      `You can select only ${availableSlots} more image${
        availableSlots === 1 ? "" : "s"
      }.`,
    );
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const productImageUpload =
    new AdminProductImageUpload();

  productImageUpload.initialize();
});