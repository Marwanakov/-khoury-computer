document.addEventListener("DOMContentLoaded", () => {
    const marquee = document.querySelector(
        "[data-brand-marquee]"
    );

    if (!marquee) {
        return;
    }

    const track = marquee.querySelector(
        "[data-brand-track]"
    );

    const previousButton = marquee.querySelector(
        "[data-brand-previous]"
    );

    const nextButton = marquee.querySelector(
        "[data-brand-next]"
    );

    if (!track || !previousButton || !nextButton) {
        return;
    }

    const reducedMotionQuery = window.matchMedia(
        "(prefers-reduced-motion: reduce)"
    );

    const moveBrands = (direction) => {
        const animation = track.getAnimations()[0];

        if (!animation) {
            marquee.scrollBy({
                left: direction * 340,
                behavior: "smooth"
            });

            return;
        }

        const timing = animation.effect.getTiming();
        const duration = Number(timing.duration);

        if (!Number.isFinite(duration) || duration <= 0) {
            return;
        }

        const currentTime = Number(
            animation.currentTime ?? 0
        );

        /*
         * Moving forward through the animation moves the
         * track to the left and reveals later brands.
         */
        const navigationStep = 3000;

        const nextTime =
                (
                    currentTime
                    + direction * navigationStep
                    + duration
                )
                % duration;

        animation.currentTime = nextTime;
    };

    previousButton.addEventListener("click", () => {
        moveBrands(-1);
    });

    nextButton.addEventListener("click", () => {
        moveBrands(1);
    });

    /*
     * Mouse clicks should not leave the whole marquee paused
     * because of the button retaining focus. Keyboard focus
     * is preserved for accessible navigation.
     */
    for (const button of [
        previousButton,
        nextButton
    ]) {
        button.addEventListener("pointerdown", event => {
            event.preventDefault();
        });
    }

    reducedMotionQuery.addEventListener("change", () => {
        marquee.scrollTo({
            left: 0,
            behavior: "auto"
        });
    });
});