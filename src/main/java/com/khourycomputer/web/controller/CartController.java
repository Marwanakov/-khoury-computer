package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.cart.AddCartItemRequest;
import com.khourycomputer.application.dto.cart.UpdateCartItemQuantityRequest;
import com.khourycomputer.application.service.CartApplicationService;
import com.khourycomputer.config.security.CurrentUserService;
import com.khourycomputer.web.viewmodel.cart.PendingCartAction;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.khourycomputer.config.security.PendingCartAuthenticationSuccessHandler;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

        private final CartApplicationService cartApplicationService;
        private final CurrentUserService currentUserService;

        public CartController(
                        CartApplicationService cartApplicationService,
                        CurrentUserService currentUserService) {
                this.cartApplicationService = cartApplicationService;
                this.currentUserService = currentUserService;
        }

        @GetMapping("/cart")
        public String showCartPage(
                        Model model,
                        HttpSession session) {
                Long currentUserId = currentUserService.getCurrentUserId();

                model.addAttribute(
                                "cart",
                                cartApplicationService.getCartByUserId(currentUserId));

                moveSessionMessageToModel(
                                session,
                                PendingCartAuthenticationSuccessHandler.SUCCESS_MESSAGE_SESSION_ATTRIBUTE,
                                "successMessage",
                                model);

                moveSessionMessageToModel(
                                session,
                                PendingCartAuthenticationSuccessHandler.ERROR_MESSAGE_SESSION_ATTRIBUTE,
                                "errorMessage",
                                model);

                return "public/cart";
        }

        @PostMapping("/cart/items")
        public String addItemToCart(
                        @RequestParam Long productId,
                        @RequestParam(defaultValue = "1") int quantity,
                        Authentication authentication,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
                boolean isAnonymous = authentication == null
                                || !authentication.isAuthenticated()
                                || authentication instanceof AnonymousAuthenticationToken;

                if (isAnonymous) {
                        session.setAttribute(
                                        PendingCartAction.SESSION_ATTRIBUTE,
                                        new PendingCartAction(productId, quantity));

                        redirectAttributes.addFlashAttribute(
                                        "infoMessage",
                                        "Sign in or create an account to add this product to your cart.");

                        return "redirect:/login";
                }

                Long currentUserId = currentUserService.getCurrentUserId();

                cartApplicationService.addItemToCart(
                                currentUserId,
                                new AddCartItemRequest(productId, quantity));

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Product added to cart successfully.");

                return "redirect:/cart";
        }

        @PostMapping("/cart/items/update")
        public String updateCartItemQuantity(
                        @RequestParam Long productId,
                        @RequestParam int quantity,
                        RedirectAttributes redirectAttributes) {
                Long currentUserId = currentUserService.getCurrentUserId();

                cartApplicationService.updateItemQuantity(
                                currentUserId,
                                new UpdateCartItemQuantityRequest(productId, quantity));

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Cart quantity updated successfully.");

                return "redirect:/cart";
        }

        @PostMapping("/cart/items/remove")
        public String removeProductFromCart(
                        @RequestParam Long productId,
                        RedirectAttributes redirectAttributes) {
                Long currentUserId = currentUserService.getCurrentUserId();

                cartApplicationService.removeProductFromCart(
                                currentUserId,
                                productId);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Product removed from cart.");

                return "redirect:/cart";
        }

        @PostMapping("/cart/clear")
        public String clearCart(RedirectAttributes redirectAttributes) {
                Long currentUserId = currentUserService.getCurrentUserId();

                cartApplicationService.clearCart(currentUserId);

                redirectAttributes.addFlashAttribute(
                                "successMessage",
                                "Cart cleared successfully.");

                return "redirect:/cart";
        }

        private void moveSessionMessageToModel(
                        HttpSession session,
                        String sessionAttributeName,
                        String modelAttributeName,
                        Model model) {
                Object message = session.getAttribute(sessionAttributeName);

                if (message != null) {
                        model.addAttribute(modelAttributeName, message);
                        session.removeAttribute(sessionAttributeName);
                }
        }
}