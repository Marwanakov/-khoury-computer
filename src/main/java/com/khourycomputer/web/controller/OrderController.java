package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.order.SubmitOrderResponse;
import com.khourycomputer.application.service.OrderApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {

        // Temporary until Spring Security provides the logged-in user.
        private static final Long TEMP_USER_ID = 1L;

        private final OrderApplicationService orderApplicationService;

        public OrderController(OrderApplicationService orderApplicationService) {
                this.orderApplicationService = orderApplicationService;
        }

        @PostMapping("/orders/submit")
        public String submitOrder(RedirectAttributes redirectAttributes) {
                SubmitOrderResponse response = orderApplicationService.submitOrder(TEMP_USER_ID);

                redirectAttributes.addFlashAttribute(
                                "confirmationMessage",
                                response.confirmationMessage());

                return "redirect:/orders/confirmation/" + response.order().id();
        }

        @GetMapping("/orders/confirmation/{orderId}")
        public String showOrderConfirmation(
                        @PathVariable Long orderId,
                        Model model) {
                model.addAttribute(
                                "order",
                                orderApplicationService.getOrderByIdForUser(
                                                TEMP_USER_ID,
                                                orderId));

                return "public/order-confirmation";
        }

        @GetMapping("/orders/my-orders")
        public String showMyOrders(Model model) {
                model.addAttribute(
                                "orders",
                                orderApplicationService.listOrdersByUserId(TEMP_USER_ID));

                return "public/my-orders";
        }

        @GetMapping("/orders/{orderId}")
        public String showOrderDetails(
                        @PathVariable Long orderId,
                        Model model) {
                model.addAttribute(
                                "order",
                                orderApplicationService.getOrderByIdForUser(
                                                TEMP_USER_ID,
                                                orderId));

                return "public/order-details";
        }

        @PostMapping("/orders/{orderId}/cancel")
        public String cancelOrder(
                        @PathVariable Long orderId,
                        RedirectAttributes redirectAttributes) {
                try {
                        orderApplicationService.cancelOrderByCustomer(
                                        TEMP_USER_ID,
                                        orderId);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Your order was cancelled successfully.");

                        return "redirect:/orders/" + orderId;

                } catch (IllegalStateException exception) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        exception.getMessage());

                        return "redirect:/orders/" + orderId;

                } catch (IllegalArgumentException exception) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        exception.getMessage());

                        return "redirect:/orders/my-orders";
                }
        }
}