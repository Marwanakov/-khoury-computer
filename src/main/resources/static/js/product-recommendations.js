document.addEventListener("DOMContentLoaded", () => {
  const carousels = document.querySelectorAll(
    "[data-recommendation-carousel]"
  );

  for (const carousel of carousels) {
    const track = carousel.querySelector(
      "[data-recommendation-track]"
    );

    const previousButton = carousel.querySelector(
      "[data-recommendation-previous]"
    );

    const nextButton = carousel.querySelector(
      "[data-recommendation-next]"
    );

    if (!track || !previousButton || !nextButton) {
      continue;
    }

    const getScrollStep = () => {
      const firstCard = track.querySelector(
        ".product-recommendation-card"
      );

      if (!firstCard) {
        return track.clientWidth;
      }

      const trackStyles = window.getComputedStyle(track);
      const gap = Number.parseFloat(trackStyles.columnGap) || 0;

      return firstCard.getBoundingClientRect().width + gap;
    };

    const updateNavigation = () => {
      const maximumScroll =
        track.scrollWidth - track.clientWidth;

      previousButton.disabled = track.scrollLeft <= 2;

      nextButton.disabled =
        track.scrollLeft >= maximumScroll - 2;
    };

    previousButton.addEventListener("click", () => {
      track.scrollBy({
        left: -getScrollStep(),
        behavior: "smooth"
      });
    });

    nextButton.addEventListener("click", () => {
      track.scrollBy({
        left: getScrollStep(),
        behavior: "smooth"
      });
    });

    track.addEventListener(
      "scroll",
      updateNavigation,
      { passive: true }
    );

    window.addEventListener(
      "resize",
      updateNavigation
    );

    updateNavigation();
  }
});