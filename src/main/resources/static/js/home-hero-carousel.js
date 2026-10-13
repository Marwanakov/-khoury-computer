document.addEventListener("DOMContentLoaded", () => {
  const carousels = document.querySelectorAll(
    "[data-hero-carousel]",
  );

  carousels.forEach((carousel) => {
    const slides = Array.from(
      carousel.querySelectorAll(".hero-carousel-slide")
    );

    const previousButton = carousel.querySelector(
      "[data-hero-previous]",
    );

    const nextButton = carousel.querySelector(
      "[data-hero-next]",
    );

    const dots = Array.from(
      carousel.querySelectorAll("[data-hero-dot]"),
    );

    if (slides.length <= 1) {
  if (previousButton) {
    previousButton.hidden = true;
  }

  if (nextButton) {
    nextButton.hidden = true;
  }

  dots.forEach((dot) => {
    dot.hidden = true;
  });

  return;
}

    const reduceMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)",
    ).matches;

    let currentIndex = 0;
    let intervalId = null;
    let touchStartX = null;

    const showSlide = (nextIndex) => {
      currentIndex =
        (nextIndex + slides.length) % slides.length;

      slides.forEach((slide, index) => {
        const active = index === currentIndex;

        slide.hidden = !active;
        slide.setAttribute(
          "aria-hidden",
          active ? "false" : "true",
        );
      });

      dots.forEach((dot, index) => {
        const active = index === currentIndex;

        dot.classList.toggle("is-active", active);
        dot.setAttribute(
          "aria-current",
          active ? "true" : "false",
        );
      });
    };

    const stopAutomaticChange = () => {
      if (intervalId !== null) {
        window.clearInterval(intervalId);
        intervalId = null;
      }
    };

    const startAutomaticChange = () => {
      stopAutomaticChange();

      if (reduceMotion || document.hidden) {
        return;
      }

      intervalId = window.setInterval(() => {
        showSlide(currentIndex + 1);
      }, 6000);
    };

    const showManualSlide = (nextIndex) => {
      showSlide(nextIndex);
      startAutomaticChange();
    };

    previousButton?.addEventListener("click", () => {
      showManualSlide(currentIndex - 1);
    });

    nextButton?.addEventListener("click", () => {
      showManualSlide(currentIndex + 1);
    });

    dots.forEach((dot) => {
      dot.addEventListener("click", () => {
        const nextIndex = Number(
          dot.dataset.heroDot,
        );

        if (Number.isInteger(nextIndex)) {
          showManualSlide(nextIndex);
        }
      });
    });

    carousel.addEventListener(
      "mouseenter",
      stopAutomaticChange,
    );

    carousel.addEventListener(
      "mouseleave",
      startAutomaticChange,
    );

    carousel.addEventListener(
      "focusin",
      stopAutomaticChange,
    );

    carousel.addEventListener("focusout", () => {
      window.setTimeout(() => {
        if (!carousel.contains(document.activeElement)) {
          startAutomaticChange();
        }
      }, 0);
    });

    carousel.addEventListener(
      "touchstart",
      (event) => {
        touchStartX =
          event.changedTouches[0]?.clientX ?? null;
      },
      { passive: true },
    );

    carousel.addEventListener(
      "touchend",
      (event) => {
        if (touchStartX === null) {
          return;
        }

        const touchEndX =
          event.changedTouches[0]?.clientX ?? touchStartX;

        const movement = touchEndX - touchStartX;

        touchStartX = null;

        if (Math.abs(movement) < 45) {
          return;
        }

        showManualSlide(
          movement > 0
            ? currentIndex - 1
            : currentIndex + 1,
        );
      },
      { passive: true },
    );

    document.addEventListener(
      "visibilitychange",
      () => {
        if (document.hidden) {
          stopAutomaticChange();
        } else {
          startAutomaticChange();
        }
      },
    );

    showSlide(0);
    startAutomaticChange();
  });
});