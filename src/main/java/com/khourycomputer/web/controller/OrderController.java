package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.order.SubmitOrderResponse;
import com.khourycomputer.application.service.OrderApplicationService;
import com.khourycomputer.config.security.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final CurrentUserService currentUserService;

    public OrderController(
            OrderApplicationService orderApplicationService,
            CurrentUserService currentUserService
    ) {
        this.orderApplicationService = orderApplicationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/orders/submit")
    public String submitOrder(RedirectAttributes redirectAttributes) {
        Long currentUserId = currentUserService.getCurrentUserId();

        SubmitOrderResponse response =
                orderApplicationService.submitOrder(currentUserId);

        redirectAttributes.addFlashAttribute(
                "confirmationMessage",
                response.confirmationMessage()
        );

        return "redirect:/orders/confirmation/" + response.order().id();
    }

    @GetMapping("/orders/confirmation/{orderId}")
    public String showOrderConfirmation(
            @PathVariable Long orderId,
            Model model
    ) {
        Long currentUserId = currentUserService.getCurrentUserId();

        model.addAttribute(
                "order",
                orderApplicationService.getOrderByIdForUser(
                        currentUserId,
                        orderId
                )
        );

        return "public/order-confirmation";
    }

    @GetMapping("/orders/my-orders")
    public String showMyOrders(Model model) {
        Long currentUserId = currentUserService.getCurrentUserId();

        model.addAttribute(
                "orders",
                orderApplicationService.listOrdersByUserId(currentUserId)
        );

        return "public/my-orders";
    }

    @GetMapping("/orders/{orderId}")
    public String showOrderDetails(
            @PathVariable Long orderId,
            Model model
    ) {
        Long currentUserId = currentUserService.getCurrentUserId();

        model.addAttribute(
                "order",
                orderApplicationService.getOrderByIdForUser(
                        currentUserId,
                        orderId
                )
        );

        return "public/order-details";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(
            @PathVariable Long orderId,
            RedirectAttributes redirectAttributes
    ) {
        Long currentUserId = currentUserService.getCurrentUserId();

        try {
            orderApplicationService.cancelOrderByCustomer(
                    currentUserId,
                    orderId
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your order was cancelled successfully."
            );

            return "redirect:/orders/" + orderId;

        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/orders/" + orderId;

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/orders/my-orders";
        }
    }
}