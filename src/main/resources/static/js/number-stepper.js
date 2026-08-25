const cartUpdateTimers = new WeakMap();

function scheduleCartUpdate(input) {
    const form = input.closest(".cart-quantity-form");

    if (!form) {
        return;
    }

    const currentTimer = cartUpdateTimers.get(form);

    if (currentTimer) {
        window.clearTimeout(currentTimer);
    }

    const timer = window.setTimeout(function () {
        if (!input.checkValidity()) {
            input.reportValidity();
            return;
        }

        form.classList.add("is-updating");

        form.querySelectorAll("button").forEach(function (button) {
        button.disabled = true;
    });

    sessionStorage.setItem(
    "cartScrollPosition",
    String(window.scrollY)
);

        form.requestSubmit();
    }, 500);

    cartUpdateTimers.set(form, timer);
}

document.addEventListener("click", function (event) {
    const button = event.target.closest("[data-stepper-action]");

    if (!button) {
        return;
    }

    const stepper = button.closest(".number-stepper");
    const input = stepper?.querySelector(".number-stepper-input");

    if (!input || input.disabled || input.readOnly) {
        return;
    }

    const step = Number(input.step) || 1;
    const min = input.min === "" ? -Infinity : Number(input.min);
    const max = input.max === "" ? Infinity : Number(input.max);

    let value = Number(input.value);

    if (!Number.isFinite(value)) {
        value = Number.isFinite(min) ? min : 0;
    }

    if (button.dataset.stepperAction === "increase") {
        value = Math.min(value + step, max);
    } else {
        value = Math.max(value - step, min);
    }

    input.value = String(value);
    input.dispatchEvent(new Event("input", { bubbles: true }));
    input.dispatchEvent(new Event("change", { bubbles: true }));
    input.focus();
});

document.addEventListener("input", function (event) {
    const input = event.target.closest(
        ".cart-quantity-form .number-stepper-input"
    );

    if (!input) {
        return;
    }

    scheduleCartUpdate(input);
});

document.addEventListener("DOMContentLoaded", function () {
    if (window.location.pathname !== "/cart") {
        return;
    }

    const savedPosition = sessionStorage.getItem(
        "cartScrollPosition"
    );

    if (savedPosition === null) {
        return;
    }

    sessionStorage.removeItem("cartScrollPosition");

    window.requestAnimationFrame(function () {
        window.scrollTo({
            top: Number(savedPosition),
            left: 0,
            behavior: "instant"
        });
    });
});