document.addEventListener("DOMContentLoaded", () => {
  const desktopPointer = window.matchMedia(
    "(min-width: 992px) "
      + "and (hover: hover) "
      + "and (pointer: fine)",
  );

  const dropdowns = Array.from(
    document.querySelectorAll(
      "[data-hover-dropdown]",
    ),
  );

  dropdowns.forEach((dropdown) => {
    const toggle = dropdown.querySelector(
      "[data-bs-toggle='dropdown']",
    );

    if (!toggle || typeof bootstrap === "undefined") {
      return;
    }

    const bootstrapDropdown =
      bootstrap.Dropdown.getOrCreateInstance(toggle);

    let closeTimer = null;

    const cancelScheduledClose = () => {
      if (closeTimer !== null) {
        window.clearTimeout(closeTimer);
        closeTimer = null;
      }
    };

    const openDropdown = () => {
      if (!desktopPointer.matches) {
        return;
      }

      cancelScheduledClose();
      bootstrapDropdown.show();
    };

    const scheduleDropdownClose = () => {
      if (!desktopPointer.matches) {
        return;
      }

      cancelScheduledClose();

      closeTimer = window.setTimeout(() => {
        bootstrapDropdown.hide();
        closeTimer = null;
      }, 160);
    };

    dropdown.addEventListener(
      "mouseenter",
      openDropdown,
    );

    dropdown.addEventListener(
      "mouseleave",
      scheduleDropdownClose,
    );

    dropdown.addEventListener(
      "focusin",
      openDropdown,
    );

    dropdown.addEventListener("focusout", () => {
      window.setTimeout(() => {
        if (
          !dropdown.contains(document.activeElement)
        ) {
          scheduleDropdownClose();
        }
      }, 0);
    });

    desktopPointer.addEventListener("change", () => {
      cancelScheduledClose();

      if (!desktopPointer.matches) {
        bootstrapDropdown.hide();
      }
    });
  });
});