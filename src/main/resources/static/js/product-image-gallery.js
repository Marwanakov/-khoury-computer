"use strict";

class ProductImageGallery {
  constructor(gallery) {
    this.gallery = gallery;

    this.viewport = gallery.querySelector(
      "[data-product-gallery-viewport]",
    );

    this.slides = Array.from(
      gallery.querySelectorAll(
        "[data-product-gallery-slide]",
      ),
    );

    this.previousButton = gallery.querySelector(
      "[data-product-gallery-previous]",
    );

    this.nextButton = gallery.querySelector(
      "[data-product-gallery-next]",
    );

    this.currentCounter = gallery.querySelector(
      "[data-product-gallery-current]",
    );

    this.currentIndex = 0;
    this.scrollFrame = null;
    this.navigationTimer = null;
    this.navigationTarget = null;
  }

  initialize() {
    if (!this.viewport || this.slides.length === 0) {
      return;
    }

    this.previousButton?.addEventListener(
      "click",
      (event) => {
        event.preventDefault();
        this.showImage(this.currentIndex - 1);
      },
    );

    this.nextButton?.addEventListener(
      "click",
      (event) => {
        event.preventDefault();
        this.showImage(this.currentIndex + 1);
      },
    );

    this.viewport.addEventListener(
      "scroll",
      () => {
        this.handleScroll();
      },
      {
        passive: true,
      },
    );

    this.viewport.addEventListener(
      "keydown",
      (event) => {
        this.handleKeyboard(event);
      },
    );

    window.addEventListener("resize", () => {
      this.realignCurrentImage();
    });

    this.updateControls();
  }

  showImage(index) {
    const targetIndex = Math.max(
      0,
      Math.min(index, this.slides.length - 1),
    );

    const targetSlide = this.slides[targetIndex];

    if (!targetSlide) {
      return;
    }

    this.currentIndex = targetIndex;
    this.navigationTarget = targetIndex;

    this.updateControls();

    /*
     * Calculate the exact distance from the current viewport
     * position to the requested slide. This remains correct
     * even when borders or responsive widths are involved.
     */
    const viewportRectangle =
      this.viewport.getBoundingClientRect();

    const slideRectangle =
      targetSlide.getBoundingClientRect();

    const targetScrollLeft =
      this.viewport.scrollLeft +
      slideRectangle.left -
      viewportRectangle.left;

    this.viewport.scrollTo({
      left: targetScrollLeft,
      behavior: this.prefersReducedMotion()
        ? "auto"
        : "smooth",
    });

    window.clearTimeout(this.navigationTimer);

    this.navigationTimer = window.setTimeout(
      () => {
        this.navigationTarget = null;
        this.synchronizeIndexFromScroll();
      },
      this.prefersReducedMotion() ? 0 : 450,
    );
  }

  handleScroll() {
    if (this.scrollFrame !== null) {
      window.cancelAnimationFrame(
        this.scrollFrame,
      );
    }

    this.scrollFrame = window.requestAnimationFrame(
      () => {
        /*
         * While an arrow-triggered animation is running,
         * retain its target index instead of allowing the
         * first few scroll pixels to reset it.
         */
        if (this.navigationTarget !== null) {
          this.currentIndex =
            this.navigationTarget;

          this.updateControls();
        } else {
          this.synchronizeIndexFromScroll();
        }

        this.scrollFrame = null;
      },
    );
  }

  synchronizeIndexFromScroll() {
    if (this.slides.length === 0) {
      return;
    }

    const viewportRectangle =
      this.viewport.getBoundingClientRect();

    const viewportCenter =
      viewportRectangle.left +
      viewportRectangle.width / 2;

    let closestIndex = 0;
    let closestDistance = Number.POSITIVE_INFINITY;

    this.slides.forEach((slide, index) => {
      const slideRectangle =
        slide.getBoundingClientRect();

      const slideCenter =
        slideRectangle.left +
        slideRectangle.width / 2;

      const distance = Math.abs(
        slideCenter - viewportCenter,
      );

      if (distance < closestDistance) {
        closestDistance = distance;
        closestIndex = index;
      }
    });

    this.currentIndex = closestIndex;
    this.updateControls();
  }

  handleKeyboard(event) {
    if (event.key === "ArrowLeft") {
      event.preventDefault();
      this.showImage(this.currentIndex - 1);
      return;
    }

    if (event.key === "ArrowRight") {
      event.preventDefault();
      this.showImage(this.currentIndex + 1);
      return;
    }

    if (event.key === "Home") {
      event.preventDefault();
      this.showImage(0);
      return;
    }

    if (event.key === "End") {
      event.preventDefault();

      this.showImage(
        this.slides.length - 1,
      );
    }
  }

  realignCurrentImage() {
    const currentSlide =
      this.slides[this.currentIndex];

    if (!currentSlide) {
      return;
    }

    const viewportRectangle =
      this.viewport.getBoundingClientRect();

    const slideRectangle =
      currentSlide.getBoundingClientRect();

    const targetScrollLeft =
      this.viewport.scrollLeft +
      slideRectangle.left -
      viewportRectangle.left;

    this.viewport.scrollTo({
      left: targetScrollLeft,
      behavior: "auto",
    });
  }

  updateControls() {
    if (this.previousButton) {
      this.previousButton.disabled =
        this.currentIndex === 0;
    }

    if (this.nextButton) {
      this.nextButton.disabled =
        this.currentIndex ===
        this.slides.length - 1;
    }

    if (this.currentCounter) {
      this.currentCounter.textContent = String(
        this.currentIndex + 1,
      );
    }
  }

  prefersReducedMotion() {
    return window.matchMedia(
      "(prefers-reduced-motion: reduce)",
    ).matches;
  }
}

function initializeProductImageGalleries() {
  document
    .querySelectorAll("[data-product-gallery]")
    .forEach((gallery) => {
      const productImageGallery =
        new ProductImageGallery(gallery);

      productImageGallery.initialize();
    });
}

/*
 * This works whether the script loads before or after the
 * DOMContentLoaded event.
 */
initializeProductImageGalleries();