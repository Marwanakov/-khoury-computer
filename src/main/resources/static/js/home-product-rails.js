document.addEventListener("DOMContentLoaded", () => {
  const rails = document.querySelectorAll(
    "[data-product-rail]",
  );

  rails.forEach((rail) => {
    const viewport = rail.querySelector(
      "[data-product-rail-viewport]",
    );

    const track = viewport?.querySelector(
      ".home-product-rail-track",
    );

    const previousButton = rail.querySelector(
      "[data-product-rail-previous]",
    );

    const nextButton = rail.querySelector(
      "[data-product-rail-next]",
    );

    const originalCards = track
      ? Array.from(
          track.querySelectorAll(".home-product-card"),
        )
      : [];

    if (
      !viewport
      || !track
      || !previousButton
      || !nextButton
      || originalCards.length === 0
    ) {
      return;
    }

    const getGap = () =>
      Number.parseFloat(
        window.getComputedStyle(track).columnGap,
      ) || 0;

    const getCardStep = () =>
      originalCards[0]
        .getBoundingClientRect()
        .width + getGap();

    const getOriginalWidth = () =>
      originalCards.length * getCardStep();

    const originalContentWidth = () =>
      originalCards.length
        * originalCards[0]
            .getBoundingClientRect()
            .width
        + Math.max(
            0,
            originalCards.length - 1,
          ) * getGap();

    const navigationRequired = () =>
      originalCards.length > 1
      && originalContentWidth()
        > viewport.clientWidth + 4;

    previousButton.hidden = true;
    nextButton.hidden = true;

    /*
     * Wait until styles and images have established
     * the card dimensions.
     */
    window.requestAnimationFrame(() => {
      if (!navigationRequired()) {
        return;
      }

      previousButton.hidden = false;
      nextButton.hidden = false;

      const createClone = (card) => {
        const clone = card.cloneNode(true);

        clone.dataset.productRailClone = "";
        clone.setAttribute("aria-hidden", "true");
        clone.setAttribute("tabindex", "-1");

        return clone;
      };

      const leadingFragment =
        document.createDocumentFragment();

      const trailingFragment =
        document.createDocumentFragment();

      originalCards.forEach((card) => {
        leadingFragment.appendChild(
          createClone(card),
        );

        trailingFragment.appendChild(
          createClone(card),
        );
      });

      track.prepend(leadingFragment);
      track.append(trailingFragment);

      let repositioning = false;
      let scrollFinishedTimer = null;
      let resizeTimer = null;

      const originalStart = () =>
        getOriginalWidth();

      const originalEnd = () =>
        originalStart() + getOriginalWidth();

      const setScrollPositionImmediately = (left) => {
        repositioning = true;

        const previousBehavior =
          viewport.style.scrollBehavior;

        viewport.style.scrollBehavior = "auto";
        viewport.scrollLeft = left;

        /*
         * Force the immediate position to be applied
         * before restoring normal smooth scrolling.
         */
        void viewport.offsetWidth;

        viewport.style.scrollBehavior =
          previousBehavior;

        window.requestAnimationFrame(() => {
          repositioning = false;
        });
      };

      const normalizeInfinitePosition = () => {
        if (repositioning) {
          return;
        }

        const start = originalStart();
        const width = getOriginalWidth();
        const end = start + width;
        const currentPosition = viewport.scrollLeft;

        if (currentPosition < start) {
          setScrollPositionImmediately(
            currentPosition + width,
          );

          return;
        }

        if (currentPosition >= end) {
          setScrollPositionImmediately(
            currentPosition - width,
          );
        }
      };

      const schedulePositionNormalization = () => {
        window.clearTimeout(scrollFinishedTimer);

        scrollFinishedTimer = window.setTimeout(
          normalizeInfinitePosition,
          100,
        );
      };

      const showNextProduct = () => {
        viewport.scrollBy({
          left: getCardStep(),
          behavior: "smooth",
        });
      };

      const showPreviousProduct = () => {
        viewport.scrollBy({
          left: -getCardStep(),
          behavior: "smooth",
        });
      };

      const showFirstProduct = () => {
        setScrollPositionImmediately(
          originalStart(),
        );
      };

      const showLastProduct = () => {
        setScrollPositionImmediately(
          originalStart()
            + (
              originalCards.length - 1
            ) * getCardStep(),
        );
      };

      previousButton.addEventListener(
        "click",
        showPreviousProduct,
      );

      nextButton.addEventListener(
        "click",
        showNextProduct,
      );

      viewport.addEventListener(
        "scroll",
        schedulePositionNormalization,
        { passive: true },
      );

      viewport.addEventListener(
        "keydown",
        (event) => {
          if (event.key === "ArrowLeft") {
            event.preventDefault();
            showPreviousProduct();
          }

          if (event.key === "ArrowRight") {
            event.preventDefault();
            showNextProduct();
          }

          if (event.key === "Home") {
            event.preventDefault();
            showFirstProduct();
          }

          if (event.key === "End") {
            event.preventDefault();
            showLastProduct();
          }
        },
      );

      window.addEventListener("resize", () => {
        window.clearTimeout(resizeTimer);

        resizeTimer = window.setTimeout(() => {
          if (!navigationRequired()) {
            previousButton.hidden = true;
            nextButton.hidden = true;
            return;
          }

          previousButton.hidden = false;
          nextButton.hidden = false;

          /*
           * Return to an equivalent stable section
           * after responsive card dimensions change.
           */
          normalizeInfinitePosition();
        }, 150);
      });

      /*
       * Begin on the first real card. An identical
       * duplicate set exists on both sides.
       */
      setScrollPositionImmediately(
        originalStart(),
      );
    });
  });
});