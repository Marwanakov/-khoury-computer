package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.cart.AddCartItemRequest;
import com.khourycomputer.application.dto.cart.UpdateCartItemQuantityRequest;
import com.khourycomputer.application.service.CartApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    // Temporary user until real login is connected with Spring Security.
    // If your test customer ID is not 1, change this number.
    private static final Long TEMP_USER_ID = 1L;

    private final CartApplicationService cartApplicationService;

    public CartController(CartApplicationService cartApplicationService) {
        this.cartApplicationService = cartApplicationService;
    }

    @GetMapping("/cart")
    public String showCartPage(Model model) {
        model.addAttribute("cart", cartApplicationService.getCartByUserId(TEMP_USER_ID));

        return "public/cart";
    }

    @PostMapping("/cart/items")
    public String addItemToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes
    ) {
        cartApplicationService.addItemToCart(
                TEMP_USER_ID,
                new AddCartItemRequest(productId, quantity)
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product added to cart successfully."
        );

        return "redirect:/cart";
    }

    @PostMapping("/cart/items/update")
    public String updateCartItemQuantity(
            @RequestParam Long productId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes
    ) {
        cartApplicationService.updateItemQuantity(
                TEMP_USER_ID,
                new UpdateCartItemQuantityRequest(productId, quantity)
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Cart quantity updated successfully."
        );

        return "redirect:/cart";
    }

    @PostMapping("/cart/items/remove")
    public String removeProductFromCart(
            @RequestParam Long productId,
            RedirectAttributes redirectAttributes
    ) {
        cartApplicationService.removeProductFromCart(TEMP_USER_ID, productId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product removed from cart."
        );

        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        cartApplicationService.clearCart(TEMP_USER_ID);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Cart cleared successfully."
        );

        return "redirect:/cart";
    }
}