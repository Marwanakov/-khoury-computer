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