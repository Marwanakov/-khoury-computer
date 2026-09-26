package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.cart.CartResponse;
import com.khourycomputer.application.dto.common.address.AddressRequest;
import com.khourycomputer.application.dto.order.SubmitOrderRequest;
import com.khourycomputer.application.dto.order.SubmitOrderResponse;
import com.khourycomputer.application.service.CartApplicationService;
import com.khourycomputer.application.service.OrderApplicationService;
import com.khourycomputer.config.security.CurrentUserService;
import com.khourycomputer.web.viewmodel.checkout.CheckoutForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckoutController {

        private final CartApplicationService cartApplicationService;
        private final OrderApplicationService orderApplicationService;
        private final CurrentUserService currentUserService;

        public CheckoutController(
                        CartApplicationService cartApplicationService,
                        OrderApplicationService orderApplicationService,
                        CurrentUserService currentUserService) {
                this.cartApplicationService = cartApplicationService;
                this.orderApplicationService = orderApplicationService;
                this.currentUserService = currentUserService;
        }

        @GetMapping("/checkout")
        public String showCheckoutPage(
                        Model model,
                        RedirectAttributes redirectAttributes) {
                Long currentUserId =
                                currentUserService.getCurrentUserId();

                CartResponse cart =
                                cartApplicationService.getCartByUserId(
                                                currentUserId);

                if (cart.items().isEmpty()) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "Add at least one product before checking out.");

                        return "redirect:/cart";
                }

                model.addAttribute("cart", cart);

                model.addAttribute(
                                "user",
                                currentUserService.getCurrentUser());

                if (!model.containsAttribute("checkoutForm")) {
                        model.addAttribute(
                                        "checkoutForm",
                                        new CheckoutForm());
                }

                return "public/checkout";
        }

        @PostMapping("/checkout/submit")
        public String submitOrder(
                        @Valid
                        @ModelAttribute("checkoutForm")
                        CheckoutForm checkoutForm,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                Long currentUserId =
                                currentUserService.getCurrentUserId();

                if (bindingResult.hasErrors()) {
                        populateCheckoutModel(
                                        currentUserId,
                                        model);

                        return "public/checkout";
                }

                try {
                        SubmitOrderResponse response =
                                        orderApplicationService.submitOrder(
                                                        currentUserId,
                                                        toSubmitOrderRequest(
                                                                        checkoutForm));

                        redirectAttributes.addFlashAttribute(
                                        "confirmationMessage",
                                        response.confirmationMessage());

                        return "redirect:/orders/confirmation/"
                                        + response.order().id();

                } catch (IllegalArgumentException exception) {
                        bindingResult.reject(
                                        "checkout.submit.failed",
                                        exception.getMessage());

                        populateCheckoutModel(
                                        currentUserId,
                                        model);

                        return "public/checkout";
                }
        }

        private SubmitOrderRequest toSubmitOrderRequest(
                        CheckoutForm checkoutForm) {
                AddressRequest deliveryAddress =
                                new AddressRequest(
                                                checkoutForm.getCity(),
                                                checkoutForm.getStreet(),
                                                checkoutForm.getAddressDetails());

                return new SubmitOrderRequest(
                                checkoutForm.getPhoneCountryCode(),
                                checkoutForm.getPhoneNumber(),
                                deliveryAddress);
        }

        private void populateCheckoutModel(
                        Long userId,
                        Model model) {
                model.addAttribute(
                                "cart",
                                cartApplicationService.getCartByUserId(
                                                userId));

                model.addAttribute(
                                "user",
                                currentUserService.getCurrentUser());
        }
}