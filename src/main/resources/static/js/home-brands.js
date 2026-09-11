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

    const brandImages = Array.from(
        track.querySelectorAll("img")
    );

    const waitForImage = image => {
        if (image.complete) {
            return Promise.resolve();
        }

        return new Promise(resolve => {
            image.addEventListener("load", resolve, {
                once: true
            });

            image.addEventListener("error", resolve, {
                once: true
            });
        });
    };

    /*
     * Start the marquee after every brand image has either
     * loaded or failed. This prevents transformed lazy images
     * from temporarily disappearing while the strip moves.
     */
    Promise.all(
        brandImages.map(waitForImage)
    ).then(() => {
        marquee.classList.add("is-ready");
    });

    const getTrackAnimation = () => {
        return track
            .getAnimations()
            .find(animation => {
                return animation.animationName ===
                    "home-brand-scroll";
            });
    };

    const moveBrands = direction => {
        const animation = getTrackAnimation();

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
         * Use a larger time step because the automatic
         * animation now has a longer duration.
         */
        const navigationStep = 5000;

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
     * Mouse clicks should not leave the marquee paused
     * because a navigation button retained focus.
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