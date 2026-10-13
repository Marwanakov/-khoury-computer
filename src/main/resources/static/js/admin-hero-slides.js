document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector(
    "[data-hero-slide-form]",
  );

  if (!form) {
    return;
  }

  const linkType = form.querySelector(
    "[data-link-type]",
  );

  const productDestination = form.querySelector(
    "[data-product-destination]",
  );

  const categoryDestination = form.querySelector(
    "[data-category-destination]",
  );

  const productSelect = form.querySelector(
    "#linkedProductId",
  );

  const categorySelect = form.querySelector(
    "#linkedCategoryId",
  );

  const updateDestinationFields = (
    clearUnusedValues = false,
  ) => {
    const selectedType = linkType?.value ?? "NONE";
    const usesProduct = selectedType === "PRODUCT";
    const usesCategory = selectedType === "CATEGORY";

    productDestination?.classList.toggle(
      "is-hidden",
      !usesProduct,
    );

    categoryDestination?.classList.toggle(
      "is-hidden",
      !usesCategory,
    );

    if (productSelect) {
      productSelect.disabled = !usesProduct;
      productSelect.required = usesProduct;

      if (clearUnusedValues && !usesProduct) {
        productSelect.value = "";
      }
    }

    if (categorySelect) {
      categorySelect.disabled = !usesCategory;
      categorySelect.required = usesCategory;

      if (clearUnusedValues && !usesCategory) {
        categorySelect.value = "";
      }
    }
  };

  const connectImagePreview = (
    inputSelector,
    previewSelector,
    placeholderSelector,
  ) => {
    const input = form.querySelector(inputSelector);
    const preview = form.querySelector(previewSelector);

    const placeholder = form.querySelector(
      placeholderSelector,
    );

    input?.addEventListener("change", () => {
      const file = input.files?.[0];

      if (!file || !preview) {
        return;
      }

      const objectUrl = URL.createObjectURL(file);

      preview.src = objectUrl;
      preview.hidden = false;

      if (placeholder) {
        placeholder.hidden = true;
      }

      preview.onload = () => {
        URL.revokeObjectURL(objectUrl);
      };
    });
  };

  linkType?.addEventListener("change", () => {
    updateDestinationFields(true);
  });

  /*
   * Preserve the saved selection when initially opening
   * an edit form.
   */
  updateDestinationFields(false);

  connectImagePreview(
    "[data-desktop-input]",
    "[data-desktop-preview]",
    "[data-desktop-placeholder]",
  );

  connectImagePreview(
    "[data-mobile-input]",
    "[data-mobile-preview]",
    "[data-mobile-placeholder]",
  );
});